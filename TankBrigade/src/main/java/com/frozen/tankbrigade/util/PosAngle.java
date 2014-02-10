package com.frozen.tankbrigade.util;

import android.graphics.PointF;

/**
* Created by sam on 02/02/14.
*/
public class PosAngle {
	public PointF point;
	public int angle;

	public PosAngle(PointF point, int angle) {
		this.point = point;
		this.angle = angle;
	}

	public String toString() {
		return "PosAngle["+point+","+angle+"]";
	}
}
