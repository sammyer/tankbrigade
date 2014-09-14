package com.frozen.tankbrigade.ai;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.AStar;
import com.frozen.tankbrigade.map.paths.AStarBoardAdapter;
import com.frozen.tankbrigade.map.paths.AStarNode;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.Arrays;

/**
 * Created by sam on 09/03/14.
 */
public class MapAnalyzer {
	private float[][] ownerShip;

	//create a series of weights to see if each square is more in player 1's territory or player 2's territory
	public void analyzeMap(GameBoard board) {
		float[][][] weights=new float[2][board.width()][board.height()];
		float[][] unitWeights=new float[board.width()][board.height()];

		ownerShip=new float[board.width()][board.height()];

		int player;
		AStar astar=new AStar();
		AStarAnalyzerMap astarMap;
		SparseMap<AStarNode> moves;
		for (GameUnit unit:board.getUnits()) {
			player=unit.ownerId-1;
			astarMap=new AStarAnalyzerMap(board,unit);
			moves=astar.findMoves(astarMap,unit.x,unit.y);
			for (AStarNode move:moves.getAllNodes()) {
				int unitMoves=unit.type.movement;
				//heuristic for unit weight score set to 1/x  where x is number of turns to get to that place
				//i.e. x=num moves/unit moves per turn
				weights[player][move.x][move.y]+=unitMoves/(float)(move.totalCost+unitMoves);
			}
		}
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
