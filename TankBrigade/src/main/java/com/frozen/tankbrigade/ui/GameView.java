package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.frozen.tankbrigade.map.GameUnit;
import com.frozen.tankbrigade.map.MoveMap;
import com.frozen.tankbrigade.map.MoveNode;
import com.frozen.tankbrigade.map.PathFinder;
import com.frozen.tankbrigade.map.TerrainMap;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

/**
 * Created by sam on 04/01/14.
 */
//public class GameView extends View {
public class GameView extends BaseSurfaceView implements View.OnTouchListener,
		MultiTouchController.MultiTouchObjectCanvas<Object> {
    public GameView(Context context) {
        this(context, null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		setOnTouchListener(this);
    }

	public static final String TAG="GameView";

	private TerrainMap map;
	private Matrix tileToScreen;
	private final static int tileSize=50;
	private RectF tileRect=new RectF();
	private MapDrawer renderer=new MapDrawer();
	private PathFinder pathFinder=new PathFinder();
	private MoveMap mapPaths;

	private boolean uiEnabled=true;

	private MultiTouchController<Object> multitouch=new MultiTouchController<Object>(this);

	public void setMap(TerrainMap map) {
		this.map=map;
		tileToScreen=new Matrix();
		tileToScreen.setScale(tileSize,tileSize);
		invalidate();
	}

	@Override
	protected void drawSurface(Canvas canvas) {
		if (mapPaths!=null&&mapPaths.isAnimating()&&mapPaths.isAnimationComplete()) {
			onAnimationComplete();
		}
		renderer.drawMap(canvas,map,tileToScreen, mapPaths);
	}

	private void onAnimationComplete() {
		if (mapPaths==null||!mapPaths.isAnimating()||mapPaths.getSelectedMove()==null) return;
		Point endPoint=mapPaths.getSelectedMove();
		mapPaths.unit.x=endPoint.x;
		mapPaths.unit.y=endPoint.y;
		uiEnabled=true;
		mapPaths=null;
	}

	//handle touch events


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (!uiEnabled) return false;
		if (tileToScreen==null) return true;

		multitouch.onTouchEvent(motionEvent);
		if (motionEvent.getActionMasked()==MotionEvent.ACTION_DOWN&&motionEvent.getActionIndex()==0) {
			Point tilePos=renderer.getMapPosFromScreen(motionEvent.getX(),motionEvent.getY(),tileToScreen,map);
			if (tilePos!=null) onTileSelected(tilePos);
		}
		return true;
	}

	private void onTileSelected(Point tilePos) {
		Log.i(TAG,"onTileSelected "+tilePos);
		GameUnit unit=map.getUnitAt(tilePos.x,tilePos.y);
		GameUnit selectedUnit=(mapPaths ==null?null: mapPaths.unit);
		if (unit==selectedUnit) mapPaths =null;
		else if (unit!=null) {
			mapPaths=new MoveMap(pathFinder.findLegalMoves(map,unit,tilePos.x,tilePos.y),unit);
		}
		else if (mapPaths !=null) {
			MoveNode selectedMove=mapPaths.map[tilePos.x][tilePos.y];
			if (selectedMove==null) mapPaths=null;
			else if (selectedMove==mapPaths.getSelectedMove()) {
				mapPaths.animateMove(selectedMove);
				uiEnabled=false;
			} else {
				mapPaths.showMove(selectedMove);
			}
		}
	}

	@Override
	public Object getDraggableObjectAtPoint(PointInfo touchPoint) {
		return new Object();
	}

	@Override
	public void getPositionAndScale(Object obj, PositionAndScale objPosAndScaleOut) {
		tileRect.set(0,0,1,1);
		tileToScreen.mapRect(tileRect);
		objPosAndScaleOut.set(tileRect.left,tileRect.top,true,tileRect.width(),false,0,0,false,0);
	}

	@Override
	public boolean setPositionAndScale(Object obj, PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		tileToScreen.reset();
		float scale=newObjPosAndScale.getScale();
		float maxScale=getWidth()/4;
		if (scale<20) scale=20;
		if (scale>maxScale) scale=maxScale;
		tileToScreen.setScale(scale,scale);
		tileToScreen.postTranslate(newObjPosAndScale.getXOff(),newObjPosAndScale.getYOff());
		return true;
	}

	@Override
	public void selectObject(Object obj, PointInfo touchPoint) {
		Log.d(TAG,"selectObject");
	}
}
