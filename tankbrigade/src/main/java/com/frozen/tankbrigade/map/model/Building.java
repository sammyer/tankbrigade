package com.frozen.tankbrigade.map.model;

/**
 * Created by sam on 27/11/14.
 */
public class Building {
	public String name;
	public int x;
	public int y;
	private BuildingType type;
	private int ownerId;

	private boolean isCapturing;
	private int capturingPlayerId;
	private int captureTurns=0;

	public enum BuildingType {FACTORY,OIL,GOLD};

	public Building(String name, int x, int y, int ownerId) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.ownerId = ownerId;
		type=BuildingType.valueOf(name.toUpperCase());
	}

	public boolean isFactory() {
		return type==BuildingType.FACTORY;
	}

	public boolean isOwnedBy(int playerId) {
		return playerId==ownerId&&!isCapturing;
	}

	public void capture(int playerId) {
		if (playerId==ownerId) return;
		if (!(isCapturing&&playerId==capturingPlayerId)) {
			startCapture(playerId);
		}
		captureTurns++;
		if (captureTurns==2) {
			ownerId=capturingPlayerId;
			endCapture();
		}
	}

	private void startCapture(int playerId) {
		isCapturing=true;
		capturingPlayerId=playerId;
		captureTurns=0;
	}

	public void endCapture() {
		isCapturing=false;
		capturingPlayerId=0;
		captureTurns=0;
	}

	public int moneyGenerated() {
		if (type==BuildingType.GOLD) return 200;
		else if (type==BuildingType.OIL) return 80;
		else return 0;
	}

	public boolean isOil() {
		return type==BuildingType.GOLD||type==BuildingType.OIL;
	}

	public Building clone() {
		return new Building(name,x,y,ownerId);
	}
}
