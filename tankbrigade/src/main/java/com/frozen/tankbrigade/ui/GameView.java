package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import com.frozen.tankbrigade.map.TerrainMap;
import com.frozen.tankbrigade.map.TerrainType;

/**
 * Created by sam on 04/01/14.
 */
//public class GameView extends View {
public class GameView extends BaseSurfaceView {
    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

	public static final String TAG="GameView";

	private TerrainMap map;
	private Matrix tileToScreen;
	private Matrix screenToTile;
	private final static int tileSize=50;

	private RectF tileRect=new RectF();
	private RectF screenRect=new RectF();
	private RectF drawRect=new RectF();
	private Paint paint=new Paint();

	public void setMap(TerrainMap map) {
		this.map=map;
		tileToScreen=new Matrix();
		tileToScreen.setScale(tileSize,tileSize);
		screenToTile=new Matrix();
		invalidate();
	}

	@Override
	protected void drawSurface(Canvas canvas) {
	//protected void onDraw(Canvas canvas) {
		int w=getWidth();
		int h=getHeight();
		Log.i(TAG,"drawSurface - map="+map+"  view dims="+w+","+h);
		if (map==null) return;
		if (w==0||h==0) return;
		screenRect.set(0, 0, w, h);

		tileToScreen.invert(screenToTile);
		screenToTile.mapRect(drawRect,screenRect);
		int minX=(int)Math.floor(drawRect.left);
		if (minX<0) minX=0;
		int maxX=(int)Math.ceil(drawRect.right);
		if (maxX>map.width()) maxX=map.width();
		int minY=(int)Math.floor(drawRect.top);
		if (minY<0) minY=0;
		int maxY=(int)Math.ceil(drawRect.bottom);
		if (maxY>map.width()) maxY=map.height();
		//Log.d(TAG,"screenRect="+screenRect);
		//Log.d(TAG,"drawRect="+drawRect);
		//Log.d(TAG,"range = "+minX+","+minY+"-"+maxX+","+maxY);


		paint.setStyle(Paint.Style.FILL);
		tileRect.set(0,0,1,1);
		tileToScreen.mapRect(drawRect,tileRect);
		float drawW=drawRect.width();
		float drawH=drawRect.height();
		float drawX, drawY;
		for (int tileX=minX;tileX<maxX;tileX++) {
			for (int tileY=minY;tileY<maxY;tileY++) {
				drawX=drawRect.left+drawW*tileX;
				drawY=drawRect.top+drawH*tileY;
				drawTerrain(canvas, drawX,drawY,drawW,drawH,map.getTerrain(tileX,tileY));
			}
		}
	}

	private static final int[] terrainColors={0x88FF44,0x22EE22,0x008800,0xCCCCCC,0x444444,0x222233,0x2266FF,0xEEEEAA};
	private void drawTerrain(Canvas canvas, float x, float y, float w, float h, TerrainType terrain) {
		paint.setColor(0xFF000000+terrainColors[terrain.id]);
		canvas.drawRect(x,y,x+w,y+h,paint);
	}
}
