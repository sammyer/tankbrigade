package com.frozen.tankbrigade.ai;

import android.graphics.Point;
import android.util.Log;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.map.paths.AttackMap;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
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
		return getDamageDoneCost(move)-0.4f*getDamageTakenCost(move)+getMoveTowardsEnemyBonus(move);
	}

	public float getMoveTowardsEnemyBonus(UnitMove move) {
		return (1-mapAnalyzer.ownerShip[move.x][move.y])*1000;
	}

	public float getDamageDoneCost(UnitMove move) {
		float damage=getDamageDone(move);
		if (damage==0) return 0;
		else return damage*move.attackTarget.type.price;
	}

	public float getDamageTakenCost(UnitMove move) {
		float damage=getDamageTaken(move);
		if (damage==0) return 0;
		else return damage*move.unit.type.price;
	}

	public float getDamageDone(UnitMove move) {
		if (move.attackTarget!=null&&move.unit.type.canAttack(move.attackTarget.type)) {
			TerrainType terrain= gameBoard.getTerrain(move.unit.x,move.unit.y);
			return move.unit.getDamageAgainst(move.attackTarget,terrain);
		} else return 0;
	}

	public float getDamageTaken(UnitMove move) {
		Point endPoint=move.getEndPoint();
		if (endPoint==null) endPoint=new Point(move.unit.x,move.unit.y);
		return getDamageTaken(move.unit, endPoint.x, endPoint.y);
	}

	private float getDamageTaken(GameUnit unit, int x, int y) {
		float damage=0;
		TerrainType terrain= gameBoard.getTerrain(x,y);
		for (AttackMap attackMap:attackMaps) {
			if (DEBUG) Log.d("AI_CostAnalyzer",String.format("check attack map @%d,%d hasUnit=%b canAttack=%b for %s"
				,x,y,attackMap.get(x,y),attackMap.getUnit().type.canAttack(unit.type),attackMap.getUnit()));
			if (attackMap.get(x,y)&&attackMap.getUnit().type.canAttack(unit.type)) {
				if (DEBUG) Log.d("AI_CostAnalyzer","adding damage against - "+attackMap.getUnit().getDamageAgainst(unit,terrain)+"  --  "+attackMap.getUnit()+" -> "+unit);
				damage+=attackMap.getUnit().getDamageAgainst(unit,terrain);
			}
		}
		if (damage>unit.health) damage=unit.health;
		return damage;
	}

	public float getBuildingCaptureScore(UnitMove move) {

	}
}
