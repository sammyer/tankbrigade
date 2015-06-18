package com.frozen.tankbrigade.map.model;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.frozen.tankbrigade.map.anim.PosAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.util.PosAngle;

/**
 * Created by sam on 12/01/14.
 */
public class GameUnit implements Ordered2D {
	public static final String TAG="GameUnit";

	public GameUnitType type;
	public int x;
	public int y;
	public int ownerId;
	public int health;
	public int movesLeft;
	private boolean attacksLeft;

	//unit this was originally cloned from
	//for calculating possibly board states
	private GameUnit originalUnit;

	private PosAngle animationPos;
	private PosAnimation animation;

	public GameUnit(GameUnitType type, int x, int y, int ownerId) {
		this.x=x;
		this.y=y;
		this.type = type;
		this.ownerId = ownerId;
		health=type.health;
		movesLeft=type.movement;
		attacksLeft=true;
	}

	public void setAnimation(PosAnimation animation) {
		this.animation=animation;
	}

	public boolean isAnimating() {
		return animation!=null;
	}

	public void updateAnimationPos() {
		if (animationPos==null) animationPos=new PosAngle(new PointF(x,y),0);
		if (animation!=null&&animation.isAnimationComplete()) animation=null;
		if (animation==null) {
			animationPos.point.x=x;
			animationPos.point.y=y;
		}
		else animationPos=animation.getAnimationPos();
	}

	public PosAngle getAnimationPos() {
		if (animationPos==null) animationPos=new PosAngle(new PointF(x,y),0);
		return animationPos;
	}

	public Point getPos() {
		return new Point(x,y);
	}

	public int getDamageAgainst(GameUnit defender, TerrainType terrain) {
		float healthPercent=health/(float)type.health;
		float damage=type.damage*healthPercent*type.getDamageMultiplier(defender.type);
		if (!defender.type.isAir()) damage*=(1-terrain.defense);
		Log.i(TAG,String.format("getDamage %s against %s ---- base_damage=%d att=%d def=%d mult=%.2f health=%.2f%% air=%b terrain=%.2f damage=%d",
				toString(),defender.toString(),type.damage,type.attackType,defender.type.defenseType,
				type.getDamageMultiplier(defender.type),healthPercent,defender.type.isAir(),1-terrain.defense,Math.round(damage)));
		return Math.round(damage);
	}

	public boolean canAttackFromCurrentPos(GameUnit defender) {
		return canAttackFrom(defender,x,y);
	}

	public boolean canAttackFrom(GameUnit defender, int attackX, int attackY) {
		if (!attacksLeft) return false;
		return type.canAttack(defender.type,attackX,attackY,defender.x,defender.y);
	}

	public void startNewTurn() {
		movesLeft=type.movement;
		attacksLeft=true;
	}

	public void setAttackUsed() {
		attacksLeft=false;
	}


	@Override
	public int getOrderX() {
		return Math.round(getAnimationPos().point.x);
	}

	@Override
	public int getOrderY() {
		return (int)Math.ceil(getAnimationPos().point.y);
	}

	public String toString() {
		return "[GameUnit pos="+x+","+y+" type="+type.name+" player="+ownerId+"]";
	}

	public GameUnit getOriginalUnit() {
		if (originalUnit==null) return this;
		else return  originalUnit;
	}

	public GameUnit clone() {
		GameUnit unit=new GameUnit(type,x,y,ownerId);
		unit.health=health;
		unit.movesLeft=movesLeft;
		unit.attacksLeft=attacksLeft;
		unit.originalUnit=getOriginalUnit();
		return unit;
	}

}
