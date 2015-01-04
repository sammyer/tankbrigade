package com.frozen.tankbrigade.loaders;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.frozen.easyjson.JSONParser;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameConfig;
import com.frozen.tankbrigade.util.FileUtils;

import org.json.JSONObject;

import java.io.File;

/**
 * Created by sam on 01/12/14.
 */
public class MapLoader {
	private static final String TAG="MapLoader";

	private static GameConfig cachedConfig;
	private static final String defConfigFile ="gameconfig.json";
	private static final String defGameFile="savedgame.json";

	public static interface ConfigLoadListener {
		public void onConfigLoaded(GameConfig config);
	}

	public static interface MapLoadListener {
		public void onMapLoaded(GameConfig config, GameBoard map);
	}

	public static interface MapSaveListener {
		public void onMapSaved();
	}

	public static void loadConfig(Context context, String filename, ConfigLoadListener listener) {
		new ConfigLoadTask(context,listener).execute(filename);
	}

	private static class ConfigLoadTask extends AsyncTask<String,Void,GameConfig> {
		private Context context;
		private ConfigLoadListener listener;

		private ConfigLoadTask(Context context, ConfigLoadListener listener) {
			this.context = context;
			this.listener = listener;
		}

		@Override
		protected GameConfig doInBackground(String... strings) {
			String filename=strings[0];
			return loadConfig(context,filename);
		}

		protected static GameConfig loadConfig(Context context, String filename) {
			JSONObject configJson= FileUtils.readJSONFileFromAssets(context, filename);
			return JSONParser.parse(configJson, GameConfig.class);
		}

		@Override
		protected void onPostExecute(GameConfig config) {
			if (listener!=null) listener.onConfigLoaded(config);
		}
	}

	//---------------------------------------------------------------------


	public static void loadMap(Context context, String mapFile, MapLoadListener listener) {
		Log.i(TAG, "loadMap");
		MapLoadTask task=new MapLoadTask(context,cachedConfig,false,listener);
		task.setConfigFile(defConfigFile);
		task.execute(mapFile);
	}

	private static class MapLoadTask extends AsyncTask<String,Void,GameBoard> {
		private Context context;
		private GameConfig config;
		private String configFile;
		private JSONObject gameData;
		private boolean loadGameData=false;
		private MapLoadListener listener;
		private GameBoardParser parser=new GameBoardParser();

		private MapLoadTask(Context context, GameConfig config, boolean isSavedGame, MapLoadListener listener) {
			this.context = context;
			this.config = config;
			this.loadGameData=isSavedGame;
			this.listener = listener;
		}

		public void setConfigFile(String configFile) {
			this.configFile=configFile;
		}
		public void setGameData(JSONObject gameData) {
			this.gameData=gameData;
		}

		@Override
		protected GameBoard doInBackground(String... strings) {
			String filename=strings[0];

			if (config==null) {
				Log.d(TAG,"Loading config - "+configFile);
				config=ConfigLoadTask.loadConfig(context,configFile);
			}
			if (loadGameData) {
				Log.d(TAG,"Loading game data");
				try {
					Log.d(TAG,"Loading game - reading file - "+filename);
					String gameDataStr=FileUtils.readFile(context,filename,false);
					gameData=new JSONObject(gameDataStr);
					Log.d(TAG,"Loading game - parsing json - "+gameData);
					GameBoard board=parser.deserialize(gameData,config);
					Log.d(TAG,"Loading game - loading map - "+board.mapId);
					GameBoard map=loadMap(context,config,board.mapId);
					board.setTerrainMap(map.getTerrainMap());
					Log.i(TAG,"Loading game - done");
					return board;
				} catch (Exception e) {
					Log.w(TAG,"Could not load saved game - "+e.toString());
					e.printStackTrace();
					return null;
				}
			} else {
				Log.d(TAG,"Loading map");
				GameBoard board=loadMap(context,config,filename);
				Log.i(TAG,"Loading map - done");
				return board;
			}
		}

		private GameBoard loadMap(Context context, GameConfig config, String mapFile) {
			String[] fileContents= FileUtils.readFileLines(context, mapFile,true);
			GameBoard board=parser.parseMapFile(fileContents, config);
			board.mapId=mapFile;
			return board;
		}

		@Override
		protected void onPostExecute(GameBoard map) {
			if (listener!=null) listener.onMapLoaded(config, map);
		}
	}


	public static void restoreGame(Context context, MapLoadListener listener) {
		restoreGame(context, getSavedGameFile(context).toString(), listener);
	}

	public static void restoreGame(Context context, String savedGameFile, MapLoadListener listener) {
		Log.i(TAG,"restoreGame");
		MapLoadTask task=new MapLoadTask(context,cachedConfig,true,listener);
		task.setConfigFile(defConfigFile);
		task.execute(savedGameFile);
	}

	public static void saveGame(Context context, GameBoard map, MapSaveListener listener) {
		new MapSaveTask(getSavedGameFile(context),listener).execute(map);
	}

	public static File getSavedGameFile(Context context) {
		return FileUtils.makeFile(context,defGameFile);
	}

	public static boolean hasSavedGame(Context context) {
		return getSavedGameFile(context).exists();
	}

	private static class MapSaveTask extends AsyncTask<GameBoard,Void,Boolean> {
		private File file;
		private MapSaveListener listener;

		private MapSaveTask(File file, MapSaveListener listener) {
			this.file = file;
			this.listener = listener;
		}

		@Override
		protected Boolean doInBackground(GameBoard... gameBoards) {
			GameBoard board=gameBoards[0];
			Log.d(TAG,"Saving game ... "+file.toString());
			GameBoardParser parser=new GameBoardParser();
			try {
				Log.d(TAG,"Saving - serializing json");
				JSONObject json=parser.serialize(board);
				Log.d(TAG,"Saving - writing to file");
				FileUtils.writeFile(file,json.toString());
				Log.i(TAG,"Saving - done");
				return true;
			} catch (Exception e) {
				Log.e(TAG,"Could not write saved game - "+e.toString());
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean aBoolean) {
			if (listener!=null&&aBoolean) listener.onMapSaved();
		}
	}
}
