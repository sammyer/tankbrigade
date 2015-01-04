package com.frozen.tankbrigade.map.model;

import android.util.SparseArray;

import com.frozen.easyjson.JsonProperty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 08/02/14.
 */
public class GameConfig {
	@JsonProperty
	public List<TerrainType> terrainTypes;
	@JsonProperty
	public List<GameUnitType> unitTypes;

	private Map<String,GameUnitType> unitNameMap;
	private SparseArray<GameUnitType> unitSymbolMap;

	public GameUnitType getUnitTypeBySymbol(char typeSymbol) {
		if (unitSymbolMap==null) {
			unitSymbolMap=new SparseArray<GameUnitType>(unitTypes.size());
			for (GameUnitType unitType:unitTypes) {
				unitSymbolMap.put(unitType.symbol,unitType);
			}
		}
		return unitSymbolMap.get(typeSymbol);
	}
	public GameUnitType getUnitTypeByName(String name) {
		if (unitNameMap==null) {
			unitNameMap=new HashMap<String, GameUnitType>(unitTypes.size());
			for (GameUnitType unitType:unitTypes) {
				unitNameMap.put(unitType.name,unitType);
			}
		}
		return unitNameMap.get(name);
	}
}
