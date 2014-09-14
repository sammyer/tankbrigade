package com.frozen.tankbrigade.map;

import android.graphics.Point;

import com.frozen.tankbrigade.map.paths.AStarNode;
import com.frozen.tankbrigade.map.model.GameUnit;

/**
 * Created by sam on 02/02/14.
 */
public class UnitMove {
	public static final int NONE=0; //no action
	public static final int MOVE=1; //can move here
	public static final int ATTACK=2; //attack move

	private AStarNode node;
	private Point[] path;
	public GameUnit unit;
	public GameUnit attackTarget;
	public int movementCost;
	public int x;
	public int y;

	public UnitMove(GameUnit unit, AStarNode moveNode, GameUnit attackTarget) {
		if (path!=null&&path.length==0) path=null;
		this.node = moveNode;
		this.movementCost=moveNode.totalCost;
		this.unit = unit;
		this.attackTarget = attackTarget;
		if (attackTarget!=null) {
			x=attackTarget.x;
			y=attackTarget.y;
		} else if (node!=null) {
			x=node.x;
			y=node.y;
		} else {
			x=unit.x;
			y=unit.y;
		}
	}
	public UnitMove(GameUnit unit, Point[] path, int movementCost, GameUnit attackTarget) {
		if (path!=null&&path.length==0) path=null;
		this.path = path;
		this.movementCost=movementCost;
		this.unit = unit;
		this.attackTarget = attackTarget;
		if (attackTarget!=null) {
			x=attackTarget.x;
			y=attackTarget.y;
		} else if (getEndPoint()!=null) {
			Point end=getEndPoint();
			x=end.x;
			y=end.y;
		} else {
			x=unit.x;
			y=unit.y;
		}
	}

	public Point getEndPoint() {
		if (path!=null&&path.length>0) return path[path.length-1];
		else if (node!=null) return new Point(node.x,node.y);
		else return null;
	}

	public Point getAttackPoint() {
		if (attackTarget==null) return null;
		else return attackTarget.getPos();
	}

	public int getActionType() {
		if (attackTarget!=null) return ATTACK;
		else if (x==unit.x&&y==unit.y) return NONE;
		else return MOVE;
	}

	public boolean isAttack() {
		return attackTarget!=null;
	}
	public boolean hasMove() {
		return (path!=null&&path.length>0)||node!=null;
	}

	public Point[] getPath() {
		if (path==null&&node!=null) {
			//lazy evaluation of node path, then you can throw node away
			path=node.getPath();
			node=null;
		}
		return path;
	}

	public String toString() {
		return "[UnitMove unit="+unit.toString()+" moveTo="+getEndPoint()+" attackTarget="+attackTarget+"]";
	}

	public UnitMove createAttackFromMove(GameUnit attackTarget) {
		UnitMove move;
		if (node!=null) move=new UnitMove(unit,node,attackTarget);
		else move=new UnitMove(unit,path,movementCost,attackTarget);
		return move;
	}
}
