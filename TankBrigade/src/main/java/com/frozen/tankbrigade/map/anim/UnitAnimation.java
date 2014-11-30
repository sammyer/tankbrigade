package com.frozen.tankbrigade.map.anim;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;

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
	private RectF animationBounds;

	public UnitAnimation(UnitMove move) {
		this.move=move;
		this.path=move.getPath();
		getBoundsFromPath();
		animateStartTime=System.currentTimeMillis();
	}

	private void getBoundsFromPath() {
		if (path.length>0) animationBounds=null;
		animationBounds=new RectF(path[0].x,path[0].y,path[0].x,path[0].y);
		for (int i=1;i<path.length;i++) {
			int x=path[i].x;
			int y=path[i].y;
			if (x<animationBounds.left) animationBounds.left=x;
			if (x>animationBounds.right) animationBounds.right=x;
			if (y<animationBounds.top) animationBounds.top=y;
			if (y>animationBounds.bottom) animationBounds.bottom=y;
		}
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

	@Override
	public RectF getAnimationBounds() {
		return animationBounds;
	}

}
