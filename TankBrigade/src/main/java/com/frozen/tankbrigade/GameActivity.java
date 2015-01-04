package com.frozen.tankbrigade;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.frozen.tankbrigade.loaders.MapLoader;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.ui.BoardFragment;

public class GameActivity extends ActionBarActivity {
	public static final String TAG="GameActivity";
	public static final String EXTRA_MAPFILE="EXTRA_MAPFILE";
	public static final String EXTRA_RESTORE_GAME="RESTORE_GAME";

	private BoardFragment gameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		gameFragment=(BoardFragment)getSupportFragmentManager().findFragmentById(R.id.container);
		Log.d(TAG,"onCreate - gameFragment?="+(gameFragment!=null)+" savedInstanceState?="+(savedInstanceState!=null));

		if (gameFragment == null) {
			boolean restoreGame=getIntent().getBooleanExtra(EXTRA_RESTORE_GAME,false);
			String mapFile=getIntent().getStringExtra(EXTRA_MAPFILE);
			Log.d(TAG,"onCreate - restore?="+restoreGame+"  mapFile="+mapFile);
			if (restoreGame) gameFragment=new BoardFragment(null,true);
			else if (mapFile==null) gameFragment=new BoardFragment();
			else gameFragment=new BoardFragment(mapFile,false);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, gameFragment)
                    .commit();

		}
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		super.onStop();
		GameBoard board=gameFragment.getBoardToSave();
		if (board!=null) MapLoader.saveGame(this, board, null);
	}
}
