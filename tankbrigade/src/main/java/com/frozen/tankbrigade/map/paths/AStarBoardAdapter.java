package com.frozen.tankbrigade.map.paths;

import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.TerrainType;

/**
 * Created by sam on 09/03/14.
 */
public class AStarBoardAdapter implements AStarMap {
	private GameBoard board;
	private GameUnit unit;
	private int w;
	private int h;
	private boolean ignoreMovesLeft=false;
	private boolean ignoreEnemyUnits=false;

	public AStarBoardAdapter(GameBoard board, GameUnit unit) {
		this.board = board;
		this.unit = unit;
		w=board.width();
		h=board.height();
	}

	//for CostAnalyzer AI, we want to assume unit has all it's moves left
	//so this would be set to true
	public void setIgnoreMovesLeft(boolean b) {
		ignoreMovesLeft=b;
	}
	//for MapAnalyzer AI, in order to find direction towards enemy,
	//it is necessary to ignore other units on the board
	public void setIgnoreEnemyUnits(boolean b) {ignoreEnemyUnits=b;}

	@Override
	public boolean canMoveHere(int x, int y) {
		if (!board.isInBounds(x,y)) return false;
		TerrainType terrain=board.getTerrain(x,y);
		GameUnit mapUnit=board.getUnitAt(x,y);

		if (mapUnit!=null&&mapUnit.ownerId!=unit.ownerId&&!ignoreEnemyUnits) {
			return false;
		}
		if (!isTraversable(terrain, unit)) {
			return false;
		}
		return true;
	}

	@Override
	public int getCost(int x, int y) {
		if (unit.type.isAir()) return 1;
		else {
			TerrainType terrain=board.getTerrain(x,y);
			return (int)Math.floor(terrain.movement);
		}
	}

	@Override
	public int getMaxCost() {
		if (ignoreMovesLeft) return unit.type.movement;
		else return unit.movesLeft;
	}


	private boolean isTraversable(TerrainType terrain, GameUnit unit) {
		if (unit.type.isLand()&&!terrain.isLand()) return false;
		if (unit.type.isWater()&&!terrain.isWater()) return false;
		if (unit.type.isTank()&&terrain.symbol==TerrainType.MOUNTAIN) return false;
		return true;
	}
}
