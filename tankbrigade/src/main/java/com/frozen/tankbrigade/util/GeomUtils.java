package com.frozen.tankbrigade.util;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

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

	//same as Matrix.setRecttoRect(unit,xform).mapRect(rect)
	public static void transformRect(RectF xform, RectF rect) {
		rect.set(
				rect.left*xform.width()+xform.left,
				rect.top*xform.height()+xform.top,
				rect.right*xform.width()+xform.left,
				rect.bottom*xform.height()+xform.top
				);
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

	public static boolean isArraySize(Object[][] arr, int w, int h) {
		if (arr==null) return false;
		if (arr.length==0) return false;
		if (arr.length!=w) return false;
		if (arr[0]==null) return false;
		if (arr[0].length!=h) return false;
		return true;
	}

	public static boolean isInBounds(Object[][] arr, int x, int y) {
		if (x<0||y<0) return false;
		if (arr==null||arr.length==0||arr[0].length==0) return false;
		if (x>=arr.length) return false;
		if (y>=arr[0].length) return false;
		return true;
	}

	public static float interpolate(float a, float b, float proportion) {
		return a + ((b - a) * proportion);
	}

	public static int interpolateColor(int a, int b, float proportion) {
		float[] hsva = new float[3];
		float[] hsvb = new float[3];
		Color.colorToHSV(a, hsva);
		Color.colorToHSV(b, hsvb);
		for (int i = 0; i < 3; i++) {
			hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
		}
		return Color.HSVToColor(hsvb);
	}

	private static RectF r=new RectF(0,0,1,1);
	public static String matrixToString(Matrix m) {
		r.set(0,0,1,1);
		m.mapRect(r);
		return "[Matrix trans="+r.left+","+r.top+" scale="+r.width()+","+r.height()+"]";
	}

	public static void setRectAspect(RectF rect, float w, float h) {
		float targetRatio=w/h;
		float ratio=rect.width()/rect.height();
		if (ratio==targetRatio) return;
		else if (ratio>targetRatio) {
			float w2=rect.height()*targetRatio;
			float centerX=rect.centerX();
			rect.left=centerX-w2/2;
			rect.right=centerX+w2/2;
		} else {
			float h2=rect.width()/targetRatio;
			float centerY=rect.centerY();
			rect.top=centerY-h2/2;
			rect.bottom=centerY+h2/2;
		}
	}
}
