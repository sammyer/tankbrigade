package com.frozen.tankbrigade.ui;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.model.GameUnitType;

/**
 * Created by sam on 29/11/14.
 */
public class DrawableMapping {
	public static int getUnitDrawable(GameUnitType unitType) {
		switch (unitType.symbol) {
			case GameUnitType.COMMANDO: return R.drawable.commando;
			case GameUnitType.BAZOOKA: return R.drawable.bazooka;
			case GameUnitType.FLAK: return R.drawable.flak;
			case GameUnitType.TANK: return R.drawable.tank;
			case GameUnitType.ROCKET: return R.drawable.rocket;
			case GameUnitType.AIRPLANE: return R.drawable.fighter;
			case GameUnitType.GOLIATH: return R.drawable.bigtank;
			case GameUnitType.MORTAR: return R.drawable.mortar;
			case GameUnitType.BOMBER: return R.drawable.bomber;
		}
		//default
		return R.drawable.commando;
	}
}
