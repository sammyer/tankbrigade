package com.frozen.tankbrigade.ui;

import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.frozen.easyjson.JSONParser;
import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.MapDrawParameters;
import com.frozen.tankbrigade.map.UnitMove;
import com.frozen.tankbrigade.map.anim.MapAnimation;
import com.frozen.tankbrigade.map.anim.UnitAnimation;
import com.frozen.tankbrigade.map.anim.UnitAttackAnimation;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnit;
import com.frozen.tankbrigade.map.MoveSearchNode;
import com.frozen.tankbrigade.map.PathFinder;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.map.model.TerrainMap;
import com.frozen.tankbrigade.map.model.TerrainType;
import com.frozen.tankbrigade.util.FileUtils;

import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class BoardFragment extends Fragment implements GameView.GameViewListener {
	private static final String TAG="BoardFragment";

	private GameView gameBoard;
	private InfoBar infoBar;
	private Button endTurnBtn;

	private Player player;
	private Player ai;
	private GameData gameConfig;
	private PathFinder pathFinder=new PathFinder();
	private TerrainMap map;
	private MoveSearchNode[][] moveMap;
	private short[][] shadeMap;
	private GameUnit selectedUnit;
	private MoveSearchNode selectedMove;
	private UnitMove currentMove;

	public BoardFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		gameBoard =(GameView)rootView.findViewById(R.id.gameview);
		infoBar=(InfoBar)rootView.findViewById(R.id.infobar);
		endTurnBtn=(Button)rootView.findViewById(R.id.doneBtn);

		JSONObject configJson= FileUtils.readJSONFile(getActivity(), "gameconfig.json");
		gameConfig= JSONParser.parse(configJson,GameData.class);

		String[] fileContents= FileUtils.readFileLines(getActivity(), "map1.txt");
		map=new TerrainMap();
		map.parseMapFile(fileContents,gameConfig);
		shadeMap=new short[map.width()][map.height()];
		Log.d("test", "size " + map.width() + "/" + map.height());
		gameBoard.setMap(map,gameConfig);
		gameBoard.setListener(this);

		endTurnBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onEndTurn();
			}
		});

		return rootView;
	}

	@Override
	public void onTileSelected(Point tilePos) {
		GameUnit unit=map.getUnitAt(tilePos.x,tilePos.y);
		MoveSearchNode move=moveMap==null?null:moveMap[tilePos.x][tilePos.y];
		Log.i(TAG,"onTileSelected "+tilePos+" unit="+unit+" selectedUnit="+selectedUnit);
		//GameUnit selectedUnit=(mapPaths ==null?null: mapPaths.unit);
		if (selectedUnit==null) {
			if (unit==null) selectTerrainAtPos(tilePos);
			else selectUnit(unit);
		} else if (unit==selectedUnit||move==null) {
			deselectUnit();
			selectTerrainAtPos(tilePos);
		}
		else if (move==selectedMove) {
			executeMove(move);
		} else if (unit!=null&&unit.ownerId==selectedUnit.ownerId) {
			selectUnit(unit);
		} else {
			selectMove(move);
		}
	}

	private void selectUnit(GameUnit unit) {
		Log.i(TAG,"selectUnit");
		infoBar.setUnit(unit);
		selectedUnit=unit;
		selectedMove=null;
		moveMap=pathFinder.findLegalMoves(map,unit);
		MapDrawParameters.setMapOverlayFromPaths(unit,moveMap,shadeMap);
		gameBoard.clearPath();
		gameBoard.setOverlay(shadeMap);
	}
	private void deselectUnit() {
		Log.i(TAG,"deselectUnit");
		selectedUnit=null;
		selectedMove=null;
		moveMap=null;
		gameBoard.setOverlay(null);
		gameBoard.clearPath();
	}

	private void selectTerrainAtPos(Point pos) {
		selectTerrain(map.getTerrain(pos.x,pos.y));
	}

	private void selectTerrain(TerrainType terrain) {
		Log.i(TAG,"selectTerrain");
		deselectUnit();
		infoBar.setTerrain(terrain);
	}

	private void selectMove(MoveSearchNode move) {
		Log.i(TAG, "selectMove");
		selectedMove=move;
		UnitMove unitMove=move.getMove(selectedUnit,map);
		gameBoard.highlightPath(unitMove.path, unitMove.getAttackPoint());
	}

	//--------------------------------- ANIMATION ---------------------------------------

	private void executeMove(MoveSearchNode move) {
		Log.i(TAG,"executeMove");
		UnitMove unitMove=move.getMove(selectedUnit,map);
		currentMove=unitMove;
		gameBoard.setOverlay(null);
		gameBoard.clearPath();

		if (unitMove.path!=null) gameBoard.animateMove(unitMove);
		else if (unitMove.attackTarget!=null) gameBoard.animateAttack(unitMove);

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
		Point endPoint=move.getEndPoint();
		GameUnit unit=move.unit;
		unit.x=endPoint.x;
		unit.y=endPoint.y;
		unit.movesLeft-=move.movementCost;
		infoBar.setUnit(selectedUnit);
		selectedMove=null;
		selectedUnit=null;

		if (move.attackTarget!=null) gameBoard.animateAttack(move);
		else endTurnBtn.setEnabled(true);
	}

	private void onAttackComplete(UnitMove move, boolean isCounterAttack) {
		GameUnit attacker=isCounterAttack?move.attackTarget:move.unit;
		GameUnit defender=isCounterAttack?move.unit:move.attackTarget;
		boolean counterAttacking=false;

		if (!isCounterAttack) attacker.movesLeft=0;
		int damage=attacker.getDamageAgainst(defender,map.getTerrain(defender.x,defender.y));
		if (damage>=defender.health) {
			map.getUnits().remove(defender);
		} else {
			defender.health-=damage;
			if (!isCounterAttack&&defender.type.canAttack(attacker.type)) {
				counterAttacking=true;
				gameBoard.animateCounterattack(move);
			}
		}
		endTurnBtn.setEnabled(!counterAttacking);
	}

	//------------------------------------------------------------------------

	private void onEndTurn() {
		selectedUnit=null;
		selectedMove=null;
		moveMap=null;
		gameBoard.setOverlay(null);
		gameBoard.clearPath();

		for (GameUnit unit:map.getUnits()) {
			unit.movesLeft=unit.type.movement;
		}
	}

}
