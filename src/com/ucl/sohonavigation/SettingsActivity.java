package com.ucl.sohonavigation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ucl.sohonavigation.experimenter.ExStartExperimentActivity;
import com.ucl.sohonavigation.participant.ParticipantActivity;
import com.ucl.sohonavigation.service.CommandCodes;
import com.ucl.sohonavigation.service.ServiceManager;
import com.ucl.sohonavigation.service.TcpClient;
import com.ucl.sohonavigation.service.TcpService;

public class SettingsActivity extends Activity {
	
	private final static String TAG = "SettingsActivity";
	// private globals
	private SohoApplication mSohoApp;
	private ServiceManager service;
	
	private Spinner spinner;
	private Button btnSettingsService, btnSettingsTcpServerConnect, btnSettingsTcpServerDisconnect, btnSettingsLoadEventData, btnSettingsTestConnection, btnSettingsSettingsStartExperiment;
	private EditText editTextIp, editTextPort;
	private TextView tVSettingConnection, tVSettingsTestConnection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		
		// add filenames to spinner
		addItemsToSpinner();
		
		// init service instance
		initService();
		
		// add listener to buttons
		addListenerToServiceButton();
		addListenerToServerButtons();
		addListenerToTestConnectionButton();
		addListenerToLoadEventDataButton();
		addListenerToStartExperimentButton();
		
		
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
		
		Log.i(TAG, "SettingsActivity onStart()");

		// init service instance
		if (service == null) {
			initService();
		}
		
		// set text on service button depending on service status
		if (service != null && service.isRunning()) {
			btnSettingsService.setText(R.string.stop_service);
		} else {
			btnSettingsService.setText(R.string.start_service);
		}
		
		tVSettingConnection = (TextView) findViewById(R.id.tVSettingConnection);
		tVSettingConnection.setText("");
		
