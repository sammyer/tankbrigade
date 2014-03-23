package com.frozen.tankbrigade.util;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
* Created by sam on 17/03/14.
*/
public class TileRect extends RectF {
	private float mapLeft;
	private float mapTop;

	public void setMatrix(Matrix tileToScreen) {
		set(0, 0, 1, 1);
		tileToScreen.mapRect(this);
		mapLeft=left;
		mapTop=top;
	}

	public void setTilePos(int x, int y) {
		offsetTo(mapLeft+x*width(),mapTop+y*height());
	}

	public void setTilePos(float x, float y) {
		offsetTo(mapLeft+x*width(),mapTop+y*height());
	}
}
