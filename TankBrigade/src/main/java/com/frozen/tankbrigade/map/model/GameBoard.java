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
    public TerrainMap terrainMap;
	public GameUnitMap gameUnits;
	public SparseArray<Player> players;



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

	public void parseMapFile(String fileContents,GameData config) {
		parseMapFile(fileContents.split("\n"),config);
	}

    public void parseMapFile(String[] fileContents,GameData config) {
		StringIterator lines=new StringIterator(fileContents);
		String line;
		gameUnits=new GameUnitMap();

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

		players=new SparseArray<Player>();
		addPlayer(Player.USER_ID,0);
		addPlayer(Player.AI_ID,0);
	}

	private void addPlayer(int id, int money) {
		Player player=new Player(id,money);
		players.put(player.id,player);
	}

	private void parseTerrainMap(StringIterator lines,List<TerrainType> terrainTypes) {
		String line=lines.get();
		String[] args=line.substring(line.indexOf("(")+1,line.indexOf(")")).split(",");

		int w=Integer.parseInt(args[0]);
		int h=Integer.parseInt(args[1]);
		terrainMap=new TerrainMap(w,h);

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
			Log.d("parse",unit.x+","+unit.y+" "+unitName+" "+unit.type.name+" "+unit.ownerId);
			gameUnits.addUnit(unit);
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
			gameUnits.addBuilding(building);
		}
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

	public GameUnit getUnitAt(int x, int y) {
		return gameUnits.getUnitAt(x,y);
	}
	public Building getBuildingAt(int x, int y) {
		return gameUnits.getBuildingAt(x,y);
	}

	public List<GameUnit> getUnits() {
		return gameUnits.getUnits();
	}
	public List<Building> getBuildings() {
		return gameUnits.getBuildings();
	}

	public Player getPlayer(int playerId) {
		return players.get(playerId);
	}

	public void addUnit(GameUnit unit) {
		gameUnits.addUnit(unit);
	}

	public boolean isInBounds(int x, int y) {
		return terrainMap.isInBounds(x,y);
	}

	public GameBoard clone() {
		GameBoard board=new GameBoard();
		board.terrainMap=terrainMap;
		board.gameUnits=gameUnits.clone();
		return board;
	}
}
