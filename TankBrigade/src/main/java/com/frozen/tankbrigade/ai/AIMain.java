package com.frozen.tankbrigade.ai;

import android.graphics.Point;
import android.util.Log;

import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sam on 16/02/14.
 */
public class AIMain {
	private List<UnitMove> moves=new ArrayList<UnitMove>();
	private PathFinder pathFinder=new PathFinder();
	private CostAnalyzer costAnalyzer;
	private GameBoard gameBoard;
	private static final String TAG="AIMain";

	public List<UnitMove> findMoves(GameBoard originalBoard, int playerId) {
		gameBoard=originalBoard.clone();
		Log.d(TAG,"initing cost analyzer");
		costAnalyzer=new CostAnalyzer(pathFinder, gameBoard,playerId);
		moves.clear();
		List<GameUnit> units=new ArrayList<GameUnit>();
		for (GameUnit unit:gameBoard.getUnits()) {
			if (unit.ownerId==playerId) units.add(unit);
		}
		for (GameUnit unit:units) {
			UnitMove move=findMoveForUnit(unit);
			Log.d(TAG,"found move for unit -- "+move);
			if (move!=null) {
				applyMove(move,gameBoard);
				moves.add(moveToGameBoard(move,originalBoard));
			}
		}
		Log.i(TAG,"AI done");
		return moves;
	}

	private UnitMove findMoveForUnit(GameUnit unit) {
		SparseMap<UnitMove> moveMap=pathFinder.findLegalMoves(gameBoard,unit);
		float highestScore=0;
		UnitMove bestMove=null;
		for (UnitMove move:moveMap.getAllNodes()) {
			float score=costAnalyzer.getScore(move);
			if (score>=highestScore) {
				highestScore=score;
				bestMove=move;
			}
		}
		return bestMove;
	}

	private void applyMove(UnitMove move,GameBoard board) {
		Point moveTarget=move.getEndPoint();
		GameUnit unit=move.unit;
		if (moveTarget!=null) {
			unit.x=moveTarget.x;
			unit.y=moveTarget.y;
			unit.movesLeft-=move.movementCost;
		}
		if (move.attackTarget!=null) {
			GameUnit target=move.attackTarget;
			TerrainType terrain=board.terrainMap.getTerrain(target.x,target.y);
			float damage=unit.getDamageAgainst(target,terrain);
			if (damage>=target.health) {
				target.health=0;
				board.gameUnits.removeUnit(target);
			} else {
				target.health-=damage;
				if (move.attackTarget.canAttackFromCurrentPos(unit)) {
					terrain=board.terrainMap.getTerrain(unit.x,unit.y);
					damage=target.getDamageAgainst(unit,terrain);
					if (damage>=unit.health) {
						unit.health=0;
						board.gameUnits.removeUnit(unit);
					} else unit.health-=damage;
				}
			}
		}
	}

	//create duplicate of move on original game board which can be applied in the game logic
	//since AI works on a theoretical game board
	private UnitMove moveToGameBoard(UnitMove move, GameBoard board) {
		GameUnit unit=move.unit.getOriginalUnit();
		GameUnit target;
		if (move.attackTarget==null) target=null;
		else target=move.attackTarget.getOriginalUnit();
		return new UnitMove(unit,move.getPath(),move.movementCost,target);
	}

}
