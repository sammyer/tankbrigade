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
public class TerrainMap {
    private TerrainType[][] map;
	private List<GameUnit> units=new ArrayList<GameUnit>();

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
		units.clear();
		while (lines.hasNext()) {
			line=lines.get().trim();
			if (line.contains("Map(")) {
				parseTerrainMap(lines,config.terrainTypes);
				continue;
			}
			if (line.contains("Units(")) {
				parseUnits(lines,config.gameUnittypes);
				continue;
			}
			lines.next();
			continue;
		}
    }

	private void parseTerrainMap(StringIterator lines,List<TerrainType> terrainTypes) {
		String line=lines.get();
		String[] args=line.substring(line.indexOf("(")+1,line.indexOf(")")).split(",");

		int w=Integer.parseInt(args[0]);
		int h=Integer.parseInt(args[1]);
		map=new TerrainType[w][h];

		SparseArray<TerrainType> terrainMap=new SparseArray<TerrainType>(terrainTypes.size());
		for (TerrainType terrain:terrainTypes) {
			terrainMap.put(terrain.symbol,terrain);
		}

		int y=0;
		while (lines.hasNext()&&y<h) {
			line=lines.next().trim();
			if (line.startsWith(":")) break;
			if (line.length()<w) continue;
			for (int x=0;x<w;x++) {
				map[x][y]=terrainMap.get(line.charAt(x));
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
			Log.d("parse",line.substring(dashpos+1).trim().charAt(0)+" "+unitName+" "+unit.type);
			units.add(unit);
		}
	}

	public int width() {
		if (map==null||map.length==0) return 0;
		else return map[0].length;
	}

	public int height() {
		if (map==null) return 0;
		else return map.length;
	}

	public TerrainType getTerrain(int x, int y) {
		try {
			return map[x][y];
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}

	public GameUnit getUnitAt(int x, int y) {
		for (GameUnit unit:units) {
			if (unit.x==x&&unit.y==y) return unit;
		}
		return null;
	}

	public List<GameUnit> getUnits() {
		return units;
	}

	public boolean isInBounds(int x, int y) {
		if (x<0||y<0) return false;
		if (x>=width()) return false;
		if (y>=height()) return false;
		return true;

	}
}
