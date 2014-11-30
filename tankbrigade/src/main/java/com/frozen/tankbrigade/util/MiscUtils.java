package com.frozen.tankbrigade.util;

/**
 * Created by sam on 30/11/14.
 */
public class MiscUtils {
	public static String capitalize(String s) {
		if (s==null) return null;
		else if (s.length()==0) return s;
		else if (s.length()==1) return s.toUpperCase();
		else return s.substring(0, 1).toUpperCase() + s.substring(1);
	}
}
