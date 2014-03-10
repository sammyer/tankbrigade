package com.frozen.tankbrigade.map.model;

import android.content.Context;

import com.frozen.easyjson.JsonProperty;
import com.frozen.tankbrigade.util.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 08/02/14.
 */
public class GameData {
	@JsonProperty
	public List<TerrainType> terrainTypes;
	@JsonProperty
	public List<GameUnitType> unitTypes;
}
