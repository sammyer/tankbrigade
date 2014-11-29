package com.frozen.tankbrigade.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.frozen.tankbrigade.R;
import com.frozen.tankbrigade.map.model.Building;
import com.frozen.tankbrigade.map.model.GameData;
import com.frozen.tankbrigade.map.model.GameUnitType;
import com.frozen.tankbrigade.map.model.Player;
import com.frozen.tankbrigade.ui.widget.FlowLayout;

/**
 * Created by sam on 29/11/14.
 */
public class FactoryDialogFragment extends DialogFragment {
	private ImageView selectedItemImageView;
	private TextView selectedItemTitleView;
	private TextView selectedItemDetailsView;
	private FlowLayout itemSelector;
	private Button buyBtn;
	private Button closeBtn;

	private Player player;
	private Building factory;
	private GameData gameData;
	private GameUnitType selectedUnitType;
	private OnBuyListener listener;


	public static interface OnBuyListener {
		public void onBuyUnit(Player player, Building factory, GameUnitType unitType);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView=inflater.inflate(R.layout.fragment_factory,container,false);
		//getDialog().setTitle("Factory");
		selectedItemDetailsView=(TextView)rootView.findViewById(R.id.itemDetails);
		selectedItemImageView=(ImageView)rootView.findViewById(R.id.itemCloseup);
		selectedItemTitleView=(TextView)rootView.findViewById(R.id.itemName);
		itemSelector=(FlowLayout)rootView.findViewById(R.id.itemTable);
		buyBtn=(Button)rootView.findViewById(R.id.buyBtn);
		closeBtn=(Button)rootView.findViewById(R.id.closeBtn);

		buyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBuy();
			}
		});
		closeBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onClose();
			}
		});

		populateTable();

		return rootView;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);

		// request a window without the title
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	public void openDialog(Player player,Building factory,FragmentManager fragmentManager) {
		this.player=player;
		this.factory=factory;
		selectUnit(null);
		populateTable();
		show(fragmentManager,"factory");
	}

	public void setGameConfig(GameData gameData) {
		this.gameData=gameData;
		populateTable();
	}

	public void setOnBuyListener(OnBuyListener listener) {
		this.listener=listener;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		populateTable();
	}

	private void populateTable() {
		if (itemSelector==null||gameData==null||player==null||getActivity()==null) return;
		itemSelector.removeAllViews();
		for (GameUnitType unitType:gameData.unitTypes) {
			createViewForUnit(unitType,itemSelector);
		}
	}

	private FlowLayout.LayoutParams tableItemLayoutParams=new FlowLayout.LayoutParams(0,0);

	private View createViewForUnit(final GameUnitType unitType, ViewGroup container) {
		LayoutInflater inflater=getActivity().getLayoutInflater();
		View view=inflater.inflate(R.layout.view_factory_item,container,false);

		view.setLayoutParams(tableItemLayoutParams);

		ImageButton btn=(ImageButton)view.findViewById(R.id.itemBtn);
		TextView price=(TextView)view.findViewById(R.id.itemPrice);

		btn.setImageResource(DrawableMapping.getUnitDrawable(unitType));
		btn.setEnabled(unitType.price<=player.money);
		price.setText(Integer.toString(unitType.price));

		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				selectUnit(unitType);
			}
		});

		container.addView(view);

		return view;
	}

	private void onBuy() {
		if (listener!=null) listener.onBuyUnit(player,factory,selectedUnitType);
		dismiss();
	}

	private void onClose() {
		dismiss();
	}

	private void selectUnit(GameUnitType unitType) {
		selectedUnitType=unitType;
		if (selectedItemDetailsView==null) return;

		if (unitType==null) {
			selectedItemImageView.setImageDrawable(null);
			selectedItemTitleView.setText("");
			selectedItemDetailsView.setText("");
			buyBtn.setEnabled(false);
		} else {
			selectedItemImageView.setImageResource(DrawableMapping.getUnitDrawable(unitType));
			selectedItemTitleView.setText(unitType.name);
			String details;
			if (unitType.isRanged()) {
				details=String.format("Damage : %d[%d-%d]\nHealth : %d\nMovement : %d",
						unitType.damage,unitType.getMinRange(),unitType.getMaxRange(),
						unitType.health,unitType.movement);
			} else {
				details=String.format("Damage : %d\nHealth : %d\nMovement : %d",
						unitType.damage,unitType.health,unitType.movement);
			}
			selectedItemDetailsView.setText(details);
			buyBtn.setEnabled(true);
		}
	}
}
