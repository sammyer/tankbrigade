package com.frozen.tankbrigade.loaders;

import android.util.Log;
import android.util.SparseArray;

import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameConfig;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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



	public GameBoard parseMapFile(String fileContents,GameConfig config) {
		return parseMapFile(fileContents.split("\n"),config);
	}

	public GameBoard parseMapFile(String[] fileContents,GameConfig config) {
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
				parseUnits(lines,config);
				continue;
			}
			if (line.contains("Buildings(")) {
				parseBuildings(lines);
				continue;
			}
			lines.next();
			continue;
		}

		board.addPlayer(Player.USER_ID, 0);
		board.addPlayer(Player.AI_ID, 0);

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

	private void parseUnits(StringIterator lines,GameConfig config) {
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
			String unitName=line.substring(dashpos+1).trim().toLowerCase();
			GameUnit unit=new GameUnit(config.getUnitTypeByName(unitName),x,y,owner);
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


	//----------------------------------- SERIALIZE --------------------------------------

	public JSONObject serialize(GameBoard board) throws JSONException {
		JSONObject data=new JSONObject();

		JSONArray playersJson=new JSONArray();
		for (int i=0;i<board.getPlayers().size();i++) {
			Player player=board.getPlayers().valueAt(i);
			playersJson.put(serializePlayer(player));
		}
		data.put("players",playersJson);

		JSONArray unitsJson=new JSONArray();
		for (GameUnit unit:board.getUnits()) unitsJson.put(serializeUnit(unit));
		data.put("units",unitsJson);

		JSONArray buildingsJson=new JSONArray();
		for (Building building:board.getBuildings()) buildingsJson.put(serializeBuilding(building));
		data.put("buildings",buildingsJson);

		data.put("map",board.mapId);

		return data;
	}

	private JSONObject serializePlayer(Player player) throws JSONException {
		JSONObject playerJson=new JSONObject();
		playerJson.put("id",player.id);
		playerJson.put("money",player.money);
		return playerJson;
	}

	private JSONObject serializeUnit(GameUnit unit) throws JSONException {
		JSONObject unitJson=new JSONObject();
		unitJson.put("x",unit.x);
		unitJson.put("y",unit.y);
		unitJson.put("owner",unit.ownerId);
		unitJson.put("health",unit.health);
		unitJson.put("movesLeft",unit.movesLeft);
		unitJson.put("type",Character.toString(unit.type.symbol));
		return unitJson;
	}

	private JSONObject serializeBuilding(Building building) throws JSONException {
		JSONObject buildingJson=new JSONObject();
		buildingJson.put("x",building.x);
		buildingJson.put("y",building.y);
		boolean isCapturing=building.getIsCapturing();
		buildingJson.put("isCapturing",isCapturing);
		buildingJson.put("owner",building.getOldOwnerId());
		if (isCapturing) {
			buildingJson.put("occupier",building.getOccupyingOwnerId());
			buildingJson.put("turns",building.getCaptureTurns());
		}
		buildingJson.put("type",building.name);
		return buildingJson;
	}

	//----------------------------------- DESERIALIZE --------------------------------------

	public GameBoard deserialize(JSONObject data,  GameConfig config) throws JSONException {
		GameBoard board=new GameBoard();
		JSONArray arr;

		arr=data.getJSONArray("players");
		for (int i=0;i<arr.length();i++) {
			board.addPlayer(deserializePlayer((JSONObject)arr.get(i)));
		}

		arr=data.getJSONArray("units");
		for (int i=0;i<arr.length();i++) {
			board.addUnit(deserializeUnit((JSONObject) arr.get(i),config));
		}

		arr=data.getJSONArray("buildings");
		for (int i=0;i<arr.length();i++) {
			board.addBuilding(deserializeBuilding((JSONObject) arr.get(i)));
		}

		board.mapId=data.getString("map");

		return board;
	}

	private Player deserializePlayer(JSONObject obj) throws JSONException {
		int id=obj.getInt("id");
		int money=obj.getInt("money");
		Player player=new Player(id,money);
		return player;
	}

	private GameUnit deserializeUnit(JSONObject obj, GameConfig config) throws JSONException {
		int owner=obj.getInt("owner");
		int x=obj.getInt("x");
		int y=obj.getInt("y");
		char typeSymbol=obj.getString("type").charAt(0);
		GameUnitType type=config.getUnitTypeBySymbol(typeSymbol);
		GameUnit unit=new GameUnit(type,x,y,owner);
		unit.health=obj.getInt("health");
		unit.movesLeft=obj.getInt("movesLeft");
		return unit;
	}

	private Building deserializeBuilding(JSONObject obj) throws JSONException {
		int x=obj.getInt("x");
		int y=obj.getInt("y");
		String type=obj.getString("type");
		int owner=obj.getInt("owner");

		boolean isCapturing=obj.getBoolean("isCapturing");
		if (isCapturing) {
			int occupier=obj.getInt("occupier");
			int numTurns=obj.getInt("turns");
			return new Building(type,x,y,owner,occupier,numTurns);
		} else {
			return new Building(type,x,y,owner);
		}
	}

}
