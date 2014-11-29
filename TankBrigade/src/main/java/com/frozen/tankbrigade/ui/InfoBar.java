package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainType;

/**
 * Created by sam on 30/01/14.
 */
public class InfoBar extends RelativeLayout {
	public InfoBar(Context context) {
		this(context, null, 0);
	}

	public InfoBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public InfoBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTerrain(TerrainType terrain) {
		if (terrain==null) {
			reset();
			return;
		}
		TextView info=(TextView)findViewById(R.id.item_info);
		TextView nameView=(TextView)findViewById(R.id.item_name);
		nameView.setText("Terrain:\n"+terrain.name);
		info.setText(String.format("move:%.1f\ndefense:%d%%",
				terrain.movement,
				Math.round(terrain.defense*100)));
	}

	public void setUnit(GameUnit unit) {
		if (unit==null) {
			reset();
			return;
		}
		TextView info=(TextView)findViewById(R.id.item_info);
		TextView nameView=(TextView)findViewById(R.id.item_name);
		String rangeStr="";
		if (unit.type.isRanged()) rangeStr="["+unit.type.getMinRange()+"-"+unit.type.getMaxRange()+"]";
		nameView.setText("Unit:\n"+unit.type.name);
		info.setText("health="+unit.health+"/"+unit.type.health+
				"\nmoves="+unit.movesLeft+"/"+unit.type.movement+
				"\nattack="+unit.type.damage+rangeStr);
	}

	public void updatePlayers(SparseArray<Player> players) {
		TextView playerInfo=(TextView)findViewById(R.id.player_info);
		String s=String.format("Player 1  - money = $%d\nPlayer 2 - money = $%d",
				players.get(1).money,players.get(2).money);
		playerInfo.setText(s);
	}

	public void reset() {
		TextView info=(TextView)findViewById(R.id.item_info);
		info.setText("");
	}
}
