package com.frozen.tankbrigade.map.paths;

import android.graphics.Point;

/**
 * Created by sam on 09/03/14.
 */
public class AStarNode extends Point {
	public int cost;
	public int totalCost;
	public AStarNode prev;

	public AStarNode(int x, int y, AStarNode prev, int cost) {
		this.x=x;
		this.y=y;
		this.prev = prev;
		this.cost = cost;
		this.totalCost = (prev==null?0:prev.totalCost)+cost;
	}

	public Point[] getPath() {
		Point[] path=new Point[getPathLength()];
		AStarNode node=this;
		int i=path.length-1;
		while (node!=null&&i>=0) {
			path[i]=node;
			node=node.prev;
			i--;
		}
		return path;
	}

	protected int getPathLength() {
		int count=0;
		AStarNode node=this;
		while (node!=null) {
			count++;
			node=node.prev;
		}
		return count;
	}
}
