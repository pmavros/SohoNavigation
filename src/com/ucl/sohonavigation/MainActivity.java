package com.ucl.sohonavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.ucl.sohonavigation.experimenter.ExStartExperimentActivity;
import com.ucl.sohonavigation.participant.ParticipantActivity;
import com.ucl.sohonavigation.service.CommandCodes;

public class MainActivity extends Activity {
	
	private final static String TAG = "MainActivity";
	
	private SohoApplication mSohoApp;
	private Button btnStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		
		Button btnSettings = (Button) findViewById(R.id.btnMainSettings);
		btnSettings.setOnClickListener(btnSettingsListener);
		
		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(btnStartListener);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if (mSohoApp.role == null) {
			btnStart.setEnabled(false);
		} else {
			btnStart.setEnabled(true);
		}
	}
	
	private OnClickListener btnSettingsListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(intent);
		}
	};
	
	private OnClickListener btnStartListener = new OnClickListener() {
		public void onClick(View v) {
			if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_PART)) {
				Intent intent = new Intent(MainActivity.this, ParticipantActivity.class);
				startActivity(intent);
			} else if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_EX)) {
				Intent intent = new Intent(MainActivity.this, ExStartExperimentActivity.class);
				startActivity(intent);
			}
		}
	};
	
	

}
