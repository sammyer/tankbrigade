package com.frozen.tankbrigade.util;


import com.frozen.tankbrigade.map.model.Ordered2D;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sam on 30/11/14.
 */
public class Iterator2D<T extends Ordered2D> implements Iterator<T> {
	private Iterator<T> iterator;
	private Comparator2D comparator;
	private T curItem;


	private class Comparator2D implements Comparator<T> {
		private boolean horizontal;
		private boolean reverseX;
		private boolean reverseY;

		private Comparator2D(boolean horizontal, boolean reverseX, boolean reverseY) {
			this.horizontal = horizontal;
			this.reverseX = reverseX;
			this.reverseY = reverseY;
		}

		@Override
		public int compare(T item1, T item2) {
			return compare(item1.getOrderX(),item1.getOrderY(),item2.getOrderX(),item2.getOrderY());
		}

		public int compare(int x1, int y1, int x2, int y2) {
			int diffX=x1-x2;
			if (reverseX) diffX=-diffX;
			int diffY=y1-y2;
			if (reverseY) diffY=-diffY;
			if (horizontal) return compare2D(diffY,diffX);
			else return compare2D(diffX,diffY);
		}

		private int compare2D(int primary, int secondary) {
			if (primary==0) return secondary;
			else return primary;
		}
	}

	public Iterator2D(Collection<T> items) {
		this(items,true,false,false);
	}

	public Iterator2D(Collection<T> items, boolean horizontal, boolean reverseX, boolean reverseY) {
		List<T> sortedItems=new ArrayList<T>(items);
		comparator=new Comparator2D(horizontal,reverseX,reverseY);
		Collections.sort(sortedItems,comparator);
		iterator=sortedItems.iterator();
		next();
	}

	//iterates through items until we get to position x,y
	public T seek(int x, int y) {
		int order;
		int itemX;
		int itemY;
		while (curItem!=null) {
			itemX=curItem.getOrderX();
			itemY=curItem.getOrderY();
			order=comparator.compare(x,y,itemX,itemY);
			if (order==0) return next();  //x,y, is current item
			else if (order>0) next(); //x,y, is after itemX,itemY
			else return null; //x,y, is before itemX,itemY
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		return curItem!=null;
	}

	public T next() {
		T item=curItem;
		if (iterator.hasNext()) curItem=iterator.next();
		else curItem=null;
		return item;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
