package com.frozen.tankbrigade.map;

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

	public MoveNode[][] findLegalMoves(TerrainMap map, GameUnit unit, int x, int y) {
		if (nodeMap==null||nodeMap.length!=map.width()||nodeMap[0].length!=map.height()) {
			nodeMap=new MoveNode[map.width()][map.height()];
		} else {
			for (int i=0;i<nodeMap.length;i++) Arrays.fill(nodeMap[i],null);
		}
		nodeSearchQueue.clear();
		MoveNode node=new MoveNode(x,y,null,0);
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

		searchForAttacks(map,nodeMap,unit);

		return nodeMap;
	}

	private void checkNode(TerrainMap map, GameUnit unit, int x, int y, MoveNode prev) {
		if (!map.isInBounds(x,y)) return;
		TerrainType terrain=map.getTerrain(x,y);

		GameUnit mapUnit=map.getUnitAt(x,y);
		int actionType;
		if (mapUnit==null) actionType=MoveNode.MOVE;  //no unit here
		else if (mapUnit.ownerId==unit.ownerId) actionType=MoveNode.PASSTHROUGH;  //friendly unit
		else actionType=MoveNode.ATTACK;  //enemy unit

		if (actionType==MoveNode.ATTACK) {
			Log.i(TAG,"checkNode "+x+","+y+" enemy here");
			return;
		}
		if (!isTraversable(terrain,unit)) {
			Log.i(TAG,"checkNode "+x+","+y+" terrain not traversable");
			return;
		}

		Log.i(TAG,"checkNode "+x+","+y+" terrain="+terrain.id+" costsofar="+prev.totalCost+
				"movecost="+movementCost(terrain,unit)+" maxcost="+unit.type.movement);

		int cost=movementCost(terrain,unit);
		int totalCost=prev.totalCost+cost;
		if (totalCost>unit.type.movement) return;

		MoveNode node=nodeMap[x][y];
		if (node!=null&&node.totalCost<=totalCost) return;

		node=new MoveNode(x,y,prev,cost);
		node.actionType=actionType;
		Log.d(TAG,"saving valid move "+node);
		nodeMap[x][y]=node;

		//dont bother adding to queue if cost==movement or if this is an enemy square
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

	private void searchForAttacks(TerrainMap map, MoveNode[][] nodeMap, GameUnit unit) {
		boolean isRangedUnit=unit.type.range>1;
		int range;
		MoveNode node;
		if (isRangedUnit) {
			for (GameUnit mapUnit:map.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				//manhattan distance
				range=Math.abs(unit.x-mapUnit.x)+Math.abs(unit.y-mapUnit.y);
				Log.d(TAG,"check ranged unit - "+unit+" -> "+mapUnit+"  range="+range+"  "+unit.type.minRange+"-"+unit.type.range);
				if (range>=unit.type.minRange&&range<=unit.type.range) {
					node=new MoveNode(mapUnit.x,mapUnit.y,null,0);
					node.actionType=MoveNode.ATTACK;
					nodeMap[mapUnit.x][mapUnit.y]=node;
				}
			}
		} else {
			for (GameUnit mapUnit:map.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				int x=mapUnit.x;
				int y=mapUnit.y;
				checkAttackNode(unit,mapUnit,x+1,y);
				checkAttackNode(unit,mapUnit,x-1,y);
				checkAttackNode(unit,mapUnit,x,y+1);
				checkAttackNode(unit,mapUnit,x,y-1);
			}
		}
	}

	private void checkAttackNode(GameUnit unit, GameUnit enemyUnit, int prevX, int prevY) {
		if (!isInBounds(nodeMap,prevX,prevY)) return;
		MoveNode prev=nodeMap[prevX][prevY];
		if (prev==null) return;
		int x=enemyUnit.x;
		int y=enemyUnit.y;

		MoveNode curNode=nodeMap[x][y];

		MoveNode node=new MoveNode(x,y,prev,0);
		node.actionType=MoveNode.ATTACK;

		if (curNode==null||node.totalCost<curNode.totalCost) {
			nodeMap[x][y]=node;
		}
	}

	private static boolean isInBounds(Object[][] arr, int x, int y) {
		if (x<0||y<0) return false;
		if (arr==null||arr.length==0||arr[0].length==0) return false;
		if (x>=arr.length) return false;
		if (y>=arr[0].length) return false;
		return true;
	}
}
