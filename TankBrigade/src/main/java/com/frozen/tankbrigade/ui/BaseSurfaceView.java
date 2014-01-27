package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by sam on 04/01/14.
 */
public abstract class BaseSurfaceView extends SurfaceView implements Runnable,SurfaceHolder.Callback {
	public static final String TAG="BaseSurfaceView";
	private Thread thread;
	private SurfaceHolder holder;
	private volatile boolean running=false;

	public BaseSurfaceView(Context context) {
		super(context);
		init();
	}

	public BaseSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BaseSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		holder=getHolder();
		holder.addCallback(this);
	}


	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.i(TAG,"surfaceCreated");
		thread=new Thread(this);
		running=true;
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
		Log.i(TAG,"surfaceChanged");
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		Log.i(TAG,"surfaceDestroyed");
		boolean retry = true;
		running = false;
		while(retry){
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void run() {
		Log.i(TAG,"run");
		while (running) {
			if(holder.getSurface().isValid()){
				try {
					Canvas canvas = holder.lockCanvas();
					if (canvas!=null) drawSurface(canvas);
					holder.unlockCanvasAndPost(canvas);
				} catch (Exception e) {
					Log.w(TAG,"Could not draw surface - "+e.toString());
				}
			}
		}
	}

	protected abstract void drawSurface(Canvas canvas);
}
