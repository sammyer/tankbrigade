package com.frozen.tankbrigade.map.model;

import com.frozen.easyjson.JsonProperty;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam on 02/01/14.
 */
public class TerrainType {
    public static char PLAIN='p';
    public static char HILL='h';
    public static char FOREST='f';
    public static char MOUNTAIN='m';
    public static char ROAD='r';
    public static char BRIDGE='b';
    public static char WATER='w';
    public static char BEACH='b';

	@JsonProperty
	public String name;
	@JsonProperty
	public char symbol;
	@JsonProperty
    public float defense;
	@JsonProperty
	public int movement;

	public TerrainType() {
	}

	public boolean isWater() {
		return (symbol==WATER||symbol==BEACH||symbol==BRIDGE);
	}

	public boolean isLand() {
		return symbol!=WATER;
	}
}
