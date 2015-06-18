package com.frozen.tankbrigade.map.anim;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

import com.frozen.tankbrigade.R;

/**
 * Created by sam on 02/02/14.
 */
public class SpriteAnimation implements MapAnimation {
	public long animateStartTime;
	private int duration;
	public Point position;

	private Bitmap[] bitmaps;

	public static final int[] EXPLOSION_RES = {
		R.drawable.explosion1,
		R.drawable.explosion2,
		R.drawable.explosion3,
		R.drawable.explosion4,
		R.drawable.explosion5
	};

	public SpriteAnimation(Context context, int[] resourceIds, int durationMillis, Point position) {
		this(load(context,resourceIds),durationMillis,position);
	}
	public SpriteAnimation(Bitmap[] bitmaps, int durationMillis, Point position) {
		this.bitmaps=bitmaps;
		animateStartTime=System.currentTimeMillis();
		this.duration=durationMillis;
		this.position=position;
	}

	public void setStartTime(int offsetMillis) {
		animateStartTime=System.currentTimeMillis()+offsetMillis;
	}

	@Override
	public boolean isAnimationComplete() {
		long timediff=System.currentTimeMillis()-animateStartTime;
		return timediff>duration;
	}

	protected int getIdx() {
		int n=bitmaps.length;
		long timediff=System.currentTimeMillis()-animateStartTime;
		int idx=(int)timediff*n/duration;
		if (idx<0) return -1;
		if (idx>=n) return -1;
		return idx;
	}

	public static Bitmap[] load(Context context,int[] resourceIds) {
		int n=resourceIds.length;
		Bitmap[] bitmaps=new Bitmap[n];
		for (int i=0;i<n;i++) {
			bitmaps[i]= BitmapFactory.decodeResource(context.getResources(), resourceIds[i]);
		}
		return bitmaps;
	}

	public Bitmap getBitmap() {
		int idx=getIdx();
		if (idx<0) return null;
		else return bitmaps[idx];
	}
}
