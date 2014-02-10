package com.frozen.tankbrigade.map;

import android.graphics.Point;

import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.TerrainMap;

/**
* Created by sam on 26/01/14.
*/
public class MoveSearchNode extends Point {
	public static final int MOVE=0; //can move here
	public static final int PASSTHROUGH=1; //can only pass through
	public static final int ATTACK=2; //attack move

	private MoveSearchNode prev;
	private int cost;
	public int totalCost;
	public int actionType=MOVE;

	MoveSearchNode(int x, int y, MoveSearchNode prev, int cost) {
		this.x=x;
		this.y=y;
		this.prev = prev;
		this.cost = cost;
		this.totalCost = (prev==null?0:prev.totalCost)+cost;
	}

	public int getPathLength() {
		int count=0;
		MoveSearchNode node=this;
		while (node!=null) {
			count++;
			node=node.prev;
		}
		return count;
	}

	protected Point[] getPath() {
		Point[] path=new Point[getPathLength()];
		MoveSearchNode node=this;
		int i=path.length-1;
		while (node!=null&&i>=0) {
			path[i]=node;
			node=node.prev;
			i--;
		}
		return path;
	}

	public UnitMove getMove(GameUnit unit, TerrainMap map) {
		GameUnit enemyUnit=null;
		Point[] path;
		if (actionType==ATTACK) {
			enemyUnit=map.getUnitAt(x,y);
			if (prev==null) path=null;
			else path=prev.getPath();
		} else {
			path=getPath();
		}
		return new UnitMove(unit,path,totalCost,enemyUnit);
	}

	@Override
	public String toString() {
		return "Node("+x+","+y+";"+totalCost+")";
	}
}
