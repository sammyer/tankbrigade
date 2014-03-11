package com.frozen.tankbrigade.util;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 10/03/14.
 */
public class SparseMap<T> {
	private int w;
	private int h;
	private SparseArray<T> arr;

	public SparseMap(int w, int h) {
		this.w = w;
		this.h = h;
		arr=new SparseArray<T>();
	}

	public void set(int x, int y, T item) {
		if (!isInBounds(x,y)) return;
		arr.put(y*w+x,item);
	}
	public T get(int x, int y) {
		if (!isInBounds(x,y)) return null;
		return arr.get(y*w+x);
	}
	public boolean isInBounds(int x, int y) {
		return x>=0&&x<w&&y>=0&&y<h;
	}
	public int width() {
		return w;
	}
	public int height() {
		return h;
	}
	public List<T> getAllNodes() {
		return asList(arr);
	}

	private static <C> List<C> asList(SparseArray<C> sparseArray) {
		if (sparseArray == null) return null;
		List<C> arrayList = new ArrayList<C>(sparseArray.size());
		for (int i = 0; i < sparseArray.size(); i++)
			arrayList.add(sparseArray.valueAt(i));
		return arrayList;
	}

}
