package com.frozen.tankbrigade.map;

/**
 * Created by sam on 12/01/14.
 */
public class GameUnit {
	public GameUnitType type;
	public int x;
	public int y;
	public int ownerId;
	public int health;

	public GameUnit(GameUnitType type, int x, int y, int ownerId) {
		this.x=x;
		this.y=y;
		this.type = type;
		this.ownerId = ownerId;
		health=type.health;
	}

	public String toString() {
		return "[GameUnit pos="+x+","+y+" type="+type.name+" player="+ownerId+"]";
	}
}
