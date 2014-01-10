package com.frozen.tankbrigade.map;

/**
 * Created by sam on 02/01/14.
 */
public class TerrainMap {
    private int[][] map;
    private TerrainType[] terrains=TerrainType.getTerrainTypes();

    public TerrainMap(String fileContents) {
		parseContents(fileContents.split("\n"));
    }

	public TerrainMap(String[] fileContents) {
		parseContents(fileContents);
	}

    private void parseContents(String[] fileContents) {
        int h=fileContents.length;
        if (h==0) return;
        int w=fileContents[0].length();
		map=new int[w][h];
        for (int y=0;y<h;y++) {
            if (fileContents[y].length()<w) continue;
            for (int x=0;x<w;x++) {
                map[x][y]=terrainId(fileContents[y].charAt(x));
            }
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
}
