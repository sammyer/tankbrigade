package com.frozen.tankbrigade.ai;

import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.AStar;
import com.frozen.tankbrigade.map.paths.AStarBoardAdapter;
import com.frozen.tankbrigade.map.paths.AStarNode;
import com.frozen.tankbrigade.util.SparseMap;

/**
 * Created by sam on 26/12/14.
 */
public class ClosesUnitMapAnalyzer implements MapAnalyzer {
	private float[][] ownership;
	private int numUnits;

	public void analyzeMap(GameBoard board, int curPlayer) {
		ownership=new float[board.width()][board.height()];

		AStar astar=new AStar();
		AStarAnalyzerMap astarMap;
		SparseMap<AStarNode> moves;
		numUnits=0;
		for (GameUnit unit:board.getUnits()) {
			if (unit.ownerId==curPlayer) continue;
			astarMap=new AStarAnalyzerMap(board,unit);
			astarMap.setIgnoreEnemyUnits(true);
			moves=astar.findMoves(astarMap,unit.x,unit.y);
			for (AStarNode move:moves.getAllNodes()) {
				int unitMoves=unit.type.movement;
				//heuristic for unit weight score set to 1/x  where x is number of turns to get to that place
				//i.e. x=num moves/unit moves per turn
				ownership[move.x][move.y]+=unitMoves/(float)(move.totalCost+unitMoves);
			}
			numUnits++;
		}
	}

	public float getMoveBonus(int x, int y) {
		return ownership[x][y]/numUnits;
	}

	private static class AStarAnalyzerMap extends AStarBoardAdapter {
		public AStarAnalyzerMap(GameBoard board, GameUnit unit) {
			super(board,unit);
		}

		@Override
		public int getMaxCost() {
			return 50;
		}

	}

}
