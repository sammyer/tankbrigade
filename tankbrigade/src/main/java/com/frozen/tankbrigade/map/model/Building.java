package com.frozen.tankbrigade.map.model;

import android.util.Log;

/**
 * Created by sam on 27/11/14.
 */
public class Building implements Ordered2D {
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

	public int getOwnerId() {
		if (isCapturing) return Player.NONE;
		else return ownerId;
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
		//HACK: this is for graphic drawing - for now, use the same graphic for gold and oil
		return type==BuildingType.GOLD||type==BuildingType.OIL;
	}

	//------ AI stuff --
	//assign a value to the building for AI purposes
	public int getAIValue() {
		if (type==BuildingType.GOLD) return 400;
		else if (type==BuildingType.OIL) return 160;
		else return 400;
	}

	private static final float CAPTURE_PER_TURN=0.5f;
	public float getOwnershipAmt(int playerId) {
		int owner;
		float amt=1;
		if (isCapturing) {
			owner=capturingPlayerId;
			amt=captureTurns*CAPTURE_PER_TURN;
		} else {
			owner=ownerId;
			amt=1;
		}
		return Player.compare(playerId,owner)*amt;
	}

	public float getOwnershipAmtIfOccupied(int playerId, int occupyingId) {
		int owner;
		float amt=1;
		if (occupyingId==ownerId||occupyingId==Player.NONE) {
			//Log.d("I_DEBUG","getOwnership - no change - owner="+ownerId);
			owner=ownerId;
			amt=1;
		}
		else {
			owner=occupyingId;
			//Log.d("I_DEBUG","getOwnership - changing - owner="+ownerId+" isCapturing="+isCapturing+" capturer="+capturingPlayerId+" turns="+captureTurns);
			if (isCapturing&&capturingPlayerId==occupyingId) {
				amt=(captureTurns+1)*CAPTURE_PER_TURN;
			}
			else amt=CAPTURE_PER_TURN;
		}
		//Log.i("I_DEBUG","getOwnership - player="+playerId+"  owner="+owner+"  amt="+amt+"  compare="+Player.compare(playerId,owner));
		return Player.compare(playerId,owner)*amt;
	}
	//-------------

	@Override
	public int getOrderX() {
		return x;
	}

	@Override
	public int getOrderY() {
		return y;
	}

	public Building clone() {
		return new Building(name,x,y,ownerId);
	}

	@Override
	public String toString() {
		return String.format("[Building %s %d,%d]",name,x,y);
	}
}
