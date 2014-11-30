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
import com.frozen.tankbrigade.util.MiscUtils;

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
		TextView info=(TextView)findViewById(R.id.itemDetails);
		TextView info2=(TextView)findViewById(R.id.itemDetails2);
		TextView nameView=(TextView)findViewById(R.id.itemName);
		nameView.setText(MiscUtils.capitalize(terrain.name));
		info.setText(String.format("Moves:%.1f\nDefense:%d%%",
				terrain.movement,
				Math.round(terrain.defense * 100)));
		info2.setText("");
	}

	public void setUnit(GameUnit unit) {
		if (unit==null) {
			reset();
			return;
		}
		TextView info=(TextView)findViewById(R.id.itemDetails);
		TextView info2=(TextView)findViewById(R.id.itemDetails2);
		TextView nameView=(TextView)findViewById(R.id.itemName);
		String rangeStr="";
		if (unit.type.isRanged()) rangeStr="["+unit.type.getMinRange()+"-"+unit.type.getMaxRange()+"]";
		nameView.setText(MiscUtils.capitalize(unit.type.name));
		String details=String.format("Health: %d/%d\nMoves: %d/%d",
				unit.health,unit.type.health,
				unit.movesLeft,unit.type.movement
				);
		String details2=String.format("Attack: %d%s",
				unit.type.damage,rangeStr
				);
		info.setText(details);
		info2.setText(details2);
	}

	public void updatePlayers(SparseArray<Player> players) {
		TextView playerInfo=(TextView)findViewById(R.id.playerDetails);
		String s=String.format("Player 1: $%d\nPlayer 2: $%d",
				players.get(1).money,players.get(2).money);
		playerInfo.setText(s);
	}

	public void reset() {
		TextView info=(TextView)findViewById(R.id.itemDetails);
		TextView nameView=(TextView)findViewById(R.id.itemName);
		TextView info2=(TextView)findViewById(R.id.itemDetails2);
		info.setText("");
		info2.setText("");
		nameView.setText("");
	}
}
