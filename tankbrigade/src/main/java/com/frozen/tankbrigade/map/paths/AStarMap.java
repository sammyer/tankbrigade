package com.frozen.tankbrigade.map.paths;

import android.graphics.Point;

/**
 * Created by sam on 09/03/14.
 */
public interface AStarMap {
	public boolean canMoveHere(int x, int y);
	public int getCost(int x, int y);
	public int getMaxCost();
}
