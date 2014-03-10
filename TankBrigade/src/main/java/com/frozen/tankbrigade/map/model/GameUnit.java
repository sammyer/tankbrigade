package com.frozen.tankbrigade.map.model;

import android.graphics.Point;
import android.graphics.PointF;
import android.util.Log;

import com.frozen.tankbrigade.map.anim.PosAnimation;
import com.frozen.tankbrigade.util.PosAngle;

/**
 * Created by sam on 12/01/14.
 */
public class GameUnit {
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

	public PosAngle getAnimationPos() {
		if (animation!=null&&animation.isAnimationComplete()) animation=null;
		if (animation==null) {
			if (animationPos==null) animationPos=new PosAngle(new PointF(x,y),0);
			else {
				animationPos.point.x=x;
				animationPos.point.y=y;
			}
		}
		else animationPos=animation.getAnimationPos();

		return animationPos;
	}

	public Point getPos() {
		return new Point(x,y);
	}

	public int getDamageAgainst(GameUnit defender, TerrainType terrain) {
		float damage=type.damage*type.getDamageMultiplier(defender.type);
		if (!defender.type.isAir()) damage*=(1-terrain.defense);
		return Math.round(damage);
	}

	public boolean canAttackFromCurrentPos(GameUnit defender) {
		return canAttackFrom(defender,x,y);
	}

	public boolean canAttackFrom(GameUnit defender, int attackX, int attackY) {
		if (!attacksLeft) return false;
		if (!type.canAttack(defender.type)) return false;
		//manhattan distance
		int range=Math.abs(attackX-defender.x)+Math.abs(attackY-defender.y);
		if (type.isRanged()) {
			if (range<type.getMinRange()||range>type.getMaxRange()) return false;
		} else {
			if (range!=1) return false;
		}
		return true;
	}

	public void startNewTurn() {
		movesLeft=type.movement;
		attacksLeft=true;
	}

	public void setAttackUsed() {
		attacksLeft=false;
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
