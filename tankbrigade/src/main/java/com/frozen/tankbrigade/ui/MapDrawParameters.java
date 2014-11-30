package com.frozen.tankbrigade.ui;

import android.graphics.Point;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
import java.util.List;

/**
* Created by sam on 26/01/14.
*/
public class MapDrawParameters {

	public static final short SHADE_INVALID=0;
	public static final short SHADE_MOVE=1;
	public static final short SHADE_ATTACK=2;
	public static final short SHADE_SELECTED_UNIT=3;
	public static final short SHADE_NONE=-1;

	private SparseMap<UnitMove> moveMap;
	public Point selectedUnit;
	public Point[] selectedPath;
	public Point selectedAttack;
	private List<MapAnimation> animations=new ArrayList<MapAnimation>();
	public int testMode=0;

	public boolean hasMoves() {
		return moveMap!=null;
	}

	public boolean showPath() {
		return selectedPath!=null;
	}

	public boolean hasSelectedMove() {
		return selectedPath!=null||selectedAttack!=null;
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

	public void setMoveOverlay(Point selectedUnit, SparseMap<UnitMove> moveMap) {
		this.selectedUnit=selectedUnit;
		this.moveMap=moveMap;
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

	public int getOverlay(int x, int y) {
		if (moveMap==null) return SHADE_NONE;
		else if (hasSelectedMove()) return getSelectedPathOverlay(x,y);
		else return getMoveSelectionOverlay(x,y);
	}

	private int getMoveSelectionOverlay(int x, int y) {
		UnitMove move=moveMap.get(x,y);
		if (x==selectedUnit.x&&y==selectedUnit.y) return SHADE_SELECTED_UNIT;
		else if (move==null) return SHADE_INVALID;
		else if (move.isAttack()) return SHADE_ATTACK;
		else return SHADE_MOVE;
	}

	private int getSelectedPathOverlay(int x, int y) {
		if (x==selectedUnit.x&&y==selectedUnit.y) return SHADE_SELECTED_UNIT;
		else if (selectedAttack!=null&&selectedAttack.x==x&&selectedAttack.y==y) return SHADE_ATTACK;
		else if (selectedPath!=null) {
			for (Point point:selectedPath) {
				if (point.x==x&&point.y==y) return SHADE_MOVE;
			}
			return SHADE_INVALID;
		} else return SHADE_INVALID;
	}

	public MapDrawParameters clone() {
		MapDrawParameters dup=new MapDrawParameters();
		dup.moveMap=moveMap;
		dup.selectedPath=selectedPath;
		dup.selectedAttack=selectedAttack;
		dup.selectedUnit=selectedUnit;
		dup.animations=animations;
		dup.testMode=testMode;
		return dup;
	}
}
