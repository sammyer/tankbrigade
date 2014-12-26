package com.frozen.tankbrigade.ai;

import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.AStar;
import com.frozen.tankbrigade.map.paths.AStarBoardAdapter;
import com.frozen.tankbrigade.map.paths.AStarNode;
import com.frozen.tankbrigade.util.SparseMap;

/**
 * Created by sam on 09/03/14.
 */
public class BorderMapAnalyzer implements MapAnalyzer {
	private float[][] ownerShip;

	//create a series of weights to see if each square is more in player 1's territory or player 2's territory
	public void analyzeMap(GameBoard board, int curPlayer) {
		float[][][] weights=new float[2][board.width()][board.height()];

		ownerShip=new float[board.width()][board.height()];

		int playerId;
		AStar astar=new AStar();
		AStarAnalyzerMap astarMap;
		SparseMap<AStarNode> moves;
		for (GameUnit unit:board.getUnits()) {
			playerId=(unit.ownerId==curPlayer)?0:1;
			astarMap=new AStarAnalyzerMap(board,unit);
			astarMap.setIgnoreEnemyUnits(true);
			moves=astar.findMoves(astarMap,unit.x,unit.y);
			for (AStarNode move:moves.getAllNodes()) {
				int unitMoves=unit.type.movement;
				//heuristic for unit weight score set to 1/x  where x is number of turns to get to that place
				//i.e. x=num moves/unit moves per turn
				weights[playerId][move.x][move.y]+=unitMoves/(float)(move.totalCost+unitMoves);
			}
		}

		float totalWeight;
		for (int i=0;i<board.width();i++) {
			for (int j=0;j<board.height();j++) {
				totalWeight=weights[0][i][j]+weights[1][i][j];
				if (totalWeight==0) ownerShip[i][j]=-1;
				else ownerShip[i][j]=weights[0][i][j]/totalWeight;
			}
		}
	}

	public float getMoveBonus(int x, int y) {
		return 1-ownerShip[x][y];
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
