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
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.util.SparseArray;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.SpriteAnimation;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.ColorGradient;
import com.frozen.tankbrigade.util.GeomUtils;
import com.frozen.tankbrigade.util.TileRect;

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

		paint.setStyle(Paint.Style.FILL);
		for (int tileY=minY;tileY<maxY;tileY++) {
			for (int tileX=minX;tileX<maxX;tileX++) {
				drawRect.setTilePos(tileX,tileY);
				drawTerrain(canvas, drawRect,map.terrainMap,tileX,tileY);
			}
		}

		for (GameUnit unit:map.getUnits()) {
			PointF unitPos=unit.getAnimationPos().point;
			float unitPosY=unitPos.y;
			//move unit up or down based on terrain level
			unitPosY-=interpolateLevel(map.terrainMap,unitPos.x,unitPos.y)*0.4f;
			drawRect.setTilePos(unitPos.x,unitPosY);
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
					if (tileX==params.selectedUnit.x&&tileY==params.selectedUnit.y) {
						drawMoveOverlay(canvas, drawRect,MapDrawParameters.SHADE_SELECTED_UNIT);
					}
					else drawMoveOverlay(canvas, drawRect,params.getOverlay(tileX,tileY));
				}
			}
		}
		if (params.showPath()) drawPath(canvas, drawRect, params.getSelectedPath());
		if (params.selectedAttack!=null) drawAttack(canvas,drawRect,params.selectedAttack);
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
		srcRect.set(0,0,bitmap.getWidth()-1,bitmap.getHeight());
		float w=rect.width();
		float yOffset=w*(0.5f+0.4f*level);
		destRect.set(0,0,w,w*srcRect.height()/srcRect.width());
		destRect.offsetTo(rect.left,rect.top-yOffset);
		canvas.drawBitmap(bitmap,srcRect,destRect,null);
	}

	private void drawTerrain(Canvas canvas, RectF rect, TerrainMap map, int x, int y) {
		TerrainType terrain=map.getTerrain(x,y);
		Bitmap bitmap;
		if (terrain.symbol==TerrainType.PLAIN||terrain.symbol==TerrainType.HILL) {
			bitmap=terrainTiles.get(R.drawable.plains);
		} else if (terrain.symbol==TerrainType.FOREST) {
			bitmap=terrainTiles.get(R.drawable.forest);
		} else if (terrain.symbol==TerrainType.MOUNTAIN) {
			bitmap=terrainTiles.get(R.drawable.mountain);
		} else if (terrain.symbol==TerrainType.WATER||terrain.symbol==TerrainType.BRIDGE) {
			bitmap=terrainTiles.get(R.drawable.water);
		} else if (terrain.symbol==TerrainType.BEACH) {
			bitmap=terrainTiles.get(R.drawable.beach);
		} else if (terrain.symbol==TerrainType.ROAD) {
			bitmap=terrainTiles.get(getRoadTile(map,x,y));
		} else return;
		int ypos=terrain.getLevel();
		drawWithOverflow(canvas, bitmap, rect, ypos);

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
			if (isRoad(map,x-1,y)||isRoad(map,x+1,y)) {
				bitmap=terrainTiles.get(R.drawable.bridge_ew);
			}
			else bitmap=terrainTiles.get(R.drawable.bridge_ns);
			drawWithOverflow(canvas, bitmap, rect, 0);
		}
	}

	private static final int[] roadTiles={
			R.drawable.road_ew,R.drawable.road_ew,
			R.drawable.road_ew,R.drawable.road_ew,
			R.drawable.road_ns,R.drawable.road_sw,
			R.drawable.road_se,R.drawable.road_sew,
			R.drawable.road_ns,R.drawable.road_nw,
			R.drawable.road_ne,R.drawable.road_new,
			R.drawable.road_ns,R.drawable.road_nsw,
			R.drawable.road_nse,R.drawable.road_nsew
	};
	private int getRoadTile(TerrainMap map, int x, int y) {
		int n=isRoad(map,x,y-1)?1:0;
		int s=isRoad(map,x,y+1)?1:0;
		int e=isRoad(map,x+1,y)?1:0;
		int w=isRoad(map,x-1,y)?1:0;
		int idx=n*8+s*4+e*2+w;
		return roadTiles[idx];
	}

	private boolean isRoad(TerrainMap map, int x, int y) {
		if (!map.isInBounds(x,y)) return false;
		else return map.getTerrain(x,y).isRoad();
	}



	private void drawMoveOverlay(Canvas canvas, RectF rect, int shadeId) {
		paint.setStyle(Paint.Style.FILL);
		if (shadeId== MapDrawParameters.SHADE_MOVE) {
			paint.setColor(moveOverlayColor);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_INVALID) {
			paint.setColor(invalidOverlayColor);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_ATTACK) {
			paint.setColor(attackOverlayColor);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
		if (shadeId== MapDrawParameters.SHADE_SELECTED_UNIT) {
			paint.setColor(selectedOverlayColor);
			canvas.drawRect(rect.left,rect.top,rect.right,rect.bottom,paint);
		}
	}

	private void drawPath(Canvas canvas, TileRect drawRect, Point[] pts) {
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(0xFF2266FF);
		paint.setStrokeWidth(drawRect.width()/3);

		Log.d(TAG,"drawPath "+pts);
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
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		srcRect.set(0,0,w,h);
		destRect.set(drawRect);
		GeomUtils.setRectAspect(destRect,w,h);

		canvas.drawBitmap(bitmap,srcRect,destRect,null);
	}

	private void drawUnit(Canvas canvas, GameUnit unit, RectF rect) {
		ColorMatrixColorFilter filter;
		if (unit.ownerId== Player.USER_ID) filter=redColorFilter;
		else filter=blueColorFilter;
		Bitmap bitmap=terrainTiles.get(getUnitDrawable(unit.type));
		drawUnitBitmap(canvas,bitmap,rect,unit.getAnimationPos().angle==0,filter);
	}

	private Matrix unitDrawMatrix=new Matrix();
	private Paint unitPaint=new Paint();
	//private static final float[] redColorFilterVals={2,0,0,0,0,0,2,0,0,-256,0,0,2,0,-256,0,0,0,1,0};
	private static final float[] redColorFilterVals={1.5f,0,0,0,0,0,1.5f,0,0,-171,0,0,1.5f,0,-171,0,0,0,1,0};
	private static final ColorMatrixColorFilter redColorFilter=
			new ColorMatrixColorFilter(new ColorMatrix(redColorFilterVals));
	//private static final float[] blueColorFilterVals={2,0,0,0,-256,0,2,0,0,-256,0,0,2,0,0,0,0,0,1,0};
	private static final float[] blueColorFilterVals={1.5f,0,0,0,-171,0,1.5f,0,0,-171,0,0,1.5f,0,0,0,0,0,1,0};
	private static final ColorMatrixColorFilter blueColorFilter=
			new ColorMatrixColorFilter(new ColorMatrix(blueColorFilterVals));
	private void drawUnitBitmap(Canvas canvas, Bitmap bitmap, RectF rect, boolean flipped, ColorFilter filter) {
		int w=bitmap.getWidth();
		int h=bitmap.getHeight();
		//Log.d(TAG,"drawUnitBitmap "+w+","+h+"  rect="+rect);
		unitDrawMatrix.reset();
		unitDrawMatrix.postTranslate(0,-w*0.45f);
		if (flipped) unitDrawMatrix.postScale(-1,1,w/2,0);
		float scale=rect.width()/w;
		unitDrawMatrix.postScale(scale,scale);
		unitDrawMatrix.postTranslate(rect.left, rect.top);
		unitPaint.setColorFilter(filter);
		canvas.drawBitmap(bitmap,unitDrawMatrix,unitPaint);
	}

	private int getUnitDrawable(GameUnitType unitType) {
		switch (unitType.symbol) {
			case GameUnitType.COMMANDO: return R.drawable.commando;
			case GameUnitType.BAZOOKA: return R.drawable.bazooka;
			case GameUnitType.FLAK: return R.drawable.flak;
			case GameUnitType.TANK: return R.drawable.tank;
			case GameUnitType.ROCKET: return R.drawable.rocket;
			case GameUnitType.AIRPLANE: return R.drawable.fighter;
		}
		//default
		return R.drawable.commando;
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
				paint.setColor(gradient.getColor(0.5f));
				canvas.drawRect(drawRect,paint);
			}
		}
	}
}