package com.frozen.tankbrigade.map;

import android.graphics.Point;

import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.IPathMap;
import com.frozen.tankbrigade.map.paths.PathNode;

import java.util.ArrayList;
import java.util.List;

/**
* Created by sam on 26/01/14.
*/
public class MapDrawParameters {

	public static final int SHADE_INVALID=0;
	public static final int SHADE_MOVE=1;
	public static final int SHADE_ATTACK=2;
	public static final int SHADE_SELECTED_UNIT=3;

	public short[][] mapOverlay;
	public Point[] selectedPath;
	public Point selectedAttack;
	private List<MapAnimation> animations=new ArrayList<MapAnimation>();

	public boolean showMoves() {
		return mapOverlay!=null;
	}

	public boolean showPath() {
		return selectedPath!=null;
	}

	public Point[] getSelectedPath() {
		return selectedPath;
	}

	//select mode

	public void setSelectedPath(Point[] path) {
		setSelectedPath(path,null);
	}
	public void setSelectedPath(Point[] path, Point attack) {
		this.selectedPath=path;
		this.selectedAttack=attack;
	}
	public void clearSelectedPath() {
		setSelectedPath(null);
	}

	public void setMapOverlay(short[][] mapOverlay) {
		this.mapOverlay=mapOverlay;
		clearSelectedPath();
	}


	public void addAnimation(MapAnimation animation) {
		animations.add(animation);
	}

	public List<MapAnimation> getAnimations() {
		return animations;
	}

	public boolean isAnimating() {
		if (animations==null) return false;
		return (animations.size()>0);
	}

	public static void setMapOverlayFromPaths(GameUnit unit, IPathMap paths, short[][] destination) {
		//if (allocatedArray==null) allocatedArray=new short[paths.length][paths[0].length];
		for (int x=0;x<paths.width();x++) {
			for (int y=0;y<paths.height();y++) {
				short shadeId;
				if (x==unit.x&&y==unit.y) shadeId=SHADE_SELECTED_UNIT;
				PathNode move=paths.getNode(x,y);
				if (move==null) shadeId=SHADE_INVALID;
				else if (move.actionType== PathNode.MOVE) shadeId=SHADE_MOVE;
				else if (move.actionType== PathNode.ATTACK) shadeId=SHADE_ATTACK;
				else shadeId=SHADE_INVALID;
				destination[x][y]=shadeId;
			}
		}
	}

	public MapDrawParameters clone() {
		MapDrawParameters dup=new MapDrawParameters();
		dup.mapOverlay=mapOverlay;
		dup.selectedPath=selectedPath;
		dup.selectedAttack=selectedAttack;
		dup.animations=animations;
		return dup;
	}
}
