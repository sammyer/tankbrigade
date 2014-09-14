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
class CostAnalyzer {
	private List<AttackMap> attackMaps;
	private GameBoard gameBoard;

	public CostAnalyzer(PathFinder pathFinder, GameBoard map, int playerId) {
		attackMaps=new ArrayList<AttackMap>();

		for (GameUnit unit:map.getUnits()) {
			if (unit.ownerId==playerId) continue;
			SparseMap<UnitMove> moveMap= pathFinder.findLegalMoves(map,unit);
			AttackMap attackMap=new AttackMap(unit,moveMap);
			attackMaps.add(attackMap);
		}
		this.gameBoard =map;
	}

	public CostAnalyzer(GameBoard map, int playerId) {
		PathFinder pathFinder=new PathFinder();
		attackMaps=new ArrayList<AttackMap>();

		for (GameUnit unit:map.getUnits()) {
			if (unit.ownerId==playerId) continue;
			SparseMap<UnitMove> moveMap= pathFinder.findLegalMoves(map,unit);
			AttackMap attackMap=new AttackMap(unit,moveMap);
			attackMaps.add(attackMap);
		}
		this.gameBoard =map;
	}

	public float getScore(UnitMove move) {
		return 2.5f*getDamageDone(move)-getDamageTaken(move);
	}

	float getDamageDone(UnitMove move) {
		if (move.attackTarget!=null&&move.unit.type.canAttack(move.attackTarget.type)) {
			TerrainType terrain= gameBoard.getTerrain(move.unit.x,move.unit.y);
			return move.unit.getDamageAgainst(move.attackTarget,terrain);
		} else return 0;
	}

	float getDamageTaken(UnitMove move) {
		Point endPoint=move.getEndPoint();
		if (endPoint==null) endPoint=new Point(move.unit.x,move.unit.y);
		return getDamageTaken(move.unit, endPoint.x, endPoint.y);
	}

	private float getDamageTaken(GameUnit unit, int x, int y) {
		float damage=0;
		TerrainType terrain= gameBoard.getTerrain(x,y);
		for (AttackMap attackMap:attackMaps) {
			//Log.d("CostAnalyzer","check attack map @"+x+","+y+" result="+attackMap.get(x,y)+","+attackMap.getUnit().type.canAttack(unit.type)+"  for "+attackMap.getUnit());
			if (attackMap.get(x,y)&&attackMap.getUnit().type.canAttack(unit.type)) {
				//Log.d("CostAnalyzer","adding damage against - "+attackMap.getUnit().getDamageAgainst(unit,terrain)+"  --  "+attackMap.getUnit()+" -> "+unit);
				damage+=attackMap.getUnit().getDamageAgainst(unit,terrain);
			}
		}
		if (damage>unit.health) damage=unit.health;
		return damage;
	}
}
