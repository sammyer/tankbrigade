package com.frozen.tankbrigade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.frozen.tankbrigade.loaders.MapLoader;

/**
 * Created by sam on 02/12/14.
 */
public class MenuActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		findViewById(R.id.map1Btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startGame("map1.txt");
			}
		});
		findViewById(R.id.map2Btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startGame("map2.txt");
			}
		});
		findViewById(R.id.map3Btn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startGame("map3.txt");
			}
		});
		View resumeBtn=findViewById(R.id.resumeBtn);
		resumeBtn.setEnabled(MapLoader.hasSavedGame(this));
		resumeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				resumeGame();
			}
		});
	}

	private void startGame(String mapFile) {
		Intent intent=new Intent(this,GameActivity.class);
		intent.putExtra(GameActivity.EXTRA_MAPFILE,mapFile);
		startActivity(intent);
	}

	private void resumeGame() {
		Intent intent=new Intent(this,GameActivity.class);
		intent.putExtra(GameActivity.EXTRA_RESTORE_GAME,true);
		startActivity(intent);
	}
}
