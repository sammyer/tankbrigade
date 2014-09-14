package com.frozen.tankbrigade.ui;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.frozen.easyjson.JSONParser;
import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.ai.AIMain;
import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.map.anim.UnitAttackAnimation;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.FileUtils;
import com.frozen.tankbrigade.util.SparseMap;

import org.json.JSONObject;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class BoardFragment extends Fragment implements
		GameView.GameViewListener {
	private static final String TAG="BoardFragment";

	private GameView gameBoardView;
	private InfoBar infoBar;
	private Button endTurnBtn;
	private Button testBtn;

	private AIMain ai=new AIMain();
	private AITask aiTask;
	private List<UnitMove> moveAnimationQueue;
	private GameData gameConfig;
	private PathFinder pathFinder=new PathFinder();
	private GameBoard boardModel;
	private SparseMap<UnitMove> moveMap;
	private short[][] shadeMap;
	private GameUnit selectedUnit;
	private UnitMove selectedMove;
	private UnitMove currentMove;
	private int curPlayer=Player.USER_ID;

	public BoardFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		gameBoardView =(GameView)rootView.findViewById(R.id.gameview);
		infoBar=(InfoBar)rootView.findViewById(R.id.infobar);
		endTurnBtn=(Button)rootView.findViewById(R.id.doneBtn);
		testBtn=(Button)rootView.findViewById(R.id.testBtn);

		JSONObject configJson= FileUtils.readJSONFile(getActivity(), "gameconfig.json");
		Log.d(TAG,"gameconfig="+configJson.toString());
		gameConfig= JSONParser.parse(configJson,GameData.class);

		String[] fileContents= FileUtils.readFileLines(getActivity(), "map1.txt");
		boardModel =new GameBoard();
		boardModel.parseMapFile(fileContents, gameConfig);
		shadeMap=new short[boardModel.width()][boardModel.height()];
		Log.d("test", "size " + boardModel.width() + "," + boardModel.height()+"  units="+ boardModel.getUnits().size());
		gameBoardView.setMap(boardModel, gameConfig);
		gameBoardView.setListener(this);

		endTurnBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onEndTurn();
			}
		});
		testBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onToggleTest();
			}
		});

		return rootView;
	}

	private void onToggleTest() {
		Log.d(TAG,"onTest - "+selectedUnit+" / "+Player.AI_ID);
		if (selectedUnit!=null&&selectedUnit.ownerId==Player.AI_ID) {
			ai.debugUnitMoves(boardModel,selectedUnit);
		}
		//gameBoardView.setTestMode(1 - gameBoardView.getTestMode());
	}

	@Override
	public void onTileSelected(Point tilePos) {
		GameUnit unit= boardModel.getUnitAt(tilePos.x,tilePos.y);
		UnitMove move=moveMap==null?null:moveMap.get(tilePos.x,tilePos.y);
		Log.i(TAG,"onTileSelected "+tilePos+" unit="+unit+" selectedUnit="+selectedUnit);
		//GameUnit selectedUnit=(mapPaths ==null?null: mapPaths.unit);
		if (selectedUnit==null||selectedUnit.ownerId!=curPlayer) {
			//nothing,terrain selected or opposite player unit selected
			deselectUnit();
			if (unit==null) selectTerrainAtPos(tilePos);
			else selectUnit(unit);
		} else if (selectedUnit==unit) {
			//clicking on same unit deselects
			deselectUnit();
			selectTerrainAtPos(tilePos);
		} else if (move==null) {
			//invalid move selected - deselect
			deselectUnit();
			selectTerrainAtPos(tilePos);
		} else if (move==selectedMove) {
			//confirm move
			executeMove(move);
		} else if (unit!=null&&unit.ownerId==selectedUnit.ownerId) {
			//while move active, select a friendly unit -> highlight that unit
			selectUnit(unit);
		} else {
			//select another move
			selectMove(move);
		}
	}

	private void selectUnit(GameUnit unit) {
		Log.i(TAG,"selectUnit");
		infoBar.setUnit(unit);
		selectedUnit=unit;
		if (unit.ownerId==curPlayer) {
			selectedMove=null;
			moveMap=pathFinder.findLegalMoves(boardModel,unit);
			gameBoardView.clearPath();
			gameBoardView.setOverlay(unit, moveMap);
		}
	}
	private void deselectUnit() {
		Log.i(TAG,"deselectUnit");
		if (selectedUnit!=null&&selectedUnit.ownerId==curPlayer) {
			selectedMove=null;
			moveMap=null;
			gameBoardView.removeOverlay();
			gameBoardView.clearPath();
		}
		selectedUnit=null;
	}

	private void selectTerrainAtPos(Point pos) {
		selectTerrain(boardModel.getTerrain(pos.x, pos.y));
	}

	private void selectTerrain(TerrainType terrain) {
		Log.i(TAG,"selectTerrain");
		deselectUnit();
		infoBar.setTerrain(terrain);
	}

	private void selectMove(UnitMove move) {
		Log.i(TAG, "selectMove");
		selectedMove=move;
		gameBoardView.highlightPath(move.getPath(), move.getAttackPoint());
	}

	//--------------------------------- ANIMATION ---------------------------------------

	public void executeMove(UnitMove unitMove) {
		Log.i(TAG,"executeMove");
		currentMove=unitMove;
		gameBoardView.removeOverlay();
		gameBoardView.clearPath();

		gameBoardView.focusOnMove(unitMove);
		if (unitMove.hasMove()) {
			GameUnit unit=unitMove.unit;
			Point endPoint=unitMove.getEndPoint();
			unit.x=endPoint.x;
			unit.y=endPoint.y;
			unit.movesLeft-=unitMove.movementCost;
			gameBoardView.animateMove(unitMove);
		}
		else if (unitMove.isAttack()) gameBoardView.animateAttack(unitMove);
		else onMoveExecuted(unitMove);

		endTurnBtn.setEnabled(false);
	}

	@Override
	public void onAnimationComplete(final MapAnimation animation, boolean allComplete) {
		if (animation instanceof UnitAnimation) {
			onMoveComplete(((UnitAnimation) animation).getMove());
		} else if (animation instanceof UnitAttackAnimation) {
			UnitAttackAnimation anim=(UnitAttackAnimation)animation;
			onAttackComplete(anim.getMove(),anim.isCounterAttack);
		}
	}

	private void onMoveComplete(UnitMove move) {
		infoBar.setUnit(selectedUnit);
		selectedMove=null;
		selectedUnit=null;

		if (move.attackTarget!=null) gameBoardView.animateAttack(move);
		else onMoveExecuted(move);
	}

	private void onAttackComplete(UnitMove move, boolean isCounterAttack) {
		GameUnit attacker=isCounterAttack?move.attackTarget:move.unit;
		GameUnit defender=isCounterAttack?move.unit:move.attackTarget;
		boolean counterAttacking=false;

		if (!isCounterAttack) {
			attacker.setAttackUsed();
			attacker.movesLeft=0;
		}
		int damage=attacker.getDamageAgainst(defender, boardModel.getTerrain(defender.x, defender.y));
		if (damage>=defender.health) {
			boardModel.getUnits().remove(defender);
		} else {
			defender.health-=damage;
			if (!isCounterAttack&&defender.canAttackFromCurrentPos(attacker)) {
				counterAttacking=true;
				gameBoardView.animateCounterattack(move);
			}
		}
		if (!counterAttacking) onMoveExecuted(move);
	}

	private void onMoveExecuted(UnitMove move) {
		if (moveAnimationQueue==null||moveAnimationQueue.isEmpty()) {
			endTurnBtn.setEnabled(true);
		} else {
			executeMove(moveAnimationQueue.remove(0));
		}
	}

	//------------------------------------------------------------------------

	private void onEndTurn() {
		selectedUnit=null;
		selectedMove=null;
		moveMap=null;
		gameBoardView.removeOverlay();
		gameBoardView.clearPath();
		endTurnBtn.setEnabled(false);

		for (GameUnit unit: boardModel.getUnits()) {
			unit.startNewTurn();
		}

		aiTask=new AITask();
		aiTask.execute();
	}

	private void onAiDone(List<UnitMove> moves) {
		moveAnimationQueue=moves;
		if (moveAnimationQueue==null||moveAnimationQueue.isEmpty()) return;
		executeMove(moveAnimationQueue.remove(0));
	}


	private class AITask extends AsyncTask<Void,Void,List<UnitMove>> {


		@Override
		protected List<UnitMove> doInBackground(Void... voids) {
			return ai.findMoves(boardModel,Player.AI_ID);
		}

		@Override
		protected void onPostExecute(List<UnitMove> result) {
			onAiDone(result);
		}
	}



}
