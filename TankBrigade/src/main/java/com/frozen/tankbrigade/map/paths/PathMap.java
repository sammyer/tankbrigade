package com.frozen.tankbrigade.map.paths;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 02/03/14.
 */
public class PathMap implements IPathMap {
	private int w;
	private int h;
	private SparseArray<PathNode> nodes=new SparseArray<PathNode>();

	@Override
	public void init(int w, int h) {
		this.w=w;
		this.h=h;
		nodes.clear();
	}

	@Override
	public void setNode(int x, int y, PathNode node) {
		nodes.put(y*w+x,node);
	}

	@Override
	public PathNode getNode(int x, int y) {
		return nodes.get(y*w+x);
	}

	@Override
	public boolean isInBounds(int x, int y) {
		return x>=0&&x<w&&y>=0&&y<h;
	}

	@Override
	public int width() {
		return w;
	}

	@Override
	public int height() {
		return h;
	}

	public List<PathNode> getAllNodes() {
		List<PathNode> nodeList=new ArrayList<PathNode>(nodes.size());
		for (int i=0;i<nodes.size();i++) {
			nodeList.add(nodes.valueAt(i));
		}
		return nodeList;
	}
}
