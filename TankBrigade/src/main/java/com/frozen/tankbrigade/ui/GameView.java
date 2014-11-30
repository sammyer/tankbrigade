package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.SpriteAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.map.anim.UnitAttackAnimation;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.util.SparseMap;

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
	private Matrix transformMtx;
	private RectF unitRect =new RectF();
	private MapDrawer renderer;
	private GameViewListener listener;
	private MapDrawParameters drawParams =new MapDrawParameters();
	private static final float MIN_SCALE=0.2f;
	private static final float MAX_SCALE=1.5f;

	private boolean uiEnabled=true;

	private MultiTouchController<Object> multitouch=new MultiTouchController<Object>(this);

	public void setMap(GameBoard map,GameData config) {
		//renderer=new BasicMapDrawer(getContext(),config);
		renderer=new GraphicMapDrawer(getContext(),config);
		this.map=map;
		transformMtx =new Matrix();
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
		renderer.drawMap(canvas,map, transformMtx, drawParameters);
	}


	//handle touch events


	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (!uiEnabled) return false;
		if (transformMtx ==null) return true;

		if (motionEvent.getActionMasked()==MotionEvent.ACTION_DOWN&&motionEvent.getActionIndex()==0) {
			Point tilePos=renderer.getMapPosFromScreen(motionEvent.getX(),motionEvent.getY(), transformMtx,map);
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
		unitRect.set(0, 0, 1, 1);
		transformMtx.mapRect(unitRect);
		objPosAndScaleOut.set(unitRect.left, unitRect.top,true, unitRect.width(),false,0,0,false,0);
	}

	private Rect mapTranslateBounds;
	private Matrix rollbackMatrix=new Matrix();
	@Override
	public boolean setPositionAndScale(Object obj, PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		float scale=newObjPosAndScale.getScale();
		//float maxScale=getWidth()/4;
		if (scale<MIN_SCALE||scale>MAX_SCALE) return false;
		if (screenRect==null) return false;

		rollbackMatrix.set(transformMtx);
		transformMtx.reset();
		transformMtx.setScale(scale, scale);
		transformMtx.postTranslate(newObjPosAndScale.getXOff(), newObjPosAndScale.getYOff());

		//check if it board is not off-screen
		if (mapTranslateBounds==null) mapTranslateBounds=new Rect(-1,-1,map.width()+1,map.height()+1);
		RectF bounds=renderer.getScreenBounds(mapTranslateBounds);
		RectF areaShown=getAreaShown();
		if (areaShown.width()>bounds.width()&&areaShown.height()>bounds.height()) {
			transformMtx.set(rollbackMatrix);
			return false;
		}
		float adjustX=-panAdjustment(areaShown.left,areaShown.right,bounds.left,bounds.right)*scale;
		float adjustY=-panAdjustment(areaShown.top,areaShown.bottom,bounds.top,bounds.bottom)*scale;

		transformMtx.postTranslate(adjustX,adjustY);

		return true;
	}

	private float panAdjustment(float start, float end, float boundsStart, float boundsEnd) {
		float w=end-start;
		float bw=boundsEnd-boundsStart;
		if (w>bw) {
			float targetStart=boundsStart-(w-bw)*0.5f;
			return targetStart-start;
		} else if (start<boundsStart) return boundsStart-start;
		else if (end>boundsEnd) return boundsEnd-end;
		else return 0;
	}

	@Override
	public void selectObject(Object obj, PointInfo touchPoint) {
		//Log.d(TAG,"selectObject");
	}


	private RectF screenRect;
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (w==0||h==0) screenRect=null;
		else screenRect=new RectF(0,0,w,h);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	private Matrix inverseTransform=new Matrix();
	private RectF areaShown=new RectF();
	public RectF getAreaShown() {
		if (screenRect==null) return null;
		transformMtx.invert(inverseTransform);
		inverseTransform.mapRect(areaShown,screenRect);
		return areaShown;
	}

	public void setFocusRect(Rect rect,boolean union) {
		if (screenRect==null) return;
		RectF focusRect=renderer.getScreenBounds(rect);
		if (union) {
			focusRect.union(getAreaShown());
		}
		transformMtx.setRectToRect(focusRect, screenRect, Matrix.ScaleToFit.CENTER);
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

	public void setOverlay(GameUnit selectedUnit, SparseMap<UnitMove> moveMap) {
		drawParams.setMoveOverlay(new Point(selectedUnit.x,selectedUnit.y),moveMap);
	}

	public void removeOverlay() {
		drawParams.setMoveOverlay(null,null);
	}

	public void setTestMode(int testMode) {
		drawParams.testMode=testMode;
	}

	public int getTestMode() {
		return drawParams.testMode;
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
		Rect moveRect=new Rect(0,0,1,1);
		moveRect.offsetTo(move.unit.x, move.unit.y);
		Rect endRect=new Rect(0,0,1,1);
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
