package com.frozen.tankbrigade.util;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by sam on 26/01/14.
 */
public class GeomUtils {
	public static final int UP=90;
	public static final int LEFT=180;
	public static final int RIGHT=0;
	public static final int DOWN=270;

	public static PointF interpolatePoint(Point start, Point end, float d) {
		float x=start.x*(1-d)+end.x*d;
		float y=start.y*(1-d)+end.y*d;
		return new PointF(x,y);
	}

	public static int getSquareAngle(Point start, Point end) {
		if (Math.abs(start.x-end.x)>Math.abs(start.y-end.y)) {
			if (start.x>end.x) return LEFT;
			else return RIGHT;
		} else {
			if (start.y>end.y) return UP;
			else return DOWN;
		}
	}

}
