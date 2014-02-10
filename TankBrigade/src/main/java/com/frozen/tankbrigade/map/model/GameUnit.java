package com.frozen.tankbrigade.map.model;

import android.graphics.Point;
import android.graphics.PointF;

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

	private PosAngle pos;
	private PosAnimation animation;

	public GameUnit(GameUnitType type, int x, int y, int ownerId) {
		this.x=x;
		this.y=y;
		pos=new PosAngle(new PointF(x,y),0);
		this.type = type;
		this.ownerId = ownerId;
		health=type.health;
		movesLeft=type.movement;
	}

	public void setAnimation(PosAnimation animation) {
		this.animation=animation;
	}

	public PosAngle getAnimationPos() {
		if (animation!=null&&animation.isAnimationComplete()) animation=null;
		if (animation==null) {
			pos.point.x=x;
			pos.point.y=y;
		}
		else pos=animation.getAnimationPos();
		return pos;
	}

	public Point getPos() {
		return new Point(x,y);
	}

	public int getDamageAgainst(GameUnit defender, TerrainType terrain) {
		float damage=type.damage*type.getDamageMultiplier(defender.type);
		if (!defender.type.isAir()) damage*=(1-terrain.defense);
		return Math.round(damage);
	}


	public String toString() {
		return "[GameUnit pos="+x+","+y+" type="+type.name+" player="+ownerId+"]";
	}
}
