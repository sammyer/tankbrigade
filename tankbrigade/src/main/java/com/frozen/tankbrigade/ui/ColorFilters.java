package com.frozen.tankbrigade.ui;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sam on 30/11/14.
 */
public class ColorFilters {

	//private static final float[] redColorFilterVals={2,0,0,0,0,0,2,0,0,-256,0,0,2,0,-256,0,0,0,1,0};
	private static final float[] redColorFilterVals={1.5f,0,0,0,0,0,1.5f,0,0,-171,0,0,1.5f,0,-171,0,0,0,1,0};
	public static final ColorMatrix redColorMatrix=new ColorMatrix(redColorFilterVals);

	//private static final float[] blueColorFilterVals={2,0,0,0,-256,0,2,0,0,-256,0,0,2,0,0,0,0,0,1,0};
	private static final float[] blueColorFilterVals={1.5f,0,0,0,-171,0,1.5f,0,0,-171,0,0,1.5f,0,0,0,0,0,1,0};
	public static final ColorMatrix blueColorMatrix=new ColorMatrix(blueColorFilterVals);

	private static final float[] darkenColorFilterVals={0.5f,0,0,0,0,0,0.5f,0,0,0,0,0,0.5f,0,0,0,0,0,1,0};
	public static final ColorMatrix darkenColorMatrix=new ColorMatrix(darkenColorFilterVals);

	private static final float[] highlightColorFilterVals={1.6f,0,0,0,64,0,1.6f,0,0,64,0,0,1.6f,0,64,0,0,0,1,0};
	public static final ColorMatrix highlightColorMatrix=new ColorMatrix(highlightColorFilterVals);

	private final ColorMatrixColorFilter redColorFilter=new ColorMatrixColorFilter(redColorMatrix);
	private final ColorMatrixColorFilter blueColorFilter=new ColorMatrixColorFilter(blueColorMatrix);
	private final ColorMatrixColorFilter darkenColorFilter=new ColorMatrixColorFilter(darkenColorMatrix);
	private final ColorMatrixColorFilter highlightColorFilter=new ColorMatrixColorFilter(highlightColorMatrix);


	private Paint paint=new Paint();
	private ColorMatrix mColorMatrix;
	private ColorMatrix concatMatrix=new ColorMatrix();

	public void reset() {
		mColorMatrix=null;
	}

	public void setMatrix(ColorMatrix matrix) {
		reset();
		addMatrix(matrix);
	}

	public void addMatrix(ColorMatrix matrix) {
		if (matrix==null) return;
		if (mColorMatrix==null) mColorMatrix=matrix;
		else {
			if (mColorMatrix!=concatMatrix) {
				concatMatrix.reset();
				concatMatrix.postConcat(mColorMatrix);
				mColorMatrix=concatMatrix;
			}
			mColorMatrix.postConcat(matrix);
		}
	}

	public Paint getPaint(ColorMatrix matrix) {
		setMatrix(matrix);
		return getPaint();
	}

	public Paint getPaint() {
		if (mColorMatrix==null) return null;
		ColorMatrixColorFilter filter;
		if (mColorMatrix==redColorMatrix) filter=redColorFilter;
		else if (mColorMatrix==blueColorMatrix) filter=blueColorFilter;
		else if (mColorMatrix==darkenColorMatrix) filter=darkenColorFilter;
		else if (mColorMatrix==highlightColorMatrix) filter=highlightColorFilter;
		else filter=new ColorMatrixColorFilter(mColorMatrix);
		paint.setColorFilter(filter);
		return paint;
	}

}
