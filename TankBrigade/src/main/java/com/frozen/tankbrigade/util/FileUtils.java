package com.frozen.tankbrigade.util;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 05/01/14.
 */
public class FileUtils {
	public static JSONObject readJSONFile(Context context, String filename) {
		String contents=readFile(context,filename);
		if (contents==null) return null;
		try {
			JSONObject json=new JSONObject(contents);
			return json;
		} catch (JSONException e) {
			return null;
		}
	}

	public static String readFile(Context context, String filename) {
		String[] lines=readFileLines(context,filename);
		if (lines==null) return null;
		return TextUtils.join("\n",lines);
	}

	public static String[] readFileLines(Context context, String filename) {
		BufferedReader reader=null;
		try {
			reader = new BufferedReader(
					new InputStreamReader(context.getAssets().open(filename)));

			// do reading, usually loop until end of file reading
			List<String> lines=new ArrayList<String>();
			String mLine = reader.readLine();
			int numLines=0;
			while (mLine != null) {
				lines.add(mLine);
				mLine = reader.readLine();
				numLines++;
			}
			String[] lineArr=new String[numLines];
			return lines.toArray(lineArr);
		} catch (IOException e) {
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
	}
}
