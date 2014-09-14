package com.frozen.tankbrigade.util;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 30/03/14.
 */
public class ColorGradient {
	private static class GradientColor {
		public float pos;
		public int color;

		private GradientColor(float pos, int color) {
			this.pos = pos;
			this.color = color;
		}
	}

	private List<GradientColor> colors;

	public ColorGradient(int startColor, int endColor) {
		colors=new ArrayList<GradientColor>();
		colors.add(new GradientColor(0,startColor));
		colors.add(new GradientColor(1,endColor));
	}
	public ColorGradient(float startPos, int startColor, float endPos, int endColor) {
		colors=new ArrayList<GradientColor>();
		colors.add(new GradientColor(startPos,startColor));
		colors.add(new GradientColor(endPos,endColor));
	}

	public void addColor(float pos, int color) {
		int insertPos=0;
		for (GradientColor item:colors) {
			if (pos==item.pos) {
				item.color=color;
				return;
			} else if (pos>item.pos) insertPos++;
			else break;
		}
		GradientColor newItem=new GradientColor(pos,color);
		colors.add(insertPos,newItem);
	}

	public int getColor(float pos) {
		GradientColor prevItem=null;
		for (GradientColor item:colors) {
			if (pos==item.pos) return item.color;
			else if (pos<item.pos) {
				if (prevItem==null) return item.color;
				else {
					float x=(pos-prevItem.pos)/(item.pos-prevItem.pos);
					return interpolateColor(prevItem.color,item.color,x);
				}
			}
			prevItem=item;
		}
		return prevItem.color;
	}

	private int interpolateColor(int color1, int color2, float x) {
		float[] hsva = new float[3];
		float[] hsvb = new float[3];
		Color.colorToHSV(color1, hsva);
		Color.colorToHSV(color2, hsvb);
		for (int i = 0; i < 3; i++) {
			hsvb[i] = interpolate(hsva[i], hsvb[i], x);
		}
		return Color.HSVToColor(hsvb);
	}

	private float interpolate(float a, float b, float proportion) {
		return (a + ((b - a) * proportion));
	}
}
