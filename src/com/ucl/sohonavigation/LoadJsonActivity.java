package com.ucl.sohonavigation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.ucl.sohonavigation.helper.EventManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class LoadJsonActivity extends Activity {
	
	private final static String TAG = "LoadJsonActivity";
	
	private SohoApplication mSohoApp;
	
	private List<String> fileNames;
	private String currentFileName;
	private Spinner spinner;
	private Button btnLoadData;
	private TextView mTextVExperimentCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		Log.i(TAG, "LoadJsonActivity onCreate()");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_json);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		
		// get all json files from SohoNavigation Folder
		getFileList();

		// add filenames to spinner
		addItemsToSpinner();
		
		// add button listener
		addListenerToLoadButton();
		
		//TODO add info on loaded JSON
		//mTextVExperimentCode = (TextView) findViewById(R.id.textVExperimentCode);
	}
	
	private void getFileList() {
		
		Log.i(TAG, "getting filenames from sd card");
		
		File sdCard = Environment.getExternalStorageDirectory();
		File dir = new File(sdCard, "SohoNavigation/routes");
		
		fileNames = new ArrayList<String>();
		
		if (dir.exists() && dir.isDirectory()) {
			for (File f : dir.listFiles()) {
				if (f.isFile()) {
					String name = f.getName();
					String[] splitName = name.split("\\.");
					if (splitName[1].equals("json")) {
						fileNames.add(splitName[0]);
					}
				}
			}
		}
		
		
	}
	
	private void addItemsToSpinner() {
		
		Log.i(TAG, "adding items to spinner");
		
		spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fileNames);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}
	
	private void addListenerToLoadButton() {
		
		Log.i(TAG, "adding load button listener");
		
		spinner = (Spinner) findViewById(R.id.spinner);
		btnLoadData = (Button) findViewById(R.id.btnLoadData);
		
		btnLoadData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fileName = String.valueOf(spinner.getSelectedItem());
				Toast.makeText(LoadJsonActivity.this, "Selected: " + fileName, Toast.LENGTH_SHORT).show();
				
				//TODO
				mSohoApp.eM = new EventManager(fileName);
				//mTextVExperimentCode.setText(mSohoApp.eM.experimentCode);
				
				Intent returnIntent = new Intent();
				setResult(RESULT_OK, returnIntent);
				finish();
			}
		});
	}
}
