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
			logException(e);
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
			else if (fieldData.type==FieldType.JSON) parseJsonField(json, obj, fieldData);
		} catch (JSONException e) {
			logException(e);
		} catch (Exception e) {
			logException(e);
		}
	}

	private static void logException(Exception e) {
		if (DEBUG) Log.e(TAG,"jpt errx - "+e.toString());
		e.printStackTrace();
	}

	private static void parsePrimitiveField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parsePrimitiveField "+field.name);
		field.field.setAccessible(true);
		if (field.clazz.equals(int.class)) {
			field.field.set(obj, json.getInt(field.name));
		} else if (field.clazz.equals(long.class)) {
			field.field.set(obj, json.getLong(field.name));
		} else if (field.clazz.equals(float.class)) {
			field.field.set(obj, (float)json.getDouble(field.name));
		} else if (field.clazz.equals(double.class)) {
			field.field.set(obj, json.getDouble(field.name));
		} else if (field.clazz.equals(boolean.class)) {
			field.field.set(obj, json.getBoolean(field.name));
		} else if (field.clazz.equals(short.class)) {
			field.field.set(obj, (short)json.getInt(field.name));
		} else if (field.clazz.equals(char.class)) {
			String s=json.getString(field.name);
			if (s!=null&s.length()>0) field.field.set(obj, s.charAt(0));
		} else if (field.clazz.equals(byte.class)) {
			field.field.set(obj, (byte)json.getInt(field.name));
		} else if (field.clazz.equals(String.class)) {
			field.field.set(obj, json.getString(field.name));
		}

		else {
			field.field.set(obj, parseBoxedPrimitive(json, field.name, field.clazz));
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

	private static void parseJsonField(JSONObject json, Object obj, FieldStruct field)
			throws JSONException, IllegalArgumentException, IllegalAccessException {
		if (DEBUG) Log.i(TAG, "parseClassField "+field.name);
		JSONObject childJson;
		childJson=json.getJSONObject(field.name);
		field.field.setAccessible(true);
		field.field.set(obj, childJson);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void parseListField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parseListField "+field.name);
		JSONArray jsonarr;
		jsonarr=json.getJSONArray(field.name);
		int arrlen=jsonarr.length();
		ArrayList list=new ArrayList();

		boolean isBoxedPrimitive=FieldStruct.isPrimitive(field.clazz);
		for (int i=0;i<arrlen;i++) {
			if (isBoxedPrimitive) {
				list.add(parseBoxedPrimitive(jsonarr, i, field.clazz));
			}
			else {
				JSONObject jsonobj;
				jsonobj = (JSONObject)jsonarr.get(i);
				if (jsonobj==null) continue;
				list.add(parseClass(jsonobj,field.clazz));
			}
		}
		field.field.setAccessible(true);
		field.field.set(obj, list);
	}


	private static Object parseBoxedPrimitive(JSONArray jsonarr, int i, Class<?> clazz) throws JSONException {
		if (clazz.equals(String.class)) {
			return jsonarr.getString(i);
		} if (clazz.equals(Integer.class)) {
			return Integer.valueOf(jsonarr.getInt(i));
		} else if (clazz.equals(Float.class)) {
			return Float.valueOf((float)jsonarr.getDouble(i));
		} else if (clazz.equals(Double.class)) {
			return Double.valueOf(jsonarr.getDouble(i));
		} else if (clazz.equals(Long.class)) {
			return Long.valueOf(jsonarr.getLong(i));
		} else if (clazz.equals(Short.class)) {
			return Short.valueOf((short)jsonarr.getInt(i));
		} else if (clazz.equals(Byte.class)) {
			return Byte.valueOf((byte)jsonarr.getInt(i));
		} else if (clazz.equals(Character.class)) {
			String s=jsonarr.getString(i);
			if (s!=null&s.length()>0) {
				return Character.valueOf(s.charAt(0));
			}
		} else if (clazz.equals(Boolean.class)) {
			return Boolean.valueOf(jsonarr.getBoolean(i));
		}
		throw new JSONException("parseBoxedPrimitive :: type not handled");
	}

	private static Object parseBoxedPrimitive(JSONObject json, String fieldName, Class<?> clazz) throws JSONException {
		if (clazz.equals(String.class)) {
			return json.get(fieldName);
		} else if (clazz.equals(Integer.class)) {
			return Integer.valueOf(json.getInt(fieldName));
		} else if (clazz.equals(Long.class)) {
			return Long.valueOf(json.getLong(fieldName));
		} else if (clazz.equals(Float.class)) {
			return Float.valueOf((float)json.getDouble(fieldName));
		} else if (clazz.equals(Double.class)) {
			return Double.valueOf(json.getDouble(fieldName));
		} else if (clazz.equals(Boolean.class)) {
			return Boolean.valueOf(json.getBoolean(fieldName));
		} else if (clazz.equals(Short.class)) {
			return Short.valueOf((short)json.getInt(fieldName));
		} else if (clazz.equals(Character.class)) {
			String s=json.getString(fieldName);
			if (s!=null&s.length()>0) return Character.valueOf(s.charAt(0));
		} else if (clazz.equals(Byte.class)) {
			return Byte.valueOf((byte)json.getInt(fieldName));
		}
		throw new JSONException("parseBoxedPrimitive :: type not handled");
	}


	private static void parseArrayField(JSONObject json, Object obj, FieldStruct field)
			throws IllegalArgumentException, IllegalAccessException, JSONException {
		if (DEBUG) Log.i(TAG, "parseArrayField "+field.name+" cl="+field.clazz);
		JSONArray jsonarr;
		jsonarr=json.getJSONArray(field.name);
		field.field.setAccessible(true);

		int arrlen=jsonarr.length();
		Object arr=Array.newInstance(field.clazz, arrlen);
		for (int i=0;i<arrlen;i++) {
			parseArrayFieldAux(arr, jsonarr, i, field.clazz);
		}


		field.field.set(obj, arr);
	}

	private static void parseArrayFieldAux(Object arr, JSONArray jsonarr, int i, Class<?> clazz)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException, JSONException {
		if (clazz.equals(int.class)) {
			Array.setInt(arr, i, jsonarr.getInt(i));
		} else if (clazz.equals(float.class)) {
			Array.setFloat(arr, i, (float)jsonarr.getDouble(i));
		} else if (clazz.equals(double.class)) {
			Array.setDouble(arr, i, jsonarr.getDouble(i));
		} else if (clazz.equals(long.class)) {
			Array.setLong(arr, i, jsonarr.getLong(i));
		} else if (clazz.equals(short.class)) {
			Array.setShort(arr, i, (short)jsonarr.getInt(i));
		} else if (clazz.equals(byte.class)) {
			Array.setByte(arr, i, (byte)jsonarr.getInt(i));
		} else if (clazz.equals(char.class)) {
			String s=jsonarr.getString(i);
			if (s!=null&s.length()>0) Array.setChar(arr, i, s.charAt(0));
		} else if (clazz.equals(boolean.class)) {
			Array.setBoolean(arr, i, jsonarr.getBoolean(i));
		} else if (clazz.equals(String.class)) {
			Array.set(arr, i, jsonarr.getString(i));
		}

		else if (clazz.isArray()) {
//multidimensional arrays
			JSONArray subJsonArr=jsonarr.getJSONArray(i);
			Class<?> subclazz=clazz.getComponentType();
			Object subarr=Array.newInstance(subclazz, subJsonArr.length());
			for (int j=0;j<subJsonArr.length();j++) {
				parseArrayFieldAux(subarr, subJsonArr, j, subclazz);
			}
			Array.set(arr, i, subarr);
		} else if (FieldStruct.isPrimitive(clazz)) {
			Array.set(arr, i, parseBoxedPrimitive(jsonarr, i, clazz));
		} else {
			JSONObject obj=(JSONObject)jsonarr.get(i);
			if (obj!=null) Array.set(arr, i, parseClass(obj,clazz));
		}
	}


	private enum FieldType {
		PRIMITIVE,LIST,ARRAY,CLASS,JSON
	}

	private static class FieldStruct {
		public Field field;
		public String name;
		public FieldType type;
		public Class<?> clazz;

		private static final Class<?>[] primitiveClasses={
				Boolean.class,Byte.class,Short.class,Integer.class,Long.class,
				Float.class,Double.class,Character.class,String.class};

		public FieldStruct(Field field) {
			this.field=field;
			name=field.getName();
			clazz=field.getType();
			if (isPrimitive(clazz)) {
				type=FieldType.PRIMITIVE;
			}
			else if (clazz.isArray()) {
				type=FieldType.ARRAY;
				clazz=clazz.getComponentType();
			} else if (clazz.isAssignableFrom(ArrayList.class)) {
				type=FieldType.LIST;
				try {
					ParameterizedType ptype=(ParameterizedType)field.getGenericType();
					clazz=(Class<?>) ptype.getActualTypeArguments()[0];
				} catch (Exception e) {
					clazz=null;
				}
			}
			else if (clazz.isAssignableFrom(JSONObject.class)) {
				type=FieldType.JSON;
			}
			else type=FieldType.CLASS;
		}

		private static boolean isPrimitive(Class<?> clazz) {
			if (clazz.isPrimitive()) return true;
			for (Class<?> pclazz:primitiveClasses) {
				if (clazz==pclazz) return true;
			}
			return false;
		}
	}
}