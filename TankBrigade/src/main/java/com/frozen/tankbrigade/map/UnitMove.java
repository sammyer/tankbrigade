package com.frozen.tankbrigade.map;

import android.graphics.Point;

import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.anim.PosAnimation;

/**
 * Created by sam on 02/02/14.
 */
public class UnitMove {

	public Point[] path;
	public GameUnit unit;
	public GameUnit attackTarget;
	public int movementCost;

	public UnitMove(GameUnit unit, Point[] path, int movementCost, GameUnit attackTarget) {
		this.path = path;
		this.movementCost=movementCost;
		this.unit = unit;
		this.attackTarget = attackTarget;
	}

	public Point getEndPoint() {
		if (path==null||path.length==0) return null;
		return path[path.length-1];
	}

	public Point getAttackPoint() {
		if (attackTarget==null) return null;
		else return attackTarget.getPos();
	}


}
