package com.frozen.tankbrigade.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.model.GameUnit;
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
		String defenseStr=Integer.toString(Math.round(terrain.defense*100));
		info.setText("Terrain "+terrain.name+"  move:1  defense:"+defenseStr);
	}

	public void setUnit(GameUnit unit) {
		if (unit==null) {
			reset();
			return;
		}
		TextView info=(TextView)findViewById(R.id.item_info);
		String rangeStr="";
		if (unit.type.isRanged()) rangeStr="["+unit.type.getMinRange()+"-"+unit.type.getMaxRange()+"]";
		info.setText("Unit: "+unit.type.name+
				"  health="+unit.health+"/"+unit.type.health+
				"  moves="+unit.movesLeft+"/"+unit.type.movement+
				"  attack="+unit.type.damage+rangeStr);
	}

	public void reset() {
		TextView info=(TextView)findViewById(R.id.item_info);
		info.setText("");
	}
}
