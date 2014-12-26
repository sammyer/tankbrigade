package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.debug.DebugTools;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.SpriteAnimation;
import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.ColorGradient;
import com.frozen.tankbrigade.util.GeomUtils;
import com.frozen.tankbrigade.util.Iterator2D;
import com.frozen.tankbrigade.util.TileRect;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 12/01/14.
 */
public class GraphicMapDrawer implements MapDrawer {
	private static final String TAG="BasicMapDrawer";

	private Matrix tileToScreen=new Matrix();
	private Matrix screenToTile=new Matrix();
	private RectF screenRect=new RectF();
	private RectF mapBoundsRect=new RectF();
	private TileRect drawRect=new TileRect();
	private RectF subrect=new RectF();
	private Paint paint=new Paint();
	private ColorFilters filters=new ColorFilters();

	private BitmapCache terrainTiles;
	private int moveOverlayColor=0x44FFFFFF;
	private int attackOverlayColor=0x88DD4422;
	private int invalidOverlayColor=0x80000000;
	private int selectedOverlayColor=0x88FFFF88;
	private final static int tileW=100;
	private final static int tileH=80;


	private static class BitmapCache {
		private SparseArray<Bitmap> bitmapCache;
		private Resources res;

		private BitmapCache(Resources res) {
			this.res = res;
			bitmapCache=new SparseArray<Bitmap>();
		}

		public Bitmap get(int id) {
			Bitmap bitmap=bitmapCache.get(id);
			if (bitmap==null) {
				Log.d(TAG,"loading bitmap first time - "+id);
				bitmap = BitmapFactory.decodeResource(res, id);
				bitmapCache.put(id,bitmap);
			}
			return bitmap;
		}
	}

	public GraphicMapDrawer(Context context, GameData config) {
		terrainTiles=new BitmapCache(context.getResources());
	}

	public void drawMap(Canvas canvas, GameBoard map, Matrix screenTransform) {
		drawMap(canvas, map, screenTransform,null);
	}

	private int count=0;
	public void drawMap(Canvas canvas, GameBoard map, Matrix screenTransform,MapDrawParameters params) {
		count++;
		int w=canvas.getWidth();
		int h=canvas.getHeight();
		//Log.i(TAG, "drawSurface - map=" + map + "  view dims=" + w + "," + h);
		if (map==null) return;
		if (w==0||h==0) return;
		screenRect.set(0, 0, w, h);

		tileToScreen.set(screenTransform);
		tileToScreen.preScale(tileW,tileH);
		tileToScreen.invert(screenToTile);
		screenToTile.mapRect(mapBoundsRect,screenRect);
		drawRect.setMatrix(tileToScreen);

		Rect mapBounds=new Rect();
		mapBounds.left=Math.max((int)Math.floor(mapBoundsRect.left),0);
		mapBounds.right=Math.min((int) Math.ceil(mapBoundsRect.right), map.width());
		mapBounds.top=Math.max((int)Math.floor(mapBoundsRect.top),0);
		mapBounds.bottom=Math.min((int) Math.ceil(mapBoundsRect.bottom), map.height());
		//Log.d(TAG,"screenRect="+screenRect);
		//Log.d(TAG,"drawRect="+drawRect);
		//Log.d(TAG,"range = "+minX+","+minY+"-"+maxX+","+maxY);

		//reset canvas
		canvas.drawColor(Color.BLACK);

		if (params.testMode>0) drawTestMap(canvas,map,params,mapBounds);
		else drawMapAux(canvas, map, params, mapBounds);
	}

