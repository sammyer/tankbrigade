package com.frozen.tankbrigade.loaders;

import android.content.Context;
import android.os.AsyncTask;

import com.frozen.easyjson.JSONParser;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.util.FileUtils;

import org.json.JSONObject;

/**
 * Created by sam on 01/12/14.
 */
public class MapLoader {
	public static interface ConfigLoadListener {
		public void onConfigLoaded(GameData config);
	}

	public static interface MapLoadListener {
		public void onMapLoaded(GameData config, GameBoard map);
	}

	public static interface MapSaveListener {
		public void onMapSaved();
	}

	public static void loadConfig(Context context, String filename, ConfigLoadListener listener) {
		new ConfigLoadTask(context,listener).execute(filename);
	}

	private static class ConfigLoadTask extends AsyncTask<String,Void,GameData> {
		private Context context;
		private ConfigLoadListener listener;

		private ConfigLoadTask(Context context, ConfigLoadListener listener) {
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected GameData doInBackground(String... strings) {
			String filename=strings[0];
			return loadConfig(context,filename);
		}

		protected static GameData loadConfig(Context context, String filename) {
			JSONObject configJson= FileUtils.readJSONFile(context, filename);
			return JSONParser.parse(configJson, GameData.class);
		}

		@Override
		protected void onPostExecute(GameData config) {
			if (listener!=null) listener.onConfigLoaded(config);
		}
	}

	//---------------------------------------------------------------------

	public static void loadMap(Context context, GameData config, String filename, MapLoadListener listener) {
		new MapLoadTask(context,config,listener).execute(filename);
	}
	public static void loadMap(Context context, GameData config, String configFile, String mapFile, MapLoadListener listener) {
		new MapLoadTask(context,config,configFile,listener).execute(mapFile);
	}

	private static class MapLoadTask extends AsyncTask<String,Void,GameBoard> {
		private Context context;
		private GameData config;
		private String configFile;
		private MapLoadListener listener;

		private MapLoadTask(Context context, GameData config, MapLoadListener listener) {
			this.context = context;
			this.config = config;
			this.listener = listener;
		}
		private MapLoadTask(Context context, GameData config, String configFile, MapLoadListener listener) {
			this.context = context;
			this.config = config;
			this.configFile = configFile;
			this.listener = listener;
		}

		@Override
		protected GameBoard doInBackground(String... strings) {
			String filename=strings[0];
			if (config==null) config=ConfigLoadTask.loadConfig(context,configFile);
			return loadMap(context,config,filename);
		}

		private GameBoard loadMap(Context context, GameData config, String mapFile) {
			String[] fileContents= FileUtils.readFileLines(context, mapFile);
			GameBoard boardModel =new GameBoard();
			boardModel.parseMapFile(fileContents, config);
			return boardModel;
		}

		@Override
		protected void onPostExecute(GameBoard map) {
			if (listener!=null) listener.onMapLoaded(config, map);
		}
	}


	public static void loadMapState(MapLoadListener listener) {

	}

	public static void saveMapState(GameBoard map, MapSaveListener listener) {

	}
}
