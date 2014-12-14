package com.frozen.tankbrigade.map.paths;

import android.util.Log;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.util.SparseMap;

/**
 * Created by sam on 14/01/14.
 */
public class PathFinder {
	private static final String TAG = "PathFinder";

	private AStar aStar;
	private boolean isAIMode=false;

	public PathFinder() {
		aStar=new AStar();
	}

	public void setAIMode(boolean aiMode) {
		isAIMode=aiMode;
	}

	public SparseMap<UnitMove> findLegalMoves(GameBoard board, GameUnit unit) {
		AStarBoardAdapter boardAdapter=new AStarBoardAdapter(board,unit);

		//In CostAnalyzer, when building attack maps, ignore moves left
		boardAdapter.setIgnoreMovesLeft(isAIMode);

		SparseMap<AStarNode> nodeMap=aStar.findMoves(boardAdapter,unit.x,unit.y);
		SparseMap<UnitMove> moveMap=new SparseMap<UnitMove>(board.width(),board.height());
		for (AStarNode node:nodeMap.getAllNodes()) {
			GameUnit occupyingUnit=board.getUnitAt(node.x,node.y);
			if (occupyingUnit!=null&&occupyingUnit!=unit) continue; //cant move into occupied spot
			UnitMove move=new UnitMove(unit,node,null);
			moveMap.set(node.x,node.y,move);
		}

		searchForAttacks(board,moveMap, unit);
		return moveMap;
	}


	private void searchForAttacks(GameBoard board, SparseMap<UnitMove> moveMap, GameUnit unit) {
		int range;
		if (unit.type.isRanged()) {
			for (GameUnit mapUnit:board.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				range=Math.abs(unit.x-mapUnit.x)+Math.abs(unit.y-mapUnit.y);
				//Log.d(TAG,"check ranged unit - "+unit+" -> "+mapUnit+"  range="+range+"  "+unit.type.getMinRange()+"-"+unit.type.getMaxRange());
				if (range>=unit.type.getMinRange()&&range<=unit.type.getMaxRange()) {}
				//ranged units cannot move and attack
				if (unit.canAttackFromCurrentPos(mapUnit)) {
					UnitMove move=new UnitMove(unit,null,0,mapUnit);
					moveMap.set(mapUnit.x, mapUnit.y, move);
				}
			}
		} else {
			for (GameUnit mapUnit:board.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				int x=mapUnit.x;
				int y=mapUnit.y;
				checkAttackNode(moveMap,unit,mapUnit,x+1,y);
				checkAttackNode(moveMap,unit,mapUnit,x-1,y);
				checkAttackNode(moveMap,unit,mapUnit,x,y+1);
				checkAttackNode(moveMap,unit,mapUnit,x,y-1);
			}
		}
	}

	private void checkAttackNode(SparseMap<UnitMove> moveMap, GameUnit unit, GameUnit enemyUnit, int prevX, int prevY) {
		UnitMove prev=moveMap.get(prevX, prevY);
		if (prev==null||prev.isAttack()) return;
		if (!unit.canAttackFrom(enemyUnit, prevX, prevY)) return;
		int x=enemyUnit.x;
		int y=enemyUnit.y;

		UnitMove prevMove=moveMap.get(prevX,prevY);
		UnitMove curMove=moveMap.get(x,y);
		if (curMove!=null&&curMove.movementCost<=prevMove.movementCost) return;

		UnitMove attackMove=prevMove.createAttackFromMove(enemyUnit);
		moveMap.set(x,y,attackMove);
	}

}