	private void drawMapAux(Canvas canvas, GameBoard map, MapDrawParameters params, Rect mapBounds) {
		int minY=mapBounds.top;
		int maxY=mapBounds.bottom;
		int minX=mapBounds.left;
		int maxX=mapBounds.right;

		synchronized (map.getUnits()) {
			for (GameUnit unitToUpdate:map.getUnits()) {
				unitToUpdate.updateAnimationPos(); //rfresh animation position
			}
		}

		Iterator2D<Building> buildingIter=new Iterator2D<Building>(map.getBuildings());
		Iterator2D<GameUnit> unitIter=new Iterator2D<GameUnit>(map.getUnits());
		Building building;
		GameUnit unit;

		//boolean showMoves=false;
		int shadingType;
		ColorMatrix shading=null;

		for (int tileY=minY;tileY<maxY;tileY++) {
			//first draw terrrains for row
			for (int tileX=minX;tileX<maxX;tileX++) {
				if (params!=null) shadingType=params.getOverlay(tileX,tileY);
				else shadingType=MapDrawParameters.SHADE_NONE;

				if (shadingType==MapDrawParameters.SHADE_INVALID) {
					shading=ColorFilters.darkenColorMatrix;
				} else if (shadingType==MapDrawParameters.SHADE_ATTACK) {
						shading=ColorFilters.redColorMatrix;
				} else if (shadingType==MapDrawParameters.SHADE_SELECTED_UNIT) {
					shading=ColorFilters.highlightColorMatrix;
				} else shading=null;

				drawRect.setTilePos(tileX,tileY);
				drawTerrain(canvas, drawRect,map.getTerrainMap(),tileX,tileY,shading);

				if (shadingType==MapDrawParameters.SHADE_INVALID) {
					shading=ColorFilters.darkenColorMatrix;
				} else shading=null;
				building=buildingIter.seek(tileX,tileY);
				if (building!=null) drawBuilding(canvas, building, drawRect,shading);
			}
			//then draw units
			for (int tileX=minX;tileX<maxX;tileX++) {

				unit=unitIter.seek(tileX,tileY);
				while (unit!=null) {
					boolean moveSelected=false;
					if (params!=null) {
						shadingType=params.getOverlay(tileX,tileY);
						moveSelected=params.hasSelectedMove();
					}
					else shadingType=MapDrawParameters.SHADE_NONE;

					if (unit.movesLeft==0&&unit.ownerId==Player.USER_ID&&!unit.isAnimating()) {
						shading=ColorFilters.darkenColorMatrix;
					} else if (shadingType==MapDrawParameters.SHADE_INVALID) {
						shading=ColorFilters.darkenColorMatrix;
					} else if (moveSelected&&shadingType==MapDrawParameters.SHADE_ATTACK) {
						shading=ColorFilters.highlightColorMatrix;
					} else shading=null;


					PointF unitPos=unit.getAnimationPos().point;
					float unitPosY=unitPos.y;
					//move unit up or down based on terrain level
					unitPosY-=interpolateLevel(map.getTerrainMap(),unitPos.x,unitPos.y)*0.4f;
					drawRect.setTilePos(unitPos.x,unitPosY);
					drawUnit(canvas,unit,drawRect,shading);
					if (unit.health<unit.type.health) {
						float healthPercent=unit.health/(float)unit.type.health;
						drawUnitHealthBar(canvas,healthPercent,drawRect);
					}

					unit=unitIter.seek(tileX,tileY);
				}
			}
		}

		if (params==null) return;
		paint.reset();
		paint.setStyle(Paint.Style.FILL);

		if (params.showPath()) drawPath(canvas, drawRect, params.getSelectedPath());
		for (MapAnimation animation:params.getAnimations()) {
			if (animation instanceof SpriteAnimation) {
				drawAnimation(canvas, drawRect, (SpriteAnimation) animation);
			}
		}
	}

	private Rect srcRect=new Rect();
	private RectF destRect=new RectF();
	//yOffset is percentage of the tile width
	private void drawWithOverflow(Canvas canvas, Bitmap bitmap, RectF rect, int level) {
		drawWithOverflow(canvas, bitmap, rect, level,null);
	}
	private void drawWithOverflow(Canvas canvas, Bitmap bitmap, RectF rect, int level,Paint paint) {
		srcRect.set(0,0,bitmap.getWidth()-1,bitmap.getHeight());
		float w=rect.width();
		float yOffset=w*(0.5f+0.4f*level);
		destRect.set(0,0,w,w*srcRect.height()/srcRect.width());
		destRect.offsetTo(rect.left,rect.top-yOffset);

		canvas.drawBitmap(bitmap,srcRect,destRect,paint);
	}

	private void drawTerrain(Canvas canvas, RectF rect, TerrainMap map, int x, int y, ColorMatrix colorMatrix) {
		TerrainType terrain=map.getTerrain(x,y);
		int resId=DrawableMapping.getTerrainDrawable(map, x, y);
		if (resId==0) return;
		Bitmap bitmap=terrainTiles.get(resId);

		int ypos=terrain.getLevel();
		drawWithOverflow(canvas, bitmap, rect, ypos,filters.getPaint(colorMatrix));

		if (terrain.getLevel()==0) {
			if (map.getTerrainLevel(x-1,y+1)==0&&map.getTerrainLevel(x,y+1)==-1) {
				bitmap=terrainTiles.get(R.drawable.shadow_side_w);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
		} else if (terrain.getLevel()==-1) {
			if (map.getTerrainLevel(x,y+1)==0) {
				bitmap=terrainTiles.get(R.drawable.shadow_s);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x,y-1)==0) {
				bitmap=terrainTiles.get(R.drawable.shadow_n);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x+1,y)==0) {
				bitmap=terrainTiles.get(R.drawable.shadow_e);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x-1,y)==0) {
				bitmap=terrainTiles.get(R.drawable.shadow_w);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}

