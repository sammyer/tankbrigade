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
public interface MapAnalyzer {
	public void analyzeMap(GameBoard board, int curPlayer);
	public float getMoveBonus(int x, int y);
}
