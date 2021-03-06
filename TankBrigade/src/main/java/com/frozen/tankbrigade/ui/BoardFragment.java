package com.frozen.tankbrigade.ui;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.WinLoseActivity;
import com.frozen.tankbrigade.ai.AIMain;
import com.frozen.tankbrigade.debug.DebugTools;
import com.frozen.tankbrigade.loaders.MapLoader;
import com.frozen.tankbrigade.map.moves.UnitMove;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.map.anim.UnitAttackAnimation;
import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.GameConfig;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.paths.PathFinder;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.GameBoard;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.SparseMap;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class BoardFragment extends Fragment implements MapLoader.MapLoadListener,
		GameView.GameViewListener, FactoryDialogFragment.OnBuyListener {
	private static final String TAG="BoardFragment";

	private GameView gameBoardView;
	private InfoBar infoBar;
	private FactoryDialogFragment factoryDialog;
	private Button endTurnBtn;
	private Button testBtn;

	private AIMain ai=new AIMain();
	private AITask aiTask;
	private PathFinder pathFinder=new PathFinder();

	private GameConfig gameConfig;
	private GameBoard boardModel;
	//for saving purposes ... create a snapshop of the board at the beginning of each turn
	private GameBoard boardSnapshot;

	private List<UnitMove> moveAnimationQueue;
	private SparseMap<UnitMove> moveMap;
	private GameUnit selectedUnit;
	private UnitMove selectedMove;

	private int curPlayerId =Player.USER_ID;
	private String mapFile;
	private boolean restoreGameOnLoad=false;

	public BoardFragment() {}
	public BoardFragment(String mapFile,boolean restoreGameOnLoad) {
		this.mapFile=mapFile;
		this.restoreGameOnLoad=restoreGameOnLoad;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		gameBoardView =(GameView)rootView.findViewById(R.id.gameview);
		infoBar=(InfoBar)rootView.findViewById(R.id.infobar);
		endTurnBtn=(Button)rootView.findViewById(R.id.doneBtn);
		testBtn=(Button)rootView.findViewById(R.id.testBtn);


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

		if (restoreGameOnLoad) loadSavedGame(mapFile);
		else loadMap(mapFile);

		return rootView;
	}

	public void loadMap(String filename) {
		setUiEnabled(false);
		gameBoardView.setListener(null);
		MapLoader.loadMap(getActivity(), filename, this);
	}

	public void loadSavedGame(String filename) {
		setUiEnabled(false);
		gameBoardView.setListener(null);
		if (filename==null) MapLoader.restoreGame(getActivity(), this);
		else MapLoader.restoreGame(getActivity(),filename,this);
	}

	public void onMapLoaded(GameConfig config, GameBoard map) {
		gameConfig=config;
		if (map==null) {
			Log.w(TAG,"onMapLoaded - map is null!");
			getActivity().finish();
			return;
		}
		boardModel=map;
		boardSnapshot=map.clone();
		Log.d(TAG, "Loaded map - size " + boardModel.width() + "," + boardModel.height()+"  units="+ boardModel.getUnits().size());

		if (moveAnimationQueue!=null) moveAnimationQueue.clear();
		moveMap=null;
		selectedUnit=null;
		selectedMove=null;

		gameBoardView.setMap(boardModel, gameConfig);
		gameBoardView.setListener(this);
		infoBar.updatePlayers(boardModel.getPlayers());
		setUiEnabled(true);
	}

	public GameBoard getBoardToSave() {
		return boardSnapshot;
	}

	private DebugTools debug=new DebugTools();
	private void onToggleTest() {
		Log.d(TAG,"onTest - "+selectedUnit+" / "+selectedMove);
		if (selectedUnit!=null&&selectedUnit.ownerId==Player.AI_ID) {
			//debug.debugUnitMoves(boardModel,selectedUnit);
		}
		if (selectedMove!=null) {
			debug.debugMove(boardModel,selectedMove);
		} else if (selectedUnit!=null&&selectedUnit.ownerId==curPlayerId) {
			selectMove(debug.debugUnitMoves(boardModel,selectedUnit,false));
		} else {
			DebugTools.testMapAnalyzer.analyzeMap(boardModel,Player.USER_ID);
			gameBoardView.setTestMode(1 - gameBoardView.getTestMode());
		}
	}

	@Override
	public void onTileSelected(Point tilePos) {
		GameUnit unit= boardModel.getUnitAt(tilePos.x, tilePos.y);
		UnitMove move=moveMap==null?null:moveMap.get(tilePos.x,tilePos.y);
		Log.i(TAG,"onTileSelected "+tilePos+" unit="+unit+" selectedUnit="+selectedUnit);
		//GameUnit selectedUnit=(mapPaths ==null?null: mapPaths.unit);
		if (selectedUnit==null||selectedUnit.ownerId!= curPlayerId) {
			//nothing,terrain selected or opposite player unit selected
			deselectUnit();
			Building building=boardModel.getBuildingAt(tilePos.x,tilePos.y);
			if (unit!=null) selectUnit(unit);
			else if (building!=null) selectBuilding(building,tilePos);
			else selectTerrainAtPos(tilePos);
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

	private void selectBuilding(Building building, Point tilePos) {
		if (building.isFactory()&&building.isOwnedBy(curPlayerId)) {
			openFactoryDialog(boardModel.getPlayer(curPlayerId),building);
		} else selectTerrainAtPos(tilePos);
	}

	private void selectUnit(GameUnit unit) {
		Log.i(TAG,"selectUnit");
		infoBar.setUnit(unit);
		selectedUnit=unit;
		if (unit.ownerId==curPlayerId&&unit.movesLeft>0) {
			selectedMove=null;
			moveMap=pathFinder.findLegalMoves(boardModel,unit);
			gameBoardView.clearPath();
			gameBoardView.setOverlay(unit, moveMap);
		}
	}
	private void deselectUnit() {
		Log.i(TAG,"deselectUnit");
		if (selectedUnit!=null&&selectedUnit.ownerId== curPlayerId) {
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

	//--------------------------------- BUILDINGS ---------------------------------------

	private void openFactoryDialog(Player player,Building building) {
		if (factoryDialog==null) {
			factoryDialog=new FactoryDialogFragment();
			factoryDialog.setGameConfig(gameConfig);
			factoryDialog.setOnBuyListener(this);
		}

		factoryDialog.openDialog(player, building, getActivity().getFragmentManager());
	}

	@Override
	public void onBuyUnit(Player player, Building factory, GameUnitType unitType) {
		player.money-=unitType.price;
		GameUnit newUnit=new GameUnit(unitType,factory.x,factory.y,player.id);
		newUnit.movesLeft=0;
		boardModel.addUnit(newUnit);
		infoBar.updatePlayers(boardModel.getPlayers());
		gameBoardView.invalidate();
	}

	private void captureBuildings(int playerId) {
		for (Building building:boardModel.getBuildings()) {
			if (building.isOwnedBy(playerId)) continue;
			GameUnit unit=boardModel.getUnitAt(building.x,building.y);
			if (unit==null) building.endCapture();
			else if (unit.ownerId==playerId&&unit.type.canCaptureBuildings()) {
				building.capture(unit.ownerId);
			}
			//air units cannot capture
		}
	}


	private void collectMoney(int playerId) {
		Player player=boardModel.getPlayer(playerId);
		if (player==null) return;
		for (Building building: boardModel.getBuildings()) {
			if (building.isOwnedBy(playerId)) {
				player.money+=building.moneyGenerated();
			}
		}
		infoBar.updatePlayers(boardModel.getPlayers());
	}

	//--------------------------------- ANIMATION ---------------------------------------

	private boolean uiEnabled=true;
	protected void setUiEnabled(boolean enabled) {
		uiEnabled=enabled;
		endTurnBtn.setEnabled(enabled);
	}

	protected boolean isUiEnabled() {
		return uiEnabled;
	}

	public void executeMove(UnitMove unitMove) {
		Log.i(TAG,"executeMove");
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

		selectedMove=null;
		selectedUnit=null;
		moveMap=null;
		setUiEnabled(false);
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
		Log.d(TAG,"onMoveExecuted - owner="+move.unit.ownerId+"  queue="+(moveAnimationQueue==null?null:moveAnimationQueue.size()));
		infoBar.setUnit(move.unit); //update infobar
		if (moveAnimationQueue==null||moveAnimationQueue.isEmpty()) {
			setUiEnabled(true);
			if (move.unit.ownerId==Player.AI_ID) onAiTurnFinished();
		} else {
			executeMove(moveAnimationQueue.remove(0));
		}
	}

	//------------------------------------------------------------------------

	private void onEndTurn() {
		Log.d(TAG,"onEndTurn");
		selectedUnit=null;
		selectedMove=null;
		moveMap=null;
		gameBoardView.removeOverlay();
		gameBoardView.clearPath();
		setUiEnabled(false);

		for (GameUnit unit: boardModel.getUnits()) {
			unit.startNewTurn();
		}
		captureBuildings(curPlayerId);
		collectMoney(curPlayerId);

		if (!checkWinCondition()) {
			aiTask=new AITask();
			aiTask.execute();
		}
	}

	private void onAiDone(List<UnitMove> moves) {
		moveAnimationQueue=moves;
		if (moveAnimationQueue==null||moveAnimationQueue.isEmpty()) return;
		executeMove(moveAnimationQueue.remove(0));
	}

	private void onAiTurnFinished() {
		Log.d(TAG,"onAiTurnFinished");
		captureBuildings(Player.AI_ID);
		collectMoney(Player.AI_ID);
		checkWinCondition();
		boardSnapshot=boardModel.clone();
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

	private boolean checkWinCondition() {
		Log.d(TAG,"checkWinCondition");
		int winner=boardModel.getWinner();
		if (winner==Player.NONE) return false;
		Intent intent=new Intent(getActivity(), WinLoseActivity.class);
		intent.putExtra(WinLoseActivity.EXTRA_OUTCOME,winner==curPlayerId);
		startActivity(intent);
		return true;
	}

}
