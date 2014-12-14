package com.frozen.tankbrigade.debug;

import android.util.Log;

import com.frozen.tankbrigade.ai.CostAnalyzer;
import com.frozen.tankbrigade.ai.MapAnalyzer;
import com.frozen.tankbrigade.map.moves.DamageInfo;
import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.util.SparseMap;

/**
 * Created by sam on 04/12/14.
 */
public class DebugTools {
	public static MapAnalyzer testMapAnalyzer=new MapAnalyzer();
	private CostAnalyzer costAnalyzer;
	private PathFinder pathFinder=new PathFinder();
	private static final String debugTag="AI_DEBUG";

	public DebugTools() {
	}



	//debugging moves:
	public UnitMove debugUnitMoves(GameBoard board,GameUnit unit,boolean doLog) {
		costAnalyzer=new CostAnalyzer(pathFinder, board,unit.ownerId);
		SparseMap<UnitMove> moveMap=pathFinder.findLegalMoves(board,unit);
		if (doLog) Log.i(debugTag, "Debugging unit " + unit.toString());
		float highestScore=0;
		UnitMove bestMove=null;
		for (UnitMove move:moveMap.getAllNodes()) {
			if (doLog) Log.d(debugTag,logMove(move));
			float score=costAnalyzer.getScore(move);
			if (score>highestScore||bestMove==null) {
				bestMove=move;
				highestScore=score;
			}
		}
		if (doLog) Log.i(debugTag,"------------------------------------------");
		return bestMove;
	}

	public void debugMove(GameBoard board, UnitMove move) {
		costAnalyzer=new CostAnalyzer(pathFinder, board,move.unit.ownerId);
		Log.d(debugTag,logMove(move));
	}

	private String logMove(UnitMove move) {
		costAnalyzer.DEBUG=true;
		DamageInfo damageDone=costAnalyzer.getDamageDone(move);
		DamageInfo damageTaken=costAnalyzer.getDamageTaken(move);
		int buildingCaptureScore=costAnalyzer.getBuildingCaptureScore(move,damageDone.isKill,damageTaken.isKill);
		float moveBonus=costAnalyzer.getMoveTowardsEnemyBonus(move);
		costAnalyzer.DEBUG=false;
		return String.format("score=%.2f  attack=%s defense=%s building=%d movebonus=%.1f move=%s",costAnalyzer.getScore(move),
				damageDone,damageTaken,buildingCaptureScore,moveBonus,move.toString());
	}

}
