package com.frozen.tankbrigade.map;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/**
* Created by sam on 26/01/14.
*/
public class MoveNode extends Point {
	public static final int MOVE=0; //can move here
	public static final int PASSTHROUGH=1; //can only pass through
	public static final int ATTACK=2; //attack move

	private MoveNode prev;
	private int cost;
	public int totalCost;
	public int actionType=MOVE;

	MoveNode(int x, int y, MoveNode prev, int cost) {
		this.x=x;
		this.y=y;
		this.prev = prev;
		this.cost = cost;
		this.totalCost = (prev==null?0:prev.totalCost)+cost;
	}

	public int getPathLength() {
		int count=0;
		MoveNode node=this;
		while (node!=null) {
			count++;
			node=node.prev;
		}
		return count;
	}

	public Point[] getPath() {
		Point[] path=new Point[getPathLength()];
		MoveNode node=this;
		int i=path.length-1;
		while (node!=null&&i>=0) {
			path[i]=node;
			node=node.prev;
			i--;
		}
		return path;
	}

	@Override
	public String toString() {
		return "Node("+x+","+y+";"+totalCost+")";
	}
}
