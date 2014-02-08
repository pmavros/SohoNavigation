package com.ucl.sohonavigation.experimenter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ucl.sohonavigation.LoadJsonActivity;
import com.ucl.sohonavigation.R;
import com.ucl.sohonavigation.SohoApplication;
import com.ucl.sohonavigation.helper.EventManager;
import com.ucl.sohonavigation.helper.Route;

public class ExStartExperimentActivity extends Activity {

	// activity tag
	private final static String TAG = "ExStartExperimentActivity";

	// private globals
	private SohoApplication mSohoApp;
	private EventManager mEventManager;
	
	private EditText editTextParticipantCode;
	private Spinner spinnerStartExperimentRoute;
	private Button btnStartExperiment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ex_start);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		mEventManager = mSohoApp.eM;
		
		addItemsToRouteSpinner();
		addListenerToStartButton();
	}
	
	private void addItemsToRouteSpinner() {
		spinnerStartExperimentRoute = (Spinner) findViewById(R.id.spinnerStartExperimentRoute);
		ArrayAdapter<Route> dataAdapter = new ArrayAdapter<Route>(this, android.R.layout.simple_spinner_item, mEventManager.getmRoutes());
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerStartExperimentRoute.setAdapter(dataAdapter);
	}
	
	private void addListenerToStartButton() {
		
		editTextParticipantCode = (EditText) findViewById(R.id.editTextParticipantCode);
		spinnerStartExperimentRoute = (Spinner) findViewById(R.id.spinnerStartExperimentRoute);
		btnStartExperiment = (Button) findViewById(R.id.btnStartExperiment);
		
		btnStartExperiment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				String participantCode = editTextParticipantCode.getText().toString(); //TODO use
				Route route = (Route) spinnerStartExperimentRoute.getSelectedItem();
				mEventManager.setmRoute(route);
				mEventManager.setmEvent(route.getEvents().get(0));
				Intent intent = new Intent(ExStartExperimentActivity.this, ExCommandActivity.class);
				startActivity(intent);
			}
			
		});
	}
}
