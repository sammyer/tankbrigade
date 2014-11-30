package com.frozen.tankbrigade.map.anim;

import android.graphics.RectF;

import com.frozen.tankbrigade.util.PosAngle;

/**
 * Created by sam on 02/02/14.
 */
public interface PosAnimation extends MapAnimation {
	public PosAngle getAnimationPos();
	public RectF getAnimationBounds();
}
