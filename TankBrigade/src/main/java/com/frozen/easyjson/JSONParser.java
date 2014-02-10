/**
 sammyer

 This is to replace the Jackson library. It should work about the same.

 */
package com.frozen.easyjson;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class JSONParser {
	private static final boolean DEBUG = true;
	private static final String TAG = "JSONParser";

	public static <T> T parse(JSONObject obj, Class<T> clazz) {
		return parseClass(obj, clazz);
	}

	private static <T> T parseClass(JSONObject json, Class<T> clazz) {
		if (DEBUG) Log.i(TAG, "parseClass "+clazz.getName());
		T obj;
		try {
			obj = clazz.newInstance();
		} catch (Exception e) {
			if (DEBUG) Log.e(TAG,"jpt err - "+e.toString());
			return null;
		}
		for (Field field:clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(JsonProperty.class)) {
				parseField(json,obj,field);
			}
		}
		return obj;
	}

	private static void parseField(JSONObject json, Object obj, Field field) {
		FieldStruct fieldData=new FieldStruct(field);
		if (fieldData.clazz==null) return;
		try {
			if (fieldData.type==FieldType.PRIMITIVE) parsePrimitiveField(json, obj, fieldData);
			else if (fieldData.type==FieldType.CLASS) parseClassField(json, obj, fieldData);
			else if (fieldData.type==FieldType.LIST) parseListField(json, obj, fieldData);
			else if (fieldData.type==FieldType.ARRAY) parseArrayField(json, obj, fieldData);
		} catch (JSONException e) {
			if (DEBUG) Log.e(TAG,"jpt err - "+e.toString());
		} catch (Exception e) {
			if (DEBUG) Log.e(TAG,"jpt err - "+e.toString());
		}
	}

	private static void parsePrimitiveField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parsePrimitiveField "+field.name);
		field.field.setAccessible(true);
		if (field.clazz.equals(int.class)) {
			field.field.set(obj, json.getInt(field.name));
		} else if (field.clazz.equals(char.class)) {
			String s=json.getString(field.name);
			if (s!=null&&s.length()>0) field.field.set(obj, s.charAt(0));
		} else if (field.clazz.equals(short.class)) {
			field.field.set(obj, (short)json.getInt(field.name));
		} else if (field.clazz.equals(long.class)) {
			field.field.set(obj, json.getLong(field.name));
		} else if (field.clazz.equals(float.class)) {
			field.field.set(obj, (float)json.getDouble(field.name));
		} else if (field.clazz.equals(double.class)) {
			field.field.set(obj, json.getDouble(field.name));
		} else if (field.clazz.equals(boolean.class)) {
			field.field.set(obj, json.getBoolean(field.name));
		} else {
			field.field.set(obj, json.get(field.name));
		}

	}

	private static void parseClassField(JSONObject json, Object obj, FieldStruct field)
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		if (DEBUG) Log.i(TAG, "parseClassField "+field.name);
		JSONObject childJson;
		childJson=json.getJSONObject(field.name);
		field.field.setAccessible(true);
		field.field.set(obj, parseClass(childJson,field.clazz));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parseListField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parseListField "+field.name);
		JSONArray jsonarr;
		jsonarr=json.getJSONArray(field.name);
		int arrlen=jsonarr.length();
		ArrayList list=new ArrayList();
		for (int i=0;i<arrlen;i++) {
			JSONObject jsonobj;
			jsonobj = (JSONObject)jsonarr.get(i);
			if (jsonobj==null) continue;
			list.add(parseClass(jsonobj,field.clazz));
		}
		field.field.setAccessible(true);
		field.field.set(obj, list);
	}

	private static void parseArrayField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parseArrayField "+field.name);
		JSONArray jsonarr;
		jsonarr=json.getJSONArray(field.name);

		int arrlen=jsonarr.length();
		Object arr=Array.newInstance(field.clazz, arrlen);
		for (int i=0;i<arrlen;i++) {
			if (field.clazz.equals(int.class)) {
				Array.set(arr, i, jsonarr.getInt(i));
			} else if (field.clazz.equals(char.class)) {
				String s=jsonarr.getString(i);
				if (s!=null&&s.length()>0) Array.set(arr, i, s.charAt(0));
			} else if (field.clazz.equals(short.class)) {
				Array.set(arr, i, (short)jsonarr.getInt(i));
			} else if (field.clazz.equals(long.class)) {
				Array.set(arr, i, jsonarr.getLong(i));
			} else if (field.clazz.equals(float.class)) {
				Array.set(arr, i, (float)jsonarr.getDouble(i));
			} else if (field.clazz.equals(double.class)) {
				Array.set(arr, i, jsonarr.getDouble(i));
			} else {
				Array.set(arr, i, jsonarr.get(i));
			}
		}
		field.field.setAccessible(true);
		field.field.set(obj, arr);
	}


	private enum FieldType {
		PRIMITIVE,LIST,ARRAY,CLASS
	}

	private static class FieldStruct {
		public Field field;
		public String name;
		public FieldType type;
		public Class<?> clazz;

		public FieldStruct(Field field) {
			this.field=field;
			name=field.getName();
			clazz=field.getType();
			if (clazz.isPrimitive()||clazz.equals(String.class)) {
				type=FieldType.PRIMITIVE;
			}
			else if (clazz.isArray()) {
				type=FieldType.ARRAY;
				clazz=clazz.getComponentType();
			} else if (clazz.isAssignableFrom(ArrayList.class)) {
				type=FieldType.LIST;
				clazz=clazz.getComponentType();
				try {
					ParameterizedType ptype=(ParameterizedType)field.getGenericType();
					clazz=(Class<?>) ptype.getActualTypeArguments()[0];
				} catch (Exception e) {
					clazz=null;
				}
			}
			else type=FieldType.CLASS;
		}
	}


}