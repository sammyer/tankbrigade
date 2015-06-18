package com.frozen.tankbrigade;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by sam on 02/12/14.
 */
public class WinLoseActivity extends Activity {

	public static final String EXTRA_OUTCOME = "EXTRA_OUTCOME";

	private boolean outcome;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_winlose);

		TextView titleView=(TextView)findViewById(R.id.titleText);
		TextView messageView=(TextView)findViewById(R.id.messageText);
		findViewById(R.id.continueBtn).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goToNextScreen();
			}
		});

		if (savedInstanceState==null) outcome=getIntent().getBooleanExtra(EXTRA_OUTCOME,true);
		else outcome=savedInstanceState.getBoolean(EXTRA_OUTCOME,true);

		titleView.setText(outcome?"WIN!":"FAIL.");
		messageView.setText(outcome?"You have won.  Solid.":"You have lost.  Bummer.");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(EXTRA_OUTCOME,outcome);
		super.onSaveInstanceState(outState);
	}

	private void goToNextScreen() {
		Intent intent=new Intent(this,MenuActivity.class);
		startActivity(intent);
	}
}
