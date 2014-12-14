package com.frozen.tankbrigade.ai;

import android.graphics.Point;
import android.util.Log;

import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.moves.DamageInfo;
import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.map.paths.AttackMap;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
* Created by sam on 26/03/14.
*/
public class CostAnalyzer {
	private List<AttackMap> attackMaps;
	private GameBoard gameBoard;
	private MapAnalyzer mapAnalyzer;
	public boolean DEBUG=false;

	public CostAnalyzer(PathFinder pathFinder, GameBoard map, int playerId) {
		attackMaps=new ArrayList<AttackMap>();
		mapAnalyzer=new MapAnalyzer();
		mapAnalyzer.analyzeMap(map, playerId);

		pathFinder.setAIMode(true);
		for (GameUnit unit:map.getUnits()) {
			if (unit.ownerId==playerId) continue;
			SparseMap<UnitMove> moveMap= pathFinder.findLegalMoves(map,unit);
			AttackMap attackMap=new AttackMap(unit,moveMap);
			attackMaps.add(attackMap);
		}
		this.gameBoard =map;
	}

	public CostAnalyzer(GameBoard map, int playerId) {
		this(new PathFinder(),map,playerId);
	}

	public float getScore(UnitMove move) {
		DamageInfo damageDone=getDamageDone(move);
		DamageInfo damageTaken=getDamageTaken(move);
		float score=damageDone.cost-0.4f*damageTaken.cost;
		score+=getMoveTowardsEnemyBonus(move);
		score+=getBuildingCaptureScore(move,damageDone.isKill,damageTaken.isKill);
		return score;
	}

	//---------------------------------------- UNIT ADVANCE BONUS---------------------------------------------

	public float getMoveTowardsEnemyBonus(UnitMove move) {
		return (1-mapAnalyzer.ownerShip[move.x][move.y])*10;
	}

	//-------------------------------- ATTACK/DEFENSE --------------------------------------------------

	public DamageInfo getDamageTaken(UnitMove move) {
		return getDamageTaken(move.unit,move.getEndPoint());
	}

	private List<DamageInfo> damageList=new ArrayList<DamageInfo>();
	private DamageInfo getDamageTaken(GameUnit unit, Point unitPos) {
		getDamageTakenList(unit, unitPos, damageList);
		if (damageList.size()==0) return DamageInfo.NO_DAMAGE;
		if (damageList.size()==1) return damageList.get(0);
		Collections.sort(damageList,new Comparator<DamageInfo>() {
			@Override
			public int compare(DamageInfo damageInfo, DamageInfo damageInfo2) {
				return Float.compare(damageInfo2.cost,damageInfo.cost); //reverse order
			}
		});
		DamageInfo totalDamage=new DamageInfo();
		for (int i=0;i<2;i++) {
			DamageInfo damage=damageList.get(i);
			totalDamage.counterDamage +=damage.counterDamage;
			if (damage.damage+totalDamage.damage>unit.health) {
				totalDamage.damage=unit.health;
				totalDamage.isKill=true;
				totalDamage.cost=unit.health*unit.type.price;
			} else {
				totalDamage.damage+=damage.damage;
				totalDamage.cost+=damage.cost;
			}
		}
		return totalDamage;
	}

	private void getDamageTakenList(GameUnit unit, Point unitPos, List<DamageInfo> damageList) {
		damageList.clear();
		for (AttackMap attackMap:attackMaps) {
			//if (DEBUG) Log.d("AI_CostAnalyzer",String.format("check attack map @%d,%d hasUnit=%b canAttack=%b for %s"
			//	,x,y,attackMap.get(x,y),attackMap.getUnit().type.canAttack(unit.type),attackMap.getUnit()));
			if (attackMap.get(unitPos.x,unitPos.y)) {
				DamageInfo damage=getDamageInfo(attackMap.getUnit(),unit,attackMap.getAttackOrigin(unitPos.x,unitPos.y),unitPos);
				if (DEBUG) Log.d("AI_DEBUG","Damage "+attackMap.getUnit()+" vs "+unit+" = "+damage);
				if (damage.damage>0&&damage.cost>0) {
					damageList.add(damage);
					//if (DEBUG) Log.d("AI_CostAnalyzer","adding damage against - "+attackMap.getUnit().getDamageAgainst(unit,terrain)+"  --  "+attackMap.getUnit()+" -> "+unit);
				}
			}
		}
	}

