package com.frozen.tankbrigade.map.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 03/03/14.
 */
public class GameUnitMap {
	private List<GameUnit> units=new ArrayList<GameUnit>();
	private List<Building> buildings=new ArrayList<Building>();

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
		units.remove(unit);
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

	public GameUnitMap clone() {
		GameUnitMap unitMap=new GameUnitMap();
		for (GameUnit unit:units) {
			unitMap.addUnit(unit.clone());
		}
		for (Building building:buildings) {
			unitMap.addBuilding(building.clone());
		}
		return unitMap;
	}
}
