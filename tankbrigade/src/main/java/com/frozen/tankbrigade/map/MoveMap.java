package com.frozen.tankbrigade.map;

import android.graphics.Point;
import android.graphics.PointF;

import com.frozen.tankbrigade.util.GeomUtils;

import java.util.List;

/**
* Created by sam on 26/01/14.
*/
public class MoveMap {
	private static final float ANIMATION_TIME=300;

	public MoveNode[][] map;
	public GameUnit unit;

	private MoveNode selectedMove;
	private boolean showMoves=true;
	private boolean showPath=false;
	private boolean animating=false;
	private long animateStartTime;

	public MoveMap(MoveNode[][] map, GameUnit unit) {
		this.map = map;
		this.unit = unit;
	}

	public boolean getShowMoves() {
		return showMoves;
	}

	public boolean getShowPath() {
		return showPath&&selectedMove!=null;
	}

	public MoveNode getSelectedMove() {
		return selectedMove;
	}
	public boolean isAnimating() {
		return animating;
	}

	public boolean isAnimationComplete() {
		if (!animating||selectedMove==null) return true;
		int numMoves=selectedMove.getPathLength()-1;
		long timediff=System.currentTimeMillis()-animateStartTime;
		return (timediff/ANIMATION_TIME)>=numMoves;
	}

	//select mode

	public void showMove(MoveNode move) {
		selectedMove=move;
		showPath=move!=null;
		showMoves=true;
		animating=false;
	}

	public void showAllMoves() {
		selectedMove=null;
		showPath=false;
		showMoves=true;
		animating=false;
	}

	public void animateMove(MoveNode move) {
		if (move==null) return;
		selectedMove=move;
		animateStartTime=System.currentTimeMillis();
		animating=true;
		showPath=false;
		showMoves=false;
	}

	public PosAngle getAnimationPosition() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		float animationPos=timediff/ANIMATION_TIME;
		Point[] moves=selectedMove.getPath();
		int numMoves=moves.length-1;
		if (numMoves<2) {
			return null;
		}

		int moveId=(int)Math.floor(animationPos);
		Point start;
		Point end;
		if (moveId>=numMoves) {
			start=moves[numMoves-1];
			end=moves[numMoves];
			return new PosAngle(new PointF(end),
					GeomUtils.getSquareAngle(start,end));
		} else {
			start=moves[moveId];
			end=moves[moveId+1];
			float moveFraction=animationPos-moveId;
			return new PosAngle(GeomUtils.interpolatePoint(start,end,moveFraction),
					GeomUtils.getSquareAngle(start,end));
		}
	}

	public static class PosAngle {
		public PointF point;
		public int angle;

		public PosAngle(PointF point, int angle) {
			this.point = point;
			this.angle = angle;
		}

	}
}
