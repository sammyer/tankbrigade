package com.frozen.tankbrigade.map.model;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 03/03/14.
 */
public class TerrainMap {
	private TerrainType[][] map;
	private int w;
	private int h;

	public TerrainMap(int w, int h) {
		this.w=w;
		this.h=h;
		map=new TerrainType[w][h];
	}

	public int width() {
		return w;
	}

	public int height() {
		return h;
	}

	public TerrainType getTerrain(int x, int y) {
		if (!isInBounds(x,y)) return null;
		return map[x][y];
	}

	//counts bridge as low level
	public int getTerrainLevel(int x, int y) {
		TerrainType terrain=getTerrain(x,y);
		if (terrain==null) return -1;
		else return terrain.getLevel();
	}
	//counts bridge as higher level
	public int getTerrainLevel2(int x, int y) {
		TerrainType terrain=getTerrain(x,y);
		if (terrain==null) return -1;
		else if (terrain.symbol==TerrainType.BRIDGE) return 0;
		else return terrain.getLevel();
	}

	public void setTerrain(int x, int y, TerrainType terrain) {
		map[x][y]=terrain;
	}

	public boolean isInBounds(int x, int y) {
		return (x>=0&&x<w&&y>=0&&y<h);
	}
}
