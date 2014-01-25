package com.frozen.tankbrigade.map;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sam on 02/01/14.
 */
public class TerrainMap {
    private int[][] map;
    private TerrainType[] terrains=TerrainType.getTerrainTypes();
	private GameUnitType[] unitTypes=GameUnitType.getUnitTypes();
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

	public void parseMapFile(String fileContents) {
		parseMapFile(fileContents.split("\n"));
	}

    public void parseMapFile(String[] fileContents) {
		StringIterator lines=new StringIterator(fileContents);
		String line;
		units.clear();
		while (lines.hasNext()) {
			line=lines.get().trim();
			if (line.contains("Map(")) {
				parseTerrainMap(lines);
				continue;
			}
			if (line.contains("Units(")) {
				parseUnits(lines);
				continue;
			}
			lines.next();
			continue;
		}
    }

	private void parseTerrainMap(StringIterator lines) {
		String line=lines.get();
		String[] args=line.substring(line.indexOf("(")+1,line.indexOf(")")).split(",");

		int w=Integer.parseInt(args[0]);
		int h=Integer.parseInt(args[1]);
		map=new int[w][h];

		int y=0;
		while (lines.hasNext()&&y<h) {
			line=lines.next().trim();
			if (line.startsWith(":")) break;
			if (line.length()<w) continue;
			for (int x=0;x<w;x++) {
				map[x][y]=terrainId(line.charAt(x));
			}
			y++;
		}
	}

	private void parseUnits(StringIterator lines) {
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
			int unitId=getUnitId(line.substring(dashpos+1).trim().toLowerCase().charAt(0));
			GameUnit unit=new GameUnit(unitTypes[unitId],x,y,owner);
			Log.d("parse",line.substring(dashpos+1).trim().charAt(0)+" "+unitId+" "+unit.type);
			units.add(unit);
		}
	}

    private int terrainId(char c) {
        if (c=='p') return TerrainType.PLAIN;
        if (c=='h') return TerrainType.HILL;
        if (c=='b') return TerrainType.BEACH;
        if (c=='g') return TerrainType.BRIDGE;
        if (c=='f') return TerrainType.FOREST;
        if (c=='m') return TerrainType.MOUNTAIN;
        if (c=='r') return TerrainType.ROAD;
        if (c=='w') return TerrainType.WATER;
        return 0;
    }

	private int getUnitId(char c) {
		if (c=='a') return GameUnitType.AIRPLANE;
		if (c=='b') return GameUnitType.BAZOOKA;
		if (c=='c') return GameUnitType.COMMANDO;
		if (c=='f') return GameUnitType.FLAK;
		if (c=='r') return GameUnitType.ROCKET;
		if (c=='t') return GameUnitType.TANK;
		return 0;
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
			int terrainId=map[x][y];
			return terrains[terrainId];
		} catch (Exception e) {
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
