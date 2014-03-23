package com.frozen.tankbrigade.ai;

import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.AStar;
import com.frozen.tankbrigade.map.paths.AStarBoardAdapter;

import java.util.Arrays;

/**
 * Created by sam on 09/03/14.
 */
public class MapAnalyzer {
	private float[][] ownerShip;

	public void analyzeMap(GameBoard board) {
		float[][][] weights=new float[2][board.width()][board.height()];
		float[][] unitWeights=new float[board.width()][board.height()];

		ownerShip=new float[board.width()][board.height()];

		int player;
		AStar astar=new AStar();
		AStarAnalyzerMap astarMap;
		for (GameUnit unit:board.getUnits()) {
			player=unit.ownerId-1;
			astarMap=new AStarAnalyzerMap(board,unit);
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
