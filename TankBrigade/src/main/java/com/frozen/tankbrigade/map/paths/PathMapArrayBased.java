package com.frozen.tankbrigade.map.paths;

import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.util.GeomUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sam on 16/02/14.
 */
public class PathMapArrayBased implements IPathMap {
	private PathNode[][] nodeMap;
	private int w;
	private int h;

	@Override
	public void init(int w, int h) {
		if (nodeMap==null||nodeMap.length!=w||nodeMap[0].length!=h) {
			nodeMap=new PathNode[w][h];
		} else {
			for (int i=0;i<nodeMap.length;i++) Arrays.fill(nodeMap[i], null);
		}
		this.w=w;
		this.h=h;
	}

	@Override
	public void setNode(int x, int y, PathNode node) {
		nodeMap[x][y]=node;
	}

	@Override
	public PathNode getNode(int x, int y) {
		return nodeMap[x][y];
	}

	@Override
	public boolean isInBounds(int x, int y) {
		return GeomUtils.isInBounds(nodeMap,x,y);
	}

	@Override
	public int width() {
		return w;
	}

	@Override
	public int height() {
		return h;
	}

	@Override
	public List<PathNode> getAllNodes() {
		return null;
	}


}
