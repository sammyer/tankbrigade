package com.frozen.tankbrigade.ui;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;

/**
 * Created by sam on 29/11/14.
 */
public class DrawableMapping {
	public static int getUnitDrawable(GameUnitType unitType) {
		switch (unitType.symbol) {
			case GameUnitType.COMMANDO: return R.drawable.commando;
			case GameUnitType.BAZOOKA: return R.drawable.bazooka;
			case GameUnitType.FLAK: return R.drawable.flak;
			case GameUnitType.TANK: return R.drawable.tank;
			case GameUnitType.ROCKET: return R.drawable.rocket;
			case GameUnitType.AIRPLANE: return R.drawable.fighter;
			case GameUnitType.GOLIATH: return R.drawable.bigtank;
			case GameUnitType.MORTAR: return R.drawable.mortar;
			case GameUnitType.BOMBER: return R.drawable.bomber;
		}
		//default
		return R.drawable.commando;
	}

	public static int getTerrainDrawable(TerrainMap map, int x, int y) {
		TerrainType terrain=map.getTerrain(x,y);
		if (terrain.symbol== TerrainType.PLAIN||terrain.symbol==TerrainType.HILL) {
			return R.drawable.plains;
		} else if (terrain.symbol==TerrainType.FOREST) {
			return R.drawable.forest;
		} else if (terrain.symbol==TerrainType.MOUNTAIN) {
			return R.drawable.mountain;
		} else if (terrain.symbol==TerrainType.WATER||terrain.symbol==TerrainType.BRIDGE) {
			return R.drawable.water;
		} else if (terrain.symbol==TerrainType.ROCKY_WATER) {
			return R.drawable.rocky;
		} else if (terrain.symbol==TerrainType.BEACH) {
			return R.drawable.beach;
		} else if (terrain.symbol==TerrainType.ROAD) {
			return getRoadTile(map,x,y);
		} else return 0;
	}
	
	public static int getBridgeDrawable(TerrainMap map, int x, int y) {
		if (isRoad(map,x-1,y)||isRoad(map,x+1,y)) {
			return R.drawable.bridge_ew;
		}
		else return R.drawable.bridge_ns;
	}

	private static final int[] roadTiles={
			R.drawable.road_ew,R.drawable.road_ew,
			R.drawable.road_ew,R.drawable.road_ew,
			R.drawable.road_ns,R.drawable.road_sw,
			R.drawable.road_se,R.drawable.road_sew,
			R.drawable.road_ns,R.drawable.road_nw,
			R.drawable.road_ne,R.drawable.road_new,
			R.drawable.road_ns,R.drawable.road_nsw,
			R.drawable.road_nse,R.drawable.road_nsew
	};
	private static int getRoadTile(TerrainMap map, int x, int y) {
		int n=isRoad(map,x,y-1)?1:0;
		int s=isRoad(map,x,y+1)?1:0;
		int e=isRoad(map,x+1,y)?1:0;
		int w=isRoad(map,x-1,y)?1:0;
		int idx=n*8+s*4+e*2+w;
		return roadTiles[idx];
	}

	private static boolean isRoad(TerrainMap map, int x, int y) {
		if (!map.isInBounds(x,y)) return false;
		else return map.getTerrain(x,y).isRoad();
	}

	
}
