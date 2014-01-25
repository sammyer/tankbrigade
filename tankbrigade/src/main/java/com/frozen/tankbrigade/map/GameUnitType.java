package com.frozen.tankbrigade.map;

/**
 * Created by sam on 12/01/14.
 */
public class GameUnitType {
	public static final int COMMANDO=0;
	public static final int BAZOOKA=1;
	public static final int FLAK=2;
	public static final int TANK=3;
	public static final int ROCKET=4;
	public static final int AIRPLANE=5;

	public enum MoveMode {LAND,AIR,WATER};

	public enum ArmorType {LIGHT,MEDIUM,HEAVY};

	public int type;
	public int damage;
	public int health;
	public int movement;
	public int range=1;
	public int minRange=1;
	public MoveMode mode=MoveMode.LAND;
	public ArmorType attackType=ArmorType.MEDIUM;
	public ArmorType defenseType=ArmorType.MEDIUM;
	public String name;

	public GameUnitType(int type, int damage, int health, int movement) {
		this.type=type;
		this.damage = damage;
		this.health = health;
		this.movement = movement;
	}

	public GameUnitType(int type, int damage, int health, int movement,
			ArmorType defenseType, ArmorType attackType) {
		this.type=type;
		this.damage = damage;
		this.health = health;
		this.movement = movement;
		this.attackType = attackType;
		this.defenseType = defenseType;
	}

	public static GameUnitType[] getUnitTypes() {
		GameUnitType[] types=new GameUnitType[6];
		types[0]=new GameUnitType(COMMANDO,22,50,3,ArmorType.LIGHT,ArmorType.LIGHT);
		types[1]=new GameUnitType(BAZOOKA,30,50,3,ArmorType.LIGHT,ArmorType.HEAVY);
		types[2]=new GameUnitType(FLAK,17,70,5,ArmorType.HEAVY,ArmorType.LIGHT);
		types[3]=new GameUnitType(TANK,30,50,3,ArmorType.HEAVY,ArmorType.HEAVY);
		types[4]=new GameUnitType(ROCKET,40,40,4,ArmorType.LIGHT,ArmorType.HEAVY);
		types[4].range=5;
		types[4].minRange=3;
		types[5]=new GameUnitType(AIRPLANE,30,50,7,ArmorType.LIGHT,ArmorType.LIGHT);
		types[5].mode=MoveMode.AIR;

		return types;
	}

	public boolean isTank() {
		return (type==FLAK||type==TANK||type==ROCKET);
	}
}
