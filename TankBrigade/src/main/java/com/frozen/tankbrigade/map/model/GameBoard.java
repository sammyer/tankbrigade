package com.frozen.tankbrigade.map.model;

import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 02/01/14.
 */
public class GameBoard {
    private TerrainMap terrainMap;
	private SparseArray<Player> players=new SparseArray<Player>();
	private List<GameUnit> units=new ArrayList<GameUnit>();
	private List<Building> buildings=new ArrayList<Building>();

	public GameBoard() {
	}

	public void setTerrainMap(TerrainMap terrainMap) {
		this.terrainMap=terrainMap;
	}

	public TerrainMap getTerrainMap() {
		return terrainMap;
	}

	public void addUnit(GameUnit unit) {
		units.add(unit);
	}

	public void addBuilding(Building building) {
		buildings.add(building);
	}

	public GameUnit getUnitAt(int x, int y) {
		for (GameUnit unit:units) {
			if (unit.x==x&&unit.y==y) return unit;
		}
		return null;
	}
	public Building getBuildingAt(int x, int y) {
		for (Building building:buildings) {
			if (building.x==x&&building.y==y) return building;
		}
		return null;
	}

	public void removeUnit(GameUnit unit) {
		synchronized (units) {
			units.remove(unit);
		}
	}

	public List<GameUnit> getUnits() {
		return units;
	}
	public List<Building> getBuildings() {return buildings;}

	//returns Player.NONE when there is no winner
	public int getWinner() {
		int winCandidate=Player.NONE;
		boolean hasCandidate=false;
		for (GameUnit unit:units) {
			if (!hasCandidate) {
				winCandidate=unit.ownerId;
				hasCandidate=true;
			} else if (unit.ownerId!=winCandidate) return Player.NONE;
		}
		for (Building building:buildings) {
			if (!building.isFactory()) continue;
			if (building.getOwnerId()==Player.NONE) continue;
			if (!hasCandidate) {
				winCandidate=building.getOwnerId();
				hasCandidate=true;
			} else if (!building.isOwnedBy(winCandidate)) return Player.NONE;
		}
		return winCandidate;
	}

	public void addPlayer(int id, int money) {
		Player player=new Player(id,money);
		players.put(player.id,player);
	}

	public SparseArray<Player> getPlayers() {
		return players;
	}

	public int width() {
		if (terrainMap==null) return 0;
		return terrainMap.width();
	}

	public int height() {
		if (terrainMap==null) return 0;
		return terrainMap.height();
	}

	public TerrainType getTerrain(int x, int y) {
		if (terrainMap==null) return null;
		return terrainMap.getTerrain(x,y);
	}

	public Player getPlayer(int playerId) {
		return players.get(playerId);
	}

	public boolean isInBounds(int x, int y) {
		return terrainMap.isInBounds(x,y);
	}

	public GameBoard clone() {
		GameBoard board=new GameBoard();
		board.terrainMap=terrainMap;
		for (GameUnit unit:units) {
			board.addUnit(unit.clone());
		}
		for (Building building:buildings) {
			board.addBuilding(building.clone());
		}
		return board;
	}
}
