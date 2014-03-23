package com.frozen.tankbrigade.ui;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;

import com.frozen.tankbrigade.map.MapDrawParameters;
import com.frozen.tankbrigade.map.model.GameBoard;

/**
 * Created by sam on 17/03/14.
 */
public interface MapDrawer {
	public void drawMap(Canvas canvas, GameBoard map, Matrix tileToScreen,MapDrawParameters params);
	public Point getMapPosFromScreen(float screenX, float screenY, Matrix tileToScreen, GameBoard map);
	public RectF getScreenBounds(Rect mapBounds);
}
