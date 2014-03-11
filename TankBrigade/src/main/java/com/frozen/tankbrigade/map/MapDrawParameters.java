package com.frozen.tankbrigade.map;

import android.graphics.Point;

import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* Created by sam on 26/01/14.
*/
public class MapDrawParameters {

	public static final short SHADE_INVALID=0;
	public static final short SHADE_MOVE=1;
	public static final short SHADE_ATTACK=2;
	public static final short SHADE_SELECTED_UNIT=3;

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

	public static void setMapOverlayFromPaths(GameUnit unit, SparseMap<UnitMove> moveMap, short[][] destination) {
		//if (allocatedArray==null) allocatedArray=new short[paths.length][paths[0].length];
		for (int x=0;x<destination.length;x++) {
			Arrays.fill(destination[x],SHADE_INVALID);
		}

		for (UnitMove move:moveMap.getAllNodes()) {
			if (move.isAttack()) destination[move.x][move.y]=SHADE_ATTACK;
			else destination[move.x][move.y]=SHADE_MOVE;
		}
		destination[unit.x][unit.y]=SHADE_SELECTED_UNIT;
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
