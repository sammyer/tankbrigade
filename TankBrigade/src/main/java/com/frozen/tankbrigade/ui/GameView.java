package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.frozen.tankbrigade.map.MapDrawParameters;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.SpriteAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.map.anim.UnitAttackAnimation;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.UnitMove;

import org.metalev.multitouch.controller.MultiTouchController;
import org.metalev.multitouch.controller.MultiTouchController.PointInfo;
import org.metalev.multitouch.controller.MultiTouchController.PositionAndScale;

import java.util.Iterator;

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

	public static interface GameViewListener {
		public void onTileSelected(Point pos);
		public void onAnimationComplete(MapAnimation animation, boolean allComplete);
	}

	public static final String TAG="GameView";

	private GameBoard map;
	private Matrix tileToScreen;
	private final static int tileSize=50;
	private RectF tileRect=new RectF();
	private MapDrawer renderer;
	private GameViewListener listener;
	private MapDrawParameters drawParams =new MapDrawParameters();

	private boolean uiEnabled=true;

	private MultiTouchController<Object> multitouch=new MultiTouchController<Object>(this);

	public void setMap(GameBoard map,GameData config) {
		renderer=new MapDrawer(getContext(),config);
		this.map=map;
		tileToScreen=new Matrix();
		tileToScreen.setScale(tileSize,tileSize);
		invalidate();
	}

	public void setListener(GameViewListener listener) {
		this.listener=listener;
	}

	@Override
	protected void drawSurface(Canvas canvas) {

		synchronized (drawParams) {
			if (drawParams!=null&&drawParams.isAnimating()) {
				Log.d(TAG,"animation complete");
				Iterator<MapAnimation> animationIterator=drawParams.getAnimations().iterator();
				int animCount=drawParams.getAnimations().size();
				while (animationIterator.hasNext()) {
					final MapAnimation animation=animationIterator.next();
					if (animation.isAnimationComplete()) {
						animationIterator.remove();
						animCount--;
						final boolean animationsComplete=(animCount==0);
						if (listener!=null) {
							new Handler(Looper.getMainLooper()).post(new Runnable() {
								@Override
								public void run() {
									listener.onAnimationComplete(animation,animationsComplete);
								}
							});
						}
					}
				}
				if (!drawParams.isAnimating()) uiEnabled=true;
			}
		}

		MapDrawParameters drawParameters=this.drawParams.clone(); //to avoid concurrency issues
		renderer.drawMap(canvas,map,tileToScreen, drawParameters);
	}


	//handle touch events


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (!uiEnabled) return false;
		if (tileToScreen==null) return true;

		if (motionEvent.getActionMasked()==MotionEvent.ACTION_DOWN&&motionEvent.getActionIndex()==0) {
			Point tilePos=renderer.getMapPosFromScreen(motionEvent.getX(),motionEvent.getY(),tileToScreen,map);
			if (tilePos!=null&&listener!=null) {
				listener.onTileSelected(tilePos);
			}
		}

		//check uienabled again in case animation was started
		if (uiEnabled) multitouch.onTouchEvent(motionEvent);
		return true;
	}

	@Override
	public Object getDraggableObjectAtPoint(PointInfo touchPoint) {
		return new Object();
	}

	@Override
	public void getPositionAndScale(Object obj, PositionAndScale objPosAndScaleOut) {
		tileRect.set(0, 0, 1, 1);
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
		//Log.d(TAG,"selectObject");
	}

	public void setFocusRect(RectF rect,boolean union) {
		int w=getWidth();
		int h=getHeight();
		if (w==0||h==0) return;
		RectF screenRect=new RectF(0, 0, w, h);
		if (union) {
			Matrix screenToTile=new Matrix();
			RectF mapBoundsRect=new RectF();
			tileToScreen.invert(screenToTile);
			screenToTile.mapRect(mapBoundsRect,screenRect);
			rect.union(mapBoundsRect);
		}
		tileToScreen.setRectToRect(rect,screenRect, Matrix.ScaleToFit.CENTER);
	}

	// ----------- UI ----------------

	public void startAnimation() {
		Log.d(TAG,"startAnimation");
		uiEnabled=false;
	}

	public void clearPath() {
		drawParams.selectedPath=null;
		drawParams.selectedAttack=null;
	}

	public void setOverlay(short[][] overlay) {
		drawParams.mapOverlay=overlay;
	}

	public void highlightPath(Point[] path,Point attack) {
		drawParams.selectedPath=path;
		drawParams.selectedAttack=attack;
	}

	public void animateMove(UnitMove move) {
		UnitAnimation unitAnim=new UnitAnimation(move);
		move.unit.setAnimation(unitAnim);
		addAnimation(unitAnim);
	}

	public void animateAttack(UnitMove move) {
		animateAttack(move,move.unit,move.getAttackPoint());
	}

	public void animateCounterattack(UnitMove move) {
		animateAttack(move,move.attackTarget,move.unit.getPos());
	}

	protected void animateAttack(UnitMove move, GameUnit unit, Point target) {
		UnitAttackAnimation unitAnim=new UnitAttackAnimation(move,unit);
		unit.setAnimation(unitAnim);
		addAnimation(unitAnim);
		SpriteAnimation explosion=new SpriteAnimation(getContext(),
				SpriteAnimation.EXPLOSION_RES,300,target);
		explosion.setStartTime(200);
		addAnimation(explosion);
	}

	protected void addAnimation(MapAnimation animation) {
		uiEnabled=false;
		synchronized (drawParams) {
			drawParams.addAnimation(animation);
		}
	}

	public void focusOnMove(UnitMove move) {
		RectF moveRect=new RectF(0,0,1,1);
		moveRect.offsetTo(move.unit.x, move.unit.y);
		RectF endRect=new RectF(0,0,1,1);
		Point endpoint=move.getEndPoint();
		if (endpoint!=null) {
			endRect.offsetTo(endpoint.x,endpoint.y);
			moveRect.union(endRect);
		}
		endpoint=move.getAttackPoint();
		if (endpoint!=null) {
			endRect.offsetTo(endpoint.x,endpoint.y);
			moveRect.union(endRect);
			}
		setFocusRect(moveRect,true);
	}
}
