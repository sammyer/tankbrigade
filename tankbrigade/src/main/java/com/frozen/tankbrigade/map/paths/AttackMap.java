package com.frozen.tankbrigade.map.paths;

import android.graphics.Point;
import android.util.SparseIntArray;

import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.BitSet;

/**
 * Created by sam on 02/03/14.
 */
public class AttackMap {
	private GameUnit unit;
	private BitSet bitset;
	private SparseIntArray attackOrigins;
	private int w;
	private int h;

	public AttackMap(GameUnit unit, int w, int h) {
		this.unit = unit;
		this.w = w;
		this.h = h;
		bitset =new BitSet(w*h);
		attackOrigins =new SparseIntArray(w*h);
	}

	public AttackMap(GameUnit unit, SparseMap<UnitMove> map) {
		this.unit = unit;
		w=map.width();
		h=map.height();
		bitset =new BitSet(w*h);
		attackOrigins =new SparseIntArray(w*h);
		setAttacks(map);
	}

	public GameUnit getUnit() {
		return unit;
	}

	protected void set(int x, int y, int moveX, int moveY) {
		if (!isInBounds(x,y)) return;
		bitset.set(index(x,y));
		attackOrigins.put(index(x, y), index(moveX, moveY));
	}

	public boolean get(int x, int y) {
		if (!isInBounds(x,y)) return false;
		return bitset.get(y*w+x);
	}

	public Point getAttackOrigin(int x, int y) {
		if (!isInBounds(x,y)) return null;
		int attackOrigin=attackOrigins.get(index(x,y),-1);
		if (attackOrigin==-1) return null;
		else return indexToPoint(attackOrigin);
	}

	private int index(int x, int y) {
		return y*w+x;
	}

	private Point indexToPoint(int idx) {
		int x=idx%w;
		int y=(idx-x)/w;
		return new Point(x,y);
	}

	private boolean isInBounds(int x, int y) {
		return x>=0&&x<w&&y>=0&&y<h;
	}


	public void setAttacks(SparseMap<UnitMove> nodeMap) {
		if (unit.type.isRanged()) setRangedAttacks();
		else {
			for (UnitMove node:nodeMap.getAllNodes()) {
				if (node.getActionType()==UnitMove.MOVE) {
					int x=node.x;
					int y=node.y;
					set(x+1,y,x,y);
					set(x-1,y,x,y);
					set(x,y+1,x,y);
					set(x,y-1,x,y);
				}
			}
		}
	}

	public void setRangedAttacks() {
		setRangedAttacks(unit.type.getMinRange(),unit.type.getMaxRange());
	}

	private void setRangedAttacks(int minRange, int maxRange) {
		int x=unit.x;
		int y=unit.y;
		for (int range=minRange;range<=maxRange;range++) {
			for (int a=0;a<range;a++) {
				int b=range-a;
				set(x+b,y-a,x,y);
				set(x-a,y-b,x,y);
				set(x-b,y+a,x,y);
				set(x+a,y+b,x,y);
			}
		}
	}
}
