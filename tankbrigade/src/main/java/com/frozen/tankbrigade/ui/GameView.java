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
	private PathFinder.MoveMap selectedPos;

	private MultiTouchController<Object> multitouch=new MultiTouchController<Object>(this);

	public void setMap(TerrainMap map) {
		this.map=map;
		tileToScreen=new Matrix();
		tileToScreen.setScale(tileSize,tileSize);
		invalidate();
	}

	@Override
	protected void drawSurface(Canvas canvas) {
	//protected void onDraw(Canvas canvas) {
		renderer.drawMap(canvas,map,tileToScreen,selectedPos);
	}

	//handle touch events


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
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
		GameUnit selectedUnit=(selectedPos==null?null:selectedPos.unit);
		if (unit==selectedUnit) selectedPos=null;
		else if (unit!=null) selectedPos=pathFinder.findLegalMoves(map,unit,tilePos.x,tilePos.y);
		else if (selectedPos!=null) {
			selectedPos.selectedMove=selectedPos.map[tilePos.x][tilePos.y];
			if (selectedPos.selectedMove==null) selectedPos=null;
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
		tileToScreen.setScale(newObjPosAndScale.getScale(),newObjPosAndScale.getScale());
		tileToScreen.postTranslate(newObjPosAndScale.getXOff(),newObjPosAndScale.getYOff());
		return true;
	}

	@Override
	public void selectObject(Object obj, PointInfo touchPoint) {
		Log.d(TAG,"selectObject");
	}
}
