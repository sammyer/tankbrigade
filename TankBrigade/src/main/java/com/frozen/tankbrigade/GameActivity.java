package com.frozen.tankbrigade;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.frozen.tankbrigade.ui.BoardFragment;

public class GameActivity extends ActionBarActivity {
	public static final String EXTRA_MAPFILE="EXTRA_MAPFILE";

	private BoardFragment gameFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
			String mapFile=getIntent().getStringExtra(EXTRA_MAPFILE);
			if (mapFile==null) gameFragment=new BoardFragment();
			else gameFragment=new BoardFragment(mapFile);
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

}
