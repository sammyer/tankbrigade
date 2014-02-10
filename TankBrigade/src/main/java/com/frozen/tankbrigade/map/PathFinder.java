package com.frozen.tankbrigade.map;

import android.util.Log;

import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;

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

	private List<MoveSearchNode> nodeSearchQueue=new ArrayList<MoveSearchNode>();
	private MoveSearchNode[][] nodeMap;

	private Comparator<MoveSearchNode> costComparator=new Comparator<MoveSearchNode>() {
		@Override
		public int compare(MoveSearchNode node, MoveSearchNode node2) {
			return node.totalCost-node2.totalCost;
		}
	};

	public MoveSearchNode[][] findLegalMoves(TerrainMap map, GameUnit unit) {
		if (nodeMap==null||nodeMap.length!=map.width()||nodeMap[0].length!=map.height()) {
			nodeMap=new MoveSearchNode[map.width()][map.height()];
		} else {
			for (int i=0;i<nodeMap.length;i++) Arrays.fill(nodeMap[i],null);
		}
		int x=unit.x;
		int y=unit.y;
		nodeSearchQueue.clear();
		MoveSearchNode node=new MoveSearchNode(x,y,null,0);
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

	private void checkNode(TerrainMap map, GameUnit unit, int x, int y, MoveSearchNode prev) {
		if (!map.isInBounds(x,y)) return;
		TerrainType terrain=map.getTerrain(x,y);

		GameUnit mapUnit=map.getUnitAt(x,y);
		int actionType;
		if (mapUnit==null) actionType= MoveSearchNode.MOVE;  //no unit here
		else if (mapUnit.ownerId==unit.ownerId) actionType= MoveSearchNode.PASSTHROUGH;  //friendly unit
		else actionType= MoveSearchNode.ATTACK;  //enemy unit

		if (actionType== MoveSearchNode.ATTACK) {
			Log.i(TAG,"checkNode "+x+","+y+" enemy here");
			return;
		}
		if (!isTraversable(terrain,unit)) {
			Log.i(TAG,"checkNode "+x+","+y+" terrain not traversable");
			return;
		}

		Log.i(TAG,"checkNode "+x+","+y+" terrain="+terrain.name+" costsofar="+prev.totalCost+
				"movecost="+movementCost(terrain,unit)+" maxcost="+unit.movesLeft);

		int cost=movementCost(terrain,unit);
		int totalCost=prev.totalCost+cost;
		if (totalCost>unit.movesLeft) return;

		MoveSearchNode node=nodeMap[x][y];
		if (node!=null&&node.totalCost<=totalCost) return;

		node=new MoveSearchNode(x,y,prev,cost);
		node.actionType=actionType;
		Log.d(TAG,"saving valid move "+node);
		nodeMap[x][y]=node;

		//dont bother adding to queue if cost==movement or if this is an enemy square
		if (totalCost<unit.movesLeft) {
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
		if (unit.type.isLand()&&!terrain.isLand()) return false;
		if (unit.type.isWater()&&!terrain.isWater()) return false;
		if (unit.type.isTank()&&terrain.symbol==TerrainType.MOUNTAIN) return false;
		return true;
	}

	private void searchForAttacks(TerrainMap map, MoveSearchNode[][] nodeMap, GameUnit unit) {
		int range;
		MoveSearchNode node;
		if (unit.type.isRanged()) {
			for (GameUnit mapUnit:map.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				//manhattan distance
				range=Math.abs(unit.x-mapUnit.x)+Math.abs(unit.y-mapUnit.y);
				Log.d(TAG,"check ranged unit - "+unit+" -> "+mapUnit+"  range="+range+"  "+
						unit.type.getMinRange()+"-"+unit.type.getMaxRange());
				if (range>=unit.type.getMinRange()&&range<=unit.type.getMaxRange()) {
					node=new MoveSearchNode(mapUnit.x,mapUnit.y,null,0);
					node.actionType= MoveSearchNode.ATTACK;
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
		MoveSearchNode prev=nodeMap[prevX][prevY];
		if (prev==null||prev.actionType!= MoveSearchNode.MOVE) return;
		if (!unit.type.canAttack(enemyUnit.type)) return;
		int x=enemyUnit.x;
		int y=enemyUnit.y;

		MoveSearchNode curNode=nodeMap[x][y];

		MoveSearchNode node=new MoveSearchNode(x,y,prev,0);
		node.actionType= MoveSearchNode.ATTACK;

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
