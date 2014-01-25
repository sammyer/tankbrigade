package com.frozen.tankbrigade.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;

import com.frozen.tankbrigade.map.GameUnit;
import com.frozen.tankbrigade.map.PathFinder;
import com.frozen.tankbrigade.map.TerrainMap;
import com.frozen.tankbrigade.map.TerrainType;

import java.util.List;

/**
 * Created by sam on 12/01/14.
 */
public class MapDrawer {
	private static final int[] terrainColors={0x88FF44,0x22EE22,0x008800,0xCCCCCC,0x444444,0x222233,0x2266FF,0xEEEEAA};
	private static final String TAG="MapDrawer";

	private Matrix screenToTile=new Matrix();
	private RectF screenRect=new RectF();
	private TileRect drawRect=new TileRect();
	private Paint paint=new Paint();

	private static final int SHADE_INVALID=0;
	private static final int SHADE_MOVE=1;
	private static final int SHADE_ATTACK=2;
	private static final int SHADE_SELECTED_UNIT=3;

	private static class TileRect extends RectF {
		private float mapLeft;
		private float mapTop;

		public void setMatrix(Matrix tileToScreen) {
			set(0, 0, 1, 1);
			tileToScreen.mapRect(this);
			mapLeft=left;
			mapTop=top;
		}

		public void setTilePos(int x, int y) {
			offsetTo(mapLeft+x*width(),mapTop+y*height());
		}
	}

	public void drawMap(Canvas canvas, TerrainMap map, Matrix tileToScreen) {
		drawMap(canvas, map, tileToScreen,null);
	}

	public void drawMap(Canvas canvas, TerrainMap map, Matrix tileToScreen,PathFinder.MoveMap moves) {
		int w=canvas.getWidth();
		int h=canvas.getHeight();
		//Log.i(TAG, "drawSurface - map=" + map + "  view dims=" + w + "," + h);
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

		//reset canvas
		canvas.drawColor(Color.BLACK);

		paint.setStyle(Paint.Style.FILL);
		drawRect.setMatrix(tileToScreen);
		for (int tileX=minX;tileX<maxX;tileX++) {
			for (int tileY=minY;tileY<maxY;tileY++) {
				drawRect.setTilePos(tileX,tileY);
				drawTerrain(canvas, drawRect,map.getTerrain(tileX,tileY));
			}
		}

		for (GameUnit unit:map.getUnits()) {
			if (unit.x<minX||unit.x>maxX||unit.y<minY||unit.y>maxY) continue;
			drawRect.setTilePos(unit.x, unit.y);
			drawUnit(canvas,unit,drawRect);
		}

		if (moves!=null) {
			int shadeId;
			for (int tileX=minX;tileX<maxX;tileX++) {
				for (int tileY=minY;tileY<maxY;tileY++) {
					drawRect.setTilePos(tileX,tileY);
					if (tileX==moves.unit.x&&tileY==moves.unit.y) shadeId=SHADE_SELECTED_UNIT;
					if (moves.map[tileX][tileY]==null) shadeId=SHADE_INVALID;
					else shadeId=SHADE_MOVE;
					drawMoveOverlay(canvas, drawRect,shadeId);
				}
			}

			if (moves.selectedMove!=null) drawMove(canvas, drawRect, moves.selectedMove);
		}
	}

	private void drawTerrain(Canvas canvas, RectF rect, TerrainType terrain) {
		paint.setColor(0xFF000000+terrainColors[terrain.id]);
		canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
	}

	private void drawMoveOverlay(Canvas canvas, RectF rect, int shadeId) {
		if (shadeId==SHADE_MOVE) {
			paint.setColor(0x88FFFFFF);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId==SHADE_INVALID) {
			paint.setColor(0xF0000000);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId==SHADE_SELECTED_UNIT) {
			paint.setColor(0xCCFFFFFF);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
	}

	private void drawMove(Canvas canvas, TileRect drawRect,PathFinder.MoveNode move) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(0xFFFF0000);
		paint.setStrokeWidth(drawRect.width()/3);
		List<Point> pts=move.getPath();
		Log.d(TAG,"drawMove "+pts);
		if (pts.size()<2) return;
		drawRect.setTilePos(pts.get(0).x,pts.get(0).y);
		float sx=drawRect.centerX();
		float sy=drawRect.centerY();
		float ex,ey;
		for (int i=1;i<pts.size();i++) {
			drawRect.setTilePos(pts.get(i).x,pts.get(i).y);
			ex=drawRect.centerX();
			ey=drawRect.centerY();
			canvas.drawLine(sx,sy,ex,ey,paint);
			sx=ex;
			sy=ey;
		}
	}

	private static final String[] unitchars={"C","B","F","T","R","A"};
	private void drawUnit(Canvas canvas, GameUnit unit, RectF rect) {
		String s=unitchars[unit.type.type];
		paint.setTextSize(rect.width());
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(unit.ownerId==1?0xFFFF0000:0xFF4444FF);
		float textHeight=paint.descent() + paint.ascent();
		canvas.drawText(s,rect.centerX(),rect.centerY()-textHeight/2,paint);
	}

	public Point getMapPosFromScreen(float screenX, float screenY, Matrix tileToScreen, TerrainMap map) {
		tileToScreen.invert(screenToTile);
		float[] xy={screenX,screenY};
		screenToTile.mapPoints(xy);
		int tileX=(int)Math.floor(xy[0]);
		int tileY=(int)Math.floor(xy[1]);
		if (tileX<0||tileY<0||tileX>=map.width()||tileY>=map.height()) return null;
		return new Point(tileX,tileY);
	}
}
