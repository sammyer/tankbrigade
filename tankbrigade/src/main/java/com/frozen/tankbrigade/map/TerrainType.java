package com.frozen.tankbrigade.map;

/**
 * Created by sam on 02/01/14.
 */
public class TerrainType {
    public static int PLAIN=0;
    public static int HILL=1;
    public static int FOREST=2;
    public static int MOUNTAIN=3;
    public static int ROAD=4;
    public static int BRIDGE=5;
    public static int WATER=6;
    public static int BEACH=7;

    public int id;
    public float defense;

	public TerrainType(int id, float defense) {
		this.id = id;
		this.defense = defense;
	}

	public static TerrainType[] getTerrainTypes() {
		TerrainType[] terrains=new TerrainType[8];
		for (int i=0;i<8;i++) terrains[i]=new TerrainType(i,0);
		return terrains;
	}
}