		tVSettingsTestConnection = (TextView) findViewById(R.id.tVSettingsTestConnection);
		tVSettingsTestConnection.setText("");
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	try {
    		if (service != null) {
                service.unbind();
    		}
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
            //TODO
        }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        try {
    		if (service != null) {
                service.unbind();
    		}
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
            //TODO
        }
    }
	
	private void addItemsToSpinner() {
		
		Log.i(TAG, "adding items to spinner");
		
		// get the spinner from xml
		spinner = (Spinner) findViewById(R.id.settingsSpinner);
		
		// create list with roles
		List<String> roles = new ArrayList<String>();
		roles.add(CommandCodes.CLIENT_NAME_PART);
		roles.add(CommandCodes.CLIENT_NAME_EX);
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, roles);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(dataAdapter);
	}
	
	private void addListenerToServiceButton() {
		
		Log.i(TAG, "adding service button listener");
		
		btnSettingsService = (Button) findViewById(R.id.btnSettingsService);
		
		// set text on button depending on service status
		if (service != null && service.isRunning()) {
			btnSettingsService.setText(R.string.stop_service);
		} else {
			btnSettingsService.setText(R.string.start_service);
		}
		
		btnSettingsService.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// check if service is running
				if (!service.isRunning()) { // service not running - start it
					service.start();
					btnSettingsService.setText(R.string.stop_service);
				} else { // service is already running - stop it
					service.stop();
					btnSettingsService.setText(R.string.start_service);
				}
				
				/*btnStartService.setEnabled(false);
				btnConnect.setEnabled(true);
				btnStopService.setEnabled(true);*/
			}
		});
	}
	
	private void addListenerToServerButtons() {
		
		Log.i(TAG, "adding tcp server button listener");
		
		btnSettingsTcpServerConnect = (Button) findViewById(R.id.btnSettingsTcpServerConnect);
		btnSettingsTcpServerDisconnect = (Button) findViewById(R.id.btnSettingsTcpServerDisconnect);
		
		btnSettingsTcpServerConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Connecting to TCP Server");
				
				// set role
				mSohoApp.role = String.valueOf(spinner.getSelectedItem());
				
				// get server address
				//String ip = "192.168.0.11";
				String port = "8086";
				editTextIp = (EditText) findViewById(R.id.editTextIp);
				editTextPort = (EditText) findViewById(R.id.editTextPort);
				String ip = editTextIp.getText().toString();
				//String port = editTextPort.getText().toString();
				
				if (ip.length()<1) {
					Toast.makeText(getApplicationContext(), "Please enter an IP adress!", Toast.LENGTH_LONG).show();
					return;
				}
				
				// connect to server
				sendStringToService(TcpService.MSG_CONNECT_TCP, ip + " " + port);
				
				/*btnConnect.setEnabled(false);
				btnDisconnect.setEnabled(true);
				btnStopService.setEnabled(false);
				btnTestConnection.setEnabled(true);*/
				
				// TODO inform about connection
			}
		});
		
		btnSettingsTcpServerDisconnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "Disconnecting Tcp Client");
				
				// disconnect from server
				sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_QUIT);
				
				// set role
				mSohoApp.role = "";
				
				/*btnDisconnect.setEnabled(false);
				btnConnect.setEnabled(true);
				btnStopService.setEnabled(true);
				btnTestConnection.setEnabled(false);*/
				
				// TODO inform about connection
			}
		});
	}
	
	private void addListenerToTestConnectionButton() {
		
		Log.i(TAG, "adding test connection button listener");
		
		btnSettingsTestConnection = (Button) findViewById(R.id.btnSettingsTestConnection);
		
		btnSettingsTestConnection.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendStringToOtherParty(TcpClient.SERVER_TEST_CONNECTION);
			}
		});
	}
	
	private void addListenerToLoadEventDataButton() {
		
		Log.i(TAG, "adding event data button listener");
		
		btnSettingsLoadEventData = (Button) findViewById(R.id.btnSettingsLoadEventData);
		
		btnSettingsLoadEventData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SettingsActivity.this, LoadJsonActivity.class);
				startActivityForResult(intent, 1);
			}
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				
			}
		}
	}

	private void addListenerToStartExperimentButton() {
		
		Log.i(TAG, "adding event data button listener");
		
		btnSettingsSettingsStartExperiment = (Button) findViewById(R.id.btnSettingsSettingsStartExperiment);
		
		btnSettingsSettingsStartExperiment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_PART)) {
					Intent intent = new Intent(SettingsActivity.this, ParticipantActivity.class);
					startActivity(intent);
				} else if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_EX)) {
					Intent intent = new Intent(SettingsActivity.this, ExStartExperimentActivity.class);
					startActivity(intent);
				}
			}
		});
	}
	
	private void initService() {
		this.service = new ServiceManager(this, TcpService.class, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				tVSettingConnection = (TextView) findViewById(R.id.tVSettingConnection);
				tVSettingsTestConnection = (TextView) findViewById(R.id.tVSettingsTestConnection);
				switch (msg.what) {
					case TcpService.MSG_TCP_CMD:
						String cmd = (String) msg.obj;
						if (cmd.equals(TcpClient.SERVER_CMD_CODE_OK)) {
							tVSettingConnection.setText("connected");
						} else if (cmd.equals(TcpClient.SERVER_CMD_CODE_DISCONNECTED)) {
							tVSettingConnection.setText("disconnected");
						} else if (cmd.equals(TcpClient.SERVER_TEST_CONNECTION)) {
							tVSettingsTestConnection.setText("ok");
							sendStringToOtherParty(TcpClient.SERVER_TEST_CONNECTION_REPLY);
						} else if (cmd.equals(TcpClient.SERVER_TEST_CONNECTION_REPLY)) {
							tVSettingsTestConnection.setText("ok");
						} else {
							Log.i(TAG, "Not a valid tcp command.");
						}
						break;
					default:
						super.handleMessage(msg);
				}
			}
		});
	}
	
	
	private void sendStringToService(int msgCode, String s) {
		try {      
			Message msg = Message.obtain(null, msgCode, s);
			service.send(msg);
		} catch (RemoteException e) {
			//TODO
			e.printStackTrace();
		}
	}
	
	/*private void sendIntToService(int msgCode, int i) {
		try {      
			Message msg = Message.obtain(null, msgCode, i, 0);
			service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}*/
	
	private void sendStringToOtherParty(String s) {
		if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_PART)) {
			sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_EX + " " + s);
		} else if (mSohoApp.role.equals(CommandCodes.CLIENT_NAME_EX)) {
			sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_PART + " " + s);
		}
	}
	
}
