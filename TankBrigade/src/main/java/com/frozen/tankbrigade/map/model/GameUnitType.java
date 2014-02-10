package com.frozen.tankbrigade.map.model;

import com.frozen.easyjson.JsonProperty;

/**
 * Created by sam on 12/01/14.
 */
public class GameUnitType {
	public static final char COMMANDO='C';
	public static final char BAZOOKA='B';
	public static final char FLAK='F';
	public static final char TANK='T';
	public static final char ROCKET='R';
	public static final char AIRPLANE='A';

	private enum MoveMode {foot,tracked,air,water};

	private static final int LIGHT=1;
	private static final int MEDIUM=2;
	private static final int HEAVY=3;

	@JsonProperty
	public char symbol;
	@JsonProperty
	public String name;
	@JsonProperty
	public int damage;
	@JsonProperty
	public int health;
	@JsonProperty
	public int movement;
	@JsonProperty
	private int attackType=MEDIUM;
	@JsonProperty
	private int defenseType=MEDIUM;
	@JsonProperty
	private int[] range;
	@JsonProperty
	private String mode;
	@JsonProperty
	public boolean canAttackAir=false;

	public GameUnitType() {
	}

	public boolean canAttack(GameUnitType defender) {
		//TODO: fill this in
		if (defender.isAir()&&!canAttackAir) return false;
		return true;
	}

	public float getDamageMultiplier(GameUnitType defender) {
		if (attackType==MEDIUM||defenseType==MEDIUM) return 1;
		if (attackType==defenseType) return 2;
		else return 0.5f;
	}

	public boolean isTank() {
		return mode.equals("tracked");
	}

	public boolean isLand() {
		return mode.equals("tracked")||mode.equals("foot");
	}

	public boolean isWater() {
		return mode.equals("water");
	}

	public boolean isAir() {
		return mode.equals("air");
	}

	public boolean isRanged() {
		return range!=null;
	}

	public int getMinRange() {
		if (range==null||range.length==0) return 1;
		else return range[0];
	}

	public int getMaxRange() {
		if (range==null||range.length==0) return 1;
		else if (range.length==1) return range[0];
		else return range[1];
	}
}
