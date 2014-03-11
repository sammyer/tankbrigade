package com.frozen.tankbrigade.map.paths;

import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sam on 09/03/14.
 */
public class AStar {
	private List<AStarNode> nodeSearchQueue=new ArrayList<AStarNode>();
	private SparseMap<AStarNode> nodeMap;

	private Comparator<AStarNode> costComparator=new Comparator<AStarNode>() {
		@Override
		public int compare(AStarNode node, AStarNode node2) {
			return node.totalCost-node2.totalCost;
		}
	};



	public SparseMap<AStarNode> findMoves(AStarMap map, int startX, int startY) {
		nodeMap=new SparseMap<AStarNode>(1000,1000);

		nodeSearchQueue.clear();
		AStarNode node=new AStarNode(startX,startY,null,0);
		nodeMap.set(startX,startY,node);
		nodeSearchQueue.add(node);

		while (nodeSearchQueue.size()>0) {
			node=nodeSearchQueue.remove(0);
			checkNode(map, node.x + 1, node.y, node);
			checkNode(map,node.x-1,node.y,node);
			checkNode(map,node.x,node.y+1,node);
			checkNode(map,node.x,node.y-1,node);
		}

		return nodeMap;
	}


	private void checkNode(AStarMap map, int x, int y, AStarNode prev) {
		if (!map.canMoveHere(x,y)) return;

		int cost=map.getCost(x,y);
		int totalCost=prev.totalCost+cost;
		if (totalCost>map.getMaxCost()) return;

		AStarNode node=nodeMap.get(x,y);
		if (node!=null&&node.totalCost<=totalCost) return;

		node=new AStarNode(x,y,prev,cost);
		nodeMap.set(x,y,node);

		//dont bother adding to queue if cost==movement or if this is an enemy square
		if (totalCost!=map.getMaxCost()) {
			int queuePos= Collections.binarySearch(nodeSearchQueue, node, costComparator);
			if (queuePos<0) queuePos=-queuePos-1; //see doc for binarysearch
			nodeSearchQueue.add(queuePos,node);
		}
	}

}
