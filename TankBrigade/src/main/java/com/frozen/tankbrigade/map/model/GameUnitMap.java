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
public class GameUnitMap {
	private List<GameUnit> units=new ArrayList<GameUnit>();

	public void addUnit(GameUnit unit) {
		units.add(unit);
	}

	public GameUnit getUnitAt(int x, int y) {
		for (GameUnit unit:units) {
			if (unit.x==x&&unit.y==y) return unit;
		}
		return null;
	}

	public void removeUnit(GameUnit unit) {
		units.remove(unit);
	}

	public List<GameUnit> getUnits() {
		return units;
	}

	public GameUnitMap clone() {
		GameUnitMap unitMap=new GameUnitMap();
		for (GameUnit unit:units) {
			unitMap.addUnit(unit.clone());
		}
		return unitMap;
	}
}
