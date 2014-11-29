package com.frozen.tankbrigade.map.paths;

import android.util.Log;

import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.BitSet;

/**
 * Created by sam on 02/03/14.
 */
public class AttackMap {
	private GameUnit unit;
	private BitSet bitSet;
	private int w;
	private int h;

	public AttackMap(GameUnit unit, int w, int h) {
		this.unit = unit;
		this.w = w;
		this.h = h;
		bitSet=new BitSet(w*h);
	}

	public AttackMap(GameUnit unit, SparseMap<UnitMove> map) {
		this.unit = unit;
		w=map.width();
		h=map.height();
		bitSet=new BitSet(w*h);
		setAttacks(map);
	}

	public GameUnit getUnit() {
		return unit;
	}

	public void set(int x, int y) {
		if (!isInBounds(x,y)) return;
		bitSet.set(y*w+x);
	}

	public boolean get(int x, int y) {
		if (!isInBounds(x,y)) return false;
		return bitSet.get(y*w+x);
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
					set(x+1,y);
					set(x-1,y);
					set(x,y+1);
					set(x,y-1);
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
				set(x+b,y-a);
				set(x-a,y-b);
				set(x-b,y+a);
				set(x+a,y+b);
			}
		}
	}
}
