package com.frozen.tankbrigade.map;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sam on 14/01/14.
 */
public class PathFinder {
	private static final String TAG = "PathFinder";

	private List<MoveNode> nodeSearchQueue=new ArrayList<MoveNode>();
	private MoveNode[][] nodeMap;

	private Comparator<MoveNode> costComparator=new Comparator<MoveNode>() {
		@Override
		public int compare(MoveNode node, MoveNode node2) {
			return node.totalCost-node2.totalCost;
		}
	};

	public static class MoveMap {
		public MoveNode[][] map;
		public GameUnit unit;
		public MoveNode selectedMove;

		public MoveMap(MoveNode[][] map, GameUnit unit) {
			this.map = map;
			this.unit = unit;
		}
	}

	public static class MoveNode extends Point {
		private MoveNode prev;
		private int cost;
		private int totalCost;

		private MoveNode(int x, int y, MoveNode prev, int cost, int totalCost) {
			this.x=x;
			this.y=y;
			this.prev = prev;
			this.cost = cost;
			this.totalCost = totalCost;
		}

		public List<Point> getPath() {
			List<Point> path=new ArrayList<Point>();
			MoveNode node=this;
			while (node!=null) {
				path.add(node);
				node=node.prev;
			}
			return path;
		}

		@Override
		public String toString() {
			return "Node("+x+","+y+";"+totalCost+")";
		}
	}

	public MoveMap findLegalMoves(TerrainMap map, GameUnit unit, int x, int y) {
		if (nodeMap==null||nodeMap.length!=map.width()||nodeMap[0].length!=map.height()) {
			nodeMap=new MoveNode[map.width()][map.height()];
		} else {
			for (int i=0;i<nodeMap.length;i++) Arrays.fill(nodeMap[i],null);
		}
		nodeSearchQueue.clear();
		MoveNode node=new MoveNode(x,y,null,0,0);
		nodeMap[x][y]=node;
		nodeSearchQueue.add(node);

		while (nodeSearchQueue.size()>0) {
			Log.d(TAG,"nodeSearchQueue="+Arrays.toString(nodeSearchQueue.toArray()));
			node=nodeSearchQueue.remove(0);
			Log.d(TAG,"checking adjacent to "+node);
			checkNode(map, unit, node.x + 1, node.y, node);
			checkNode(map,unit,node.x-1,node.y,node);
			checkNode(map,unit,node.x,node.y+1,node);
			checkNode(map,unit,node.x,node.y-1,node);
		}
		return new MoveMap(nodeMap,unit);
	}

	private void checkNode(TerrainMap map, GameUnit unit, int x, int y, MoveNode prev) {
		if (!map.isInBounds(x,y)) return;
		TerrainType terrain=map.getTerrain(x,y);

		if (map.getUnitAt(x,y)!=null) {
			Log.w(TAG,"checkNode "+x+","+y+" unit there!");
			return;
		}
		if (!isTraversable(terrain,unit)) {
			Log.w(TAG,"checkNode "+x+","+y+" terrain "+terrain.id+" not traversable");
			return;
		}
		Log.i(TAG,"checkNode "+x+","+y+" terrain="+terrain.id+" costsofar="+prev.totalCost+
				"movecost="+movementCost(terrain,unit)+" maxcost="+unit.type.movement);

		int cost=movementCost(terrain,unit);
		int totalCost=prev.totalCost+cost;
		if (totalCost>unit.type.movement) return;
		MoveNode node=nodeMap[x][y];
		if (node!=null&&node.totalCost<=totalCost) return;
		node=new MoveNode(x,y,prev,cost,totalCost);
		Log.d(TAG,"saving valid move "+node);
		nodeMap[x][y]=node;

		//dont bother adding to queue if cost==movement
		if (totalCost<unit.type.movement) {
			Log.d(TAG,"adding node to searchqueue "+node);
			int queuePos= Collections.binarySearch(nodeSearchQueue, node, costComparator);
			if (queuePos<0) queuePos=-queuePos-1; //see doc for binarysearch
			nodeSearchQueue.add(queuePos,node);
		}
	}

	private int movementCost(TerrainType terrain, GameUnit unit) {
		return 1;
	}

	private boolean isTraversable(TerrainType terrain, GameUnit unit) {
		if (unit.type.mode==GameUnitType.MoveMode.LAND&&!terrain.isLand()) return false;
		if (unit.type.mode==GameUnitType.MoveMode.WATER&&!terrain.isWater()) return false;
		if (unit.type.isTank()&&terrain.id==TerrainType.MOUNTAIN) return false;
		return true;
	}
}
