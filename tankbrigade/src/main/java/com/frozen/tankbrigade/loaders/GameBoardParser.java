package com.frozen.tankbrigade.loaders;

import android.util.Log;
import android.util.SparseArray;

import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sam on 26/12/14.
 */
public class GameBoardParser {
	private GameBoard board;

	private static class StringIterator {
		private String[] strings;
		private int pos;

		private StringIterator(String[] strings) {
			this.strings = strings;
			pos=0;
		}
		public boolean hasNext() {
			return pos+1<strings.length;
		}

		public boolean hasCurrent() {
			return pos<strings.length;
		}

		public String next() {
			pos++;
			return strings[pos];
		}

		public String get() {
			return strings[pos];
		}
	}



	public GameBoard parseMapFile(String fileContents,GameData config) {
		return parseMapFile(fileContents.split("\n"),config);
	}

	public GameBoard parseMapFile(String[] fileContents,GameData config) {
		StringIterator lines=new StringIterator(fileContents);
		String line;
		board=new GameBoard();

		while (lines.hasNext()) {
			line=lines.get().trim();
			if (line.contains("Map(")) {
				parseTerrainMap(lines,config.terrainTypes);
				continue;
			}
			if (line.contains("Units(")) {
				parseUnits(lines,config.unitTypes);
				continue;
			}
			if (line.contains("Buildings(")) {
				parseBuildings(lines);
				continue;
			}
			lines.next();
			continue;
		}

		board.addPlayer(Player.USER_ID,0);
		board.addPlayer(Player.AI_ID,0);

		return board;
	}


	private void parseTerrainMap(StringIterator lines,List<TerrainType> terrainTypes) {
		String line=lines.get();
		String[] args=line.substring(line.indexOf("(")+1,line.indexOf(")")).split(",");

		int w=Integer.parseInt(args[0]);
		int h=Integer.parseInt(args[1]);
		TerrainMap terrainMap=new TerrainMap(w,h);

		SparseArray<TerrainType> typeMap=new SparseArray<TerrainType>(terrainTypes.size());
		for (TerrainType terrain:terrainTypes) {
			typeMap.put(terrain.symbol, terrain);
		}

		int y=0;
		while (lines.hasNext()&&y<h) {
			line=lines.next().trim();
			if (line.startsWith(":")) break;
			if (line.length()<w) continue;
			for (int x=0;x<w;x++) {
				terrainMap.setTerrain(x, y, typeMap.get(line.charAt(x)));
			}
			y++;
		}

		board.setTerrainMap(terrainMap);
	}

	private void parseUnits(StringIterator lines,List<GameUnitType> unitTypes) {
		String line=lines.get();
		String arg=line.substring(line.indexOf("(") + 1, line.indexOf(")"));

		int owner=Integer.parseInt(arg);
		Map<String,GameUnitType> unitMap=new HashMap<String, GameUnitType>(unitTypes.size());
		for (GameUnitType unitType:unitTypes) {
			unitMap.put(unitType.name,unitType);
		}

		while (lines.hasNext()) {
			line=lines.next().trim();
			if (line.startsWith(":")) break;
			if (line.length()==0) continue;
			int commapos=line.indexOf(',');
			int dashpos=line.indexOf('-');
			if (commapos<0||dashpos<0) continue;
			int x=Integer.parseInt(line.substring(0,commapos).trim());
			int y=Integer.parseInt(line.substring(commapos+1,dashpos).trim());
			String unitName=line.substring(dashpos+1).trim().toLowerCase();
			GameUnit unit=new GameUnit(unitMap.get(unitName),x,y,owner);
			Log.d("parse", unit.x + "," + unit.y + " " + unitName + " " + unit.type.name + " " + unit.ownerId);
			board.addUnit(unit);
		}
	}


	private void parseBuildings(StringIterator lines) {
		String line=lines.get();
		String arg=line.substring(line.indexOf("(") + 1, line.indexOf(")"));
		int owner=Integer.parseInt(arg);

		while (lines.hasNext()) {
			line=lines.next().trim();
			if (line.startsWith(":")) break;
			if (line.length()==0) continue;
			int commapos=line.indexOf(',');
			int dashpos=line.indexOf('-');
			if (commapos<0||dashpos<0) continue;
			int x=Integer.parseInt(line.substring(0,commapos).trim());
			int y=Integer.parseInt(line.substring(commapos+1,dashpos).trim());
			String buildingName;
			buildingName=line.substring(dashpos+1).trim().toLowerCase();
			Building building=new Building(buildingName,x,y,owner);
			board.addBuilding(building);
		}
	}

}
