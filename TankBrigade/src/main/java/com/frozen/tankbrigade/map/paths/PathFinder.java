package com.frozen.tankbrigade.map.paths;

import android.util.Log;

import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameBoard;
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

	private List<PathNode> nodeSearchQueue=new ArrayList<PathNode>();
	private IPathMap nodeMap;

	private Comparator<PathNode> costComparator=new Comparator<PathNode>() {
		@Override
		public int compare(PathNode node, PathNode node2) {
			return node.totalCost-node2.totalCost;
		}
	};

	public PathFinder() {
		nodeMap=new PathMap();
	}

	public PathFinder(IPathMap nodeMap) {
		this.nodeMap=nodeMap;
	}


	public IPathMap findLegalMoves(GameBoard map, GameUnit unit) {
		nodeMap.init(map.width(),map.height());

		int x=unit.x;
		int y=unit.y;
		nodeSearchQueue.clear();
		PathNode node=new PathNode(x,y,null,0);
		nodeMap.setNode(x,y,node);
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

		searchForValidAttacks(map, nodeMap, unit);
		return nodeMap;
	}

	private void checkNode(GameBoard map, GameUnit unit, int x, int y, PathNode prev) {
		if (!map.isInBounds(x,y)) return;
		TerrainType terrain=map.getTerrain(x,y);

		GameUnit mapUnit=map.getUnitAt(x,y);
		int actionType;
		if (mapUnit==null) actionType= PathNode.MOVE;  //no unit here
		else if (mapUnit.ownerId==unit.ownerId) actionType= PathNode.PASSTHROUGH;  //friendly unit
		else actionType= PathNode.ATTACK;  //enemy unit

		if (actionType== PathNode.ATTACK) {
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

		PathNode node=nodeMap.getNode(x,y);
		if (node!=null&&node.totalCost<=totalCost) return;

		node=new PathNode(x,y,prev,cost);
		node.actionType=actionType;
		Log.d(TAG,"saving valid move "+node);
		nodeMap.setNode(x,y,node);

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

	private void searchForValidAttacks(GameBoard map, IPathMap nodeMap, GameUnit unit) {
		int range;
		PathNode node;
		if (unit.type.isRanged()) {
			for (GameUnit mapUnit:map.getUnits()) {
				if (mapUnit.ownerId==unit.ownerId) continue;
				range=Math.abs(unit.x-mapUnit.x)+Math.abs(unit.y-mapUnit.y);
				Log.d(TAG,"check ranged unit - "+unit+" -> "+mapUnit+"  range="+range+"  "+
						unit.type.getMinRange()+"-"+unit.type.getMaxRange());
				if (range>=unit.type.getMinRange()&&range<=unit.type.getMaxRange()) {}
				//ranged units cannot move and attack
				if (unit.canAttackFromCurrentPos(mapUnit)) {
					node=new PathNode(mapUnit.x,mapUnit.y,null,0);
					node.actionType= PathNode.ATTACK;
					nodeMap.setNode(mapUnit.x, mapUnit.y, node);
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
		if (!nodeMap.isInBounds(prevX,prevY)) return;
		PathNode prev=nodeMap.getNode(prevX,prevY);
		if (prev==null||prev.actionType!= PathNode.MOVE) return;
		if (!unit.canAttackFrom(enemyUnit, prevX, prevY)) return;
		int x=enemyUnit.x;
		int y=enemyUnit.y;

		PathNode curNode=nodeMap.getNode(x,y);

		PathNode node=new PathNode(x,y,prev,0);
		node.actionType= PathNode.ATTACK;

		if (curNode==null||node.totalCost<curNode.totalCost) {
			nodeMap.setNode(x,y,node);
		}
	}

}
