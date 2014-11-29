package com.frozen.tankbrigade.map.model;

/**
 * Created by sam on 08/02/14.
 */
public class Player {
	public static int USER_ID=1;
	public static int AI_ID=2;
	public static int NEUTRAL=0;

	public int id;
	public int money;

	public Player(int id, int startingMoney) {
		this.id=id;
		this.money=startingMoney;
	}
}