			if (map.getTerrainLevel(x+1,y+1)==0&&map.getTerrainLevel(x+1,y)<0) {
				bitmap=terrainTiles.get(R.drawable.shadow_se);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x-1,y+1)==0&&map.getTerrainLevel(x-1,y)<1) {
				bitmap=terrainTiles.get(R.drawable.shadow_sw);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x+1,y-1)==0&&
				map.getTerrainLevel(x+1,y)<1&&
				map.getTerrainLevel(x,y-1)<1) {
				bitmap=terrainTiles.get(R.drawable.shadow_ne);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
			if (map.getTerrainLevel(x-1,y-1)==0&&
					map.getTerrainLevel(x-1,y)<1&&
					map.getTerrainLevel(x,y-1)<1) {
				bitmap=terrainTiles.get(R.drawable.shadow_nw);
				drawWithOverflow(canvas, bitmap, rect, ypos);
			}
		}
		if (terrain.symbol==TerrainType.BRIDGE) {
			bitmap=terrainTiles.get(DrawableMapping.getBridgeDrawable(map, x, y));
			drawWithOverflow(canvas, bitmap, rect, 0,filters.getPaint());
		}
	}

	private void drawPath(Canvas canvas, TileRect drawRect, Point[] pts) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(0xFF555555);
		paint.setStrokeWidth(drawRect.width()/6);

		//Log.d(TAG,"drawPath "+pts);
		if (pts.length<2) return;

		Path path=new Path();

		drawRect.setTilePos(pts[0].x,pts[0].y);
		float x=drawRect.centerX();
		float y=drawRect.centerY();
		path.moveTo(x,y);
		for (int i=1;i<pts.length;i++) {
			drawRect.setTilePos(pts[i].x,pts[i].y);
			x=drawRect.centerX();
			y=drawRect.centerY();
			path.lineTo(x,y);
		}
		canvas.drawPath(path,paint);
	}


	private void drawAnimation(Canvas canvas, TileRect drawRect,SpriteAnimation animation) {
		Bitmap bitmap=animation.getBitmap();
		if (bitmap==null) return;
		drawRect.setTilePos(animation.position.x,animation.position.y);
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		srcRect.set(0,0,w,h);
		destRect.set(drawRect);
		GeomUtils.setRectAspect(destRect,w,h);

		canvas.drawBitmap(bitmap,srcRect,destRect,null);
	}

	private void drawUnit(Canvas canvas, GameUnit unit, RectF rect,ColorMatrix colorMatrix) {
		filters.setMatrix(colorMatrix);
		if (unit.ownerId== Player.USER_ID) filters.addMatrix(ColorFilters.redColorMatrix);
		else filters.addMatrix(ColorFilters.blueColorMatrix);
		Bitmap bitmap=terrainTiles.get(DrawableMapping.getUnitDrawable(unit.type));
		drawUnitBitmap(canvas,bitmap,rect,unit.getAnimationPos().angle==0,filters.getPaint());
	}

	private Matrix unitDrawMatrix=new Matrix();
	private void drawUnitBitmap(Canvas canvas, Bitmap bitmap, RectF rect, boolean flipped, Paint unitPaint) {
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		//Log.d(TAG,"drawUnitBitmap "+w+","+h+"  rect="+rect);
		unitDrawMatrix.reset();
		unitDrawMatrix.postTranslate(0,-w*0.45f);
		if (flipped) unitDrawMatrix.postScale(-1,1,w/2,0);
		float scale=rect.width()/w;
		unitDrawMatrix.postScale(scale, scale);
		unitDrawMatrix.postTranslate(rect.left, rect.top);
		canvas.drawBitmap(bitmap,unitDrawMatrix,unitPaint);
	}

	private RectF subrect2=new RectF();
	private void drawUnitHealthBar(Canvas canvas, float percent, RectF rect) {
		subrect.set(0.7f,0.05f,0.95f,0.55f);
		GeomUtils.transformRect(rect, subrect);
		subrect2.set(0, 1 - percent, 1, 1);
		GeomUtils.transformRect(subrect,subrect2);

		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setStrokeWidth(drawRect.width()*0.025f);
		paint.setColor(0xFF000000);
		canvas.drawRect(subrect,paint);

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(0xFF000000+GeomUtils.interpolateColor(0xFF0000,0x00AA00,percent));
		canvas.drawRect(subrect2, paint);

	}

	private int getBuildingDrawable(Building building) {
		if (building.isFactory()) {
			if (building.isOwnedBy(Player.USER_ID)) return R.drawable.factory_red;
			else if (building.isOwnedBy(Player.AI_ID)) return R.drawable.factory_blue;
			else return R.drawable.factory;
		} else return R.drawable.gem;
	}

	private void drawBuilding(Canvas canvas, Building building, RectF rect,ColorMatrix colorMatrix) {
		Bitmap bitmap=terrainTiles.get(getBuildingDrawable(building));
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();

		unitDrawMatrix.reset();
		unitDrawMatrix.postTranslate(0,-w*0.45f);
		float scale=rect.width()/w;
		unitDrawMatrix.postScale(scale, scale);
		unitDrawMatrix.postTranslate(rect.left, rect.top);

		filters.setMatrix(colorMatrix);
		ColorMatrix matrix;
		if (building.isOil()&&building.isOwnedBy(Player.USER_ID)) {
			filters.addMatrix(ColorFilters.redColorMatrix);
		} else if (building.isOil()&&building.isOwnedBy(Player.AI_ID)) {
			filters.addMatrix(ColorFilters.blueColorMatrix);
		}
		canvas.drawBitmap(bitmap,unitDrawMatrix, filters.getPaint());
	}


	private float interpolateLevel(TerrainMap map, float x, float y) {
		int xInt=(int)Math.floor(x);
		int yInt=(int)Math.floor(y);
		float xFrac=x-xInt;
		float yFrac=y-yInt;
		int lev1=map.getTerrainLevel2(xInt,yInt);
		int lev2=map.getTerrainLevel2(xInt+1,yInt);
		int lev3=map.getTerrainLevel2(xInt,yInt+1);
		int lev4=map.getTerrainLevel2(xInt+1,yInt+1);
		return GeomUtils.interpolate(
				GeomUtils.interpolate(lev1,lev2,xFrac),
				GeomUtils.interpolate(lev3,lev4,xFrac),
				yFrac
		);
	}

	@Override
	public Point getMapPosFromScreen(float screenX, float screenY, Matrix screenTransform, GameBoard map) {
		tileToScreen.set(screenTransform);
		tileToScreen.preScale(tileW,tileH);
		tileToScreen.invert(screenToTile);
		float[] xy={screenX,screenY};
		screenToTile.mapPoints(xy);
		int tileX=(int)Math.floor(xy[0]);
		int tileY=(int)Math.floor(xy[1]);
		//Log.d(TAG,"getMapPosFromScreen - "+screenX+","+screenY+" = "+tileX+","+tileY);
		if (tileX<0||tileY<0||tileX>=map.width()||tileY>=map.height()) return null;
		return new Point(tileX,tileY);
	}

	@Override
	public RectF getScreenBounds(Rect mapBounds) {
		RectF bounds=new RectF(
				mapBounds.left*tileW,
				mapBounds.top*tileH,
				mapBounds.right*tileW,
				mapBounds.bottom*tileH
		);
		return bounds;
	}






	private ColorGradient gradient;
	private void drawTestMap(Canvas canvas, GameBoard map, MapDrawParameters params, Rect mapBounds) {
		if (gradient==null) {
			gradient=new ColorGradient(0xFF0000,0x00CC00);
		}
		paint.setStyle(Paint.Style.FILL);
		for (int tileY=mapBounds.top;tileY<mapBounds.bottom;tileY++) {
			for (int tileX=mapBounds.left;tileX<mapBounds.right;tileX++) {
				drawRect.setTilePos(tileX, tileY);
				//float val=0.5f;
				float val= DebugTools.testMapAnalyzer.getMoveBonus(tileX,tileY);
				if (val>=0){
					paint.setColor(gradient.getColor(val));
					paint.setAlpha(50);
					canvas.drawRect(drawRect,paint);
				}
			}
		}
	}
}