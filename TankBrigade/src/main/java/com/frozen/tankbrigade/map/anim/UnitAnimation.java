package com.frozen.tankbrigade.map.anim;

import android.graphics.Point;
import android.graphics.PointF;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.util.GeomUtils;
import com.frozen.tankbrigade.util.PosAngle;

/**
 * Created by sam on 02/02/14.
 */
public class UnitAnimation implements PosAnimation,MapAnimation {
	private static final float ANIMATION_TIME=300;
	private long animateStartTime;
	private Point[] path;
	private UnitMove move;

	public UnitAnimation(UnitMove move) {
		this.move=move;
		this.path=move.getPath();
		animateStartTime=System.currentTimeMillis();
	}

	public UnitMove getMove() {
		return move;
	}

	@Override
	public boolean isAnimationComplete() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		return timediff>ANIMATION_TIME*(path.length-1);
	}

	@Override
	public PosAngle getAnimationPos() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		float animationPos=timediff/ANIMATION_TIME;

		int numMoves=path.length-1;
		if (numMoves<1) {
			return null;
		}

		int moveId=(int)Math.floor(animationPos);
		Point start;
		Point end;
		if (moveId>=numMoves) {
			start=path[numMoves-1];
			end=path[numMoves];
			return new PosAngle(new PointF(end),
					GeomUtils.getSquareAngle(start, end));
		} else {
			start=path[moveId];
			end=path[moveId+1];
			float moveFraction=animationPos-moveId;
			return new PosAngle(GeomUtils.interpolatePoint(start,end,moveFraction),
					GeomUtils.getSquareAngle(start,end));
		}
	}

}
