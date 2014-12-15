package com.frozen.tankbrigade.map.model;

/**
 * Created by sam on 08/02/14.
 */
public class Player {
	public static int USER_ID=1;
	public static int AI_ID=2;
	public static int NONE =0;

	public int id;
	public int money;

	public Player(int id, int startingMoney) {
		this.id=id;
		this.money=startingMoney;
	}

	//returns 1 if is same player, -1 if different players, or 0 if comparing to neutral
	public static int compare(int playerId1, int playerId2) {
		if (playerId1==Player.NONE||playerId2==Player.NONE) return 0;
		else if (playerId1==playerId2) return 1;
		else return -1;
	}


}
