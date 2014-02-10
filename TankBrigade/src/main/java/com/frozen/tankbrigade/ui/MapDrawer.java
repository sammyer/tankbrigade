package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.frozen.tankbrigade.map.MapDrawParameters;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.SpriteAnimation;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.GeomUtils;

import java.util.List;

/**
 * Created by sam on 12/01/14.
 */
public class MapDrawer {
	private static final String TAG="MapDrawer";

	private Matrix screenToTile=new Matrix();
	private RectF screenRect=new RectF();
	private RectF mapBoundsRect=new RectF();
	private TileRect drawRect=new TileRect();
	private RectF subrect=new RectF();
	private Paint paint=new Paint();

	private SparseIntArray terrainColorMap;

	public MapDrawer(Context context, GameData config) {
		terrainColorMap=new SparseIntArray(config.terrainTypes.size());
		for (TerrainType terrain:config.terrainTypes) {
			String colorName="terrainColor_"+terrain.name;
			int color=context.getResources().getIdentifier(colorName,"color",context.getPackageName());
			terrainColorMap.put(terrain.symbol,color);
		}
	}

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

		public void setTilePos(float x, float y) {
			offsetTo(mapLeft+x*width(),mapTop+y*height());
		}
	}

	public void drawMap(Canvas canvas, TerrainMap map, Matrix tileToScreen) {
		drawMap(canvas, map, tileToScreen,null);
	}

	public void drawMap(Canvas canvas, TerrainMap map, Matrix tileToScreen,MapDrawParameters params) {
		int w=canvas.getWidth();
		int h=canvas.getHeight();
		//Log.i(TAG, "drawSurface - map=" + map + "  view dims=" + w + "," + h);
		if (map==null) return;
		if (w==0||h==0) return;
		screenRect.set(0, 0, w, h);

		tileToScreen.invert(screenToTile);
		screenToTile.mapRect(mapBoundsRect,screenRect);
		int minX=(int)Math.floor(mapBoundsRect.left);
		if (minX<0) minX=0;
		int maxX=(int)Math.ceil(mapBoundsRect.right);
		if (maxX>map.width()) maxX=map.width();
		int minY=(int)Math.floor(mapBoundsRect.top);
		if (minY<0) minY=0;
		int maxY=(int)Math.ceil(mapBoundsRect.bottom);
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
			PointF unitPos=unit.getAnimationPos().point;
			drawRect.setTilePos(unitPos.x,unitPos.y);
			if (RectF.intersects(drawRect,screenRect)) {
				drawUnit(canvas,unit,drawRect);
				if (unit.health<unit.type.health) {
					float healthPercent=unit.health/(float)unit.type.health;
					drawUnitHealthBar(canvas,healthPercent,drawRect);
				}
			}
		}

		if (params==null) return;

		if (params.showMoves()) {
			for (int tileX=minX;tileX<maxX;tileX++) {
				for (int tileY=minY;tileY<maxY;tileY++) {
					drawRect.setTilePos(tileX,tileY);
					drawMoveOverlay(canvas, drawRect,params.mapOverlay[tileX][tileY]);
				}
			}
		}
		if (params.showPath()) drawMove(canvas, drawRect, params.getSelectedPath());
		if (params.selectedAttack!=null) drawAttack(canvas,drawRect,params.selectedAttack);
		for (MapAnimation animation:params.getAnimations()) {
			if (animation instanceof SpriteAnimation) {
				drawAnimation(canvas, drawRect, (SpriteAnimation) animation);
			}
		}
	}

	private void drawTerrain(Canvas canvas, RectF rect, TerrainType terrain) {
		paint.setColor(terrainColorMap.get(terrain.symbol));
		canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
	}

	private void drawMoveOverlay(Canvas canvas, RectF rect, int shadeId) {
		paint.setStyle(Paint.Style.FILL);
		if (shadeId== MapDrawParameters.SHADE_MOVE) {
			paint.setColor(0x88FFFFFF);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_INVALID) {
			paint.setColor(0xE0000000);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_ATTACK) {
			paint.setColor(0xCCFF0000);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_SELECTED_UNIT) {
			paint.setColor(0xCCFFFFFF);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
	}

	private void drawMove(Canvas canvas, TileRect drawRect,Point[] pts) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(0xFF2266FF);
		paint.setStrokeWidth(drawRect.width()/3);

		Log.d(TAG,"drawMove "+pts);
		if (pts.length<2) return;
		drawRect.setTilePos(pts[0].x,pts[0].y);
		float sx=drawRect.centerX();
		float sy=drawRect.centerY();
		float ex,ey;
		for (int i=1;i<pts.length;i++) {
			drawRect.setTilePos(pts[i].x,pts[i].y);
			ex=drawRect.centerX();
			ey=drawRect.centerY();
			canvas.drawLine(sx,sy,ex,ey,paint);
			sx=ex;
			sy=ey;
		}
	}

	private void drawAttack(Canvas canvas, TileRect drawRect,Point pos) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(0xFFFF8800);
		paint.setStrokeWidth(2);
		drawRect.setTilePos(pos.x,pos.y);
		canvas.drawCircle(drawRect.centerX(),drawRect.centerY(),drawRect.width()*0.45f,paint);
	}

	private void drawAnimation(Canvas canvas, TileRect drawRect,SpriteAnimation animation) {
		Bitmap bitmap=animation.getBitmap();
		if (bitmap==null) return;
		drawRect.setTilePos(animation.position.x,animation.position.y);
		canvas.drawBitmap(bitmap,drawRect.left,drawRect.top,null);
	}

	private void drawUnit(Canvas canvas, GameUnit unit, RectF rect) {
		String s=Character.toString(unit.type.symbol);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(rect.width());
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(unit.ownerId==1?0xFFFF0000:0xFF4444FF);
		float textHeight=paint.descent() + paint.ascent();
		canvas.drawText(s,rect.centerX(),rect.centerY()-textHeight/2,paint);
	}

	private RectF subrect2=new RectF();
	private void drawUnitHealthBar(Canvas canvas, float percent, RectF rect) {
		subrect.set(0.7f,0.05f,0.95f,0.55f);
		GeomUtils.transformRect(rect, subrect);
		subrect2.set(0, 1 - percent, 1, 1);
		GeomUtils.transformRect(subrect,subrect2);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xFF000000+GeomUtils.interpolateColor(0xFF0000,0x00AA00,percent));
		canvas.drawRect(subrect2, paint);

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(drawRect.width()*0.025f);
		paint.setColor(0xFF000000);
		canvas.drawRect(subrect,paint);
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
