package com.frozen.tankbrigade.map.paths;

import com.frozen.tankbrigade.map.model.GameUnit;

import java.util.List;

/**
 * Created by sam on 16/02/14.
 */
public interface IPathMap {
	public void init(int w, int h);
	public void setNode(int x, int y, PathNode node);
	public PathNode getNode(int x, int y);
	public boolean isInBounds(int x, int y);
	public int width();
	public int height();
	public List<PathNode> getAllNodes();
}