	public DamageInfo getDamageDone(UnitMove move) {
		return getDamageInfo(move.unit,move.attackTarget,move.getEndPoint(),null);
	}

	private DamageInfo getDamageInfo(GameUnit attacker, GameUnit defender, Point attackPoint, Point defensePoint) {
		if (attacker==null||defender==null) return DamageInfo.NO_DAMAGE;
		int attx=attackPoint==null?attacker.x:attackPoint.x;
		int atty=attackPoint==null?attacker.y:attackPoint.y;
		int defx=defensePoint==null?defender.x:defensePoint.x;
		int defy=defensePoint==null?defender.y:defensePoint.y;
		if (!attacker.type.canAttack(defender.type,attx,atty,defx,defy)) return DamageInfo.NO_DAMAGE;
		TerrainType terrain= gameBoard.getTerrain(defx,defy);
		DamageInfo damageInfo;
		float damage=attacker.getDamageAgainst(defender,terrain);
		float attackCost;
		if (damage>=defender.health) {
			attackCost=defender.health/defender.type.health*defender.type.price;
			return new DamageInfo(defender.health,attackCost,0,0,true);
		}
		attackCost=damage/defender.type.health*defender.type.price;
		if (DEBUG) Log.d("AI_DEBUG",String.format("Defender can attack? - %d,%d,%s -> %d,%d,%s = %b",
				defx,defy,defender.type.name,attx,atty,attacker.type.name,
				defender.type.canAttack(attacker.type,defx,defy,attx,atty)));
		if (!defender.type.canAttack(attacker.type,defx,defy,attx,atty)) {
			return new DamageInfo(damage,attackCost,0,0,false);
		}

		terrain= gameBoard.getTerrain(attx,atty);
		//for sake of calculations, temporary reduce health
		int originalHealth=defender.health;
		defender.health-=damage;
		float returnDamage=defender.getDamageAgainst(attacker,terrain);
		defender.health=originalHealth;
		float defenseCost=returnDamage/attacker.type.health*attacker.type.price;
		return new DamageInfo(damage,attackCost,returnDamage,defenseCost,false);
	}

	//--------------------------------------  BUILDINGS ------------------------------------------

	public int getBuildingCaptureScore(UnitMove move,boolean kills, boolean isKilled) {
		int score=0;
		int playerId=move.unit.ownerId;
		Point movePos;
		if (isKilled) {
			Building oldBuilding=gameBoard.getBuildingAt(move.unit.x,move.unit.y);
			if (oldBuilding!=null) score+=ownershipChangeScore(oldBuilding,playerId,playerId,Player.NONE);
		} else if (move.hasMove()&&move.unit.type.canCaptureBuildings()) {
			movePos=move.getEndPoint();
			Building newBuilding=gameBoard.getBuildingAt(movePos.x,movePos.y);
			Building oldBuilding=gameBoard.getBuildingAt(move.unit.x,move.unit.y);
			if (oldBuilding!=null) score+=ownershipChangeScore(oldBuilding,playerId,playerId,Player.NONE);
			if (newBuilding!=null) score+=ownershipChangeScore(newBuilding,playerId,Player.NONE,playerId);
		}
		if (move.isAttack()) {
			movePos=move.getAttackPoint();
			Building attackBuilding=gameBoard.getBuildingAt(movePos.x,movePos.y);
			if (attackBuilding!=null) {
				if (kills) score+=ownershipChangeScore(attackBuilding,
						playerId,move.attackTarget.ownerId,Player.NONE);
			}
		}
		return score;
	}

	private int ownershipChangeScore(Building building, int playerId, int oldOccupier, int newOccupier) {
		return building.getAIValue()*ownershipChange(playerId,
				building.ownerIfOccupiedBy(oldOccupier),
				building.ownerIfOccupiedBy(newOccupier));
	}
	private int ownershipChange(int playerId, int oldOwnerId, int newOwnerId) {
		return comparePlayer(playerId,newOwnerId)-comparePlayer(playerId,oldOwnerId);
	}

	//returns 1 if is same player, -1 if different players, or 0 if comparing to neutral
	public static int comparePlayer(int playerId1, int playerId2) {
		if (playerId1==Player.NONE||playerId2==Player.NONE) return 0;
		else if (playerId1==playerId2) return 1;
		else return -1;
	}

}
