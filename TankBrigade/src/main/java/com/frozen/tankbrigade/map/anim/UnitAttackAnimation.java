package com.frozen.tankbrigade.map.anim;

import android.graphics.PointF;
import android.graphics.RectF;

import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.util.PosAngle;

/**
 * Created by sam on 02/02/14.
 */
public class UnitAttackAnimation implements MapAnimation,PosAnimation {
	private static final int ANIMATION_TIME=500;
	private static final float JUMP_AMT=0.5f;

	private long animateStartTime;
	private PosAngle pos;
	private PointF startPos;
	private UnitMove move;
	public boolean isCounterAttack;
	private RectF animationBounds;

	public UnitAttackAnimation(UnitMove move,GameUnit unit) {
		this.move=move;
		pos=unit.getAnimationPos();
		isCounterAttack=(unit!=move.unit);
		startPos=new PointF();
		startPos.set(pos.point);
		animateStartTime=System.currentTimeMillis();
		animationBounds=new RectF(startPos.x,startPos.y-JUMP_AMT,startPos.x,startPos.y);
	}

	public UnitMove getMove() {
		return move;
	}

	@Override
	public boolean isAnimationComplete() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		return timediff>ANIMATION_TIME;
	}

	@Override
	public PosAngle getAnimationPos() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		float percent=(float)timediff/ANIMATION_TIME;
		if (percent<0) percent=0;
		if (percent>1) percent=1;

		float n=percent*2-1;
		pos.point.y=startPos.y-(1-n*n)*JUMP_AMT;
		return pos;
	}

	@Override
	public RectF getAnimationBounds() {
		return animationBounds;
	}

}
