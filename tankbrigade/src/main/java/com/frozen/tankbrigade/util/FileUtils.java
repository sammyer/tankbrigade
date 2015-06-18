package com.frozen.tankbrigade.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 05/01/14.
 */
public class FileUtils {

	public static JSONObject readJSONFileFromAssets(Context context, String filename) {
		String contents= readFile(context, filename, true);
		if (contents==null) return null;
		try {
			JSONObject json=new JSONObject(contents);
			return json;
		} catch (JSONException e) {
			return null;
		}
	}

	public static String readFile(Context context, String filename, boolean asInputStream) {
		String[] lines=readFileLines(context,filename,asInputStream);
		if (lines==null) return null;
		return TextUtils.join("\n",lines);
	}

	public static String[] readFileLines(Context context, String filename, boolean asInputStream) {
		BufferedReader reader=null;
		try {
			if (asInputStream) {
				reader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
			} else {
				reader = new BufferedReader(new FileReader(new File(filename)));
			}

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
			Log.e("Exception","Error reading lines - "+e.toString());
			return null;
		} finally {
			try {
				reader.close();
			} catch (Exception e) {}
		}
	}

	public static File makeFile(Context context, String filename) {
		File file;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			file = new File(Environment.getExternalStorageDirectory(), filename);
		} else {
			file = new File(context.getFilesDir(), filename);
		}
		return file;
	}

	public static void writeFile(File file, String data) throws IOException {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					new FileOutputStream(file));
			outputStreamWriter.write(data);
			outputStreamWriter.close();
		}
		catch (IOException e) {
			Log.e("Exception", "File write failed: " + e.toString());
		}
	}
}
