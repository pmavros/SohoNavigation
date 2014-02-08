package com.ucl.sohonavigation.experimenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ucl.sohonavigation.LoadJsonActivity;
import com.ucl.sohonavigation.MainActivity;
import com.ucl.sohonavigation.R;
import com.ucl.sohonavigation.SohoApplication;
import com.ucl.sohonavigation.helper.Event;
import com.ucl.sohonavigation.helper.EventManager;
import com.ucl.sohonavigation.helper.Route;
import com.ucl.sohonavigation.service.CommandCodes;
import com.ucl.sohonavigation.service.ServiceManager;
import com.ucl.sohonavigation.service.TcpClient;
import com.ucl.sohonavigation.service.TcpService;

public class ExCommandActivity extends Activity {

	// activity tag
	private final static String TAG = "ExCommandActivity";

	// private globals
	private SohoApplication mSohoApp;
	private ServiceManager service;
	private EventManager mEventManager;
	private Spinner spinnerCmdExperimentEvent;
	private TextView textVNextEvent, textView2, textView3;
	private Button btnSendCommand;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ex_cmd);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		mEventManager = mSohoApp.eM;
		
		// init service instance
		initService();

		addItemsToEventSpinner();
		addListenerToSpinnerItemSelection();
		addListenerToButton();
		
		//sendCommandToServer(CommandCodes.READY + ""); // TODO change SERVER
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// init service instance
		if (service == null) {
			initService();
		}
		
		updateScreen();
		btnSendCommand.setEnabled(false);
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	try {
            service.unbind();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            service.unbind();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }
	
	private void initService() {
		this.service = new ServiceManager(this, TcpService.class, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case TcpService.MSG_TCP_CMD:
						int[] cmds = getCommandCodesFromString((String) msg.obj);
						Route route = null;
						Event event = null;
						switch (cmds[0]) {
							case CommandCodes.PARTICIPANT_ACTION:
								switch (cmds[1]) {
									case CommandCodes.PARTICIPANT_STARTING_ROUTE:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										Toast.makeText(getApplicationContext(), "Paricipant started route: " + route.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_AT_ROUTE_START:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										Toast.makeText(getApplicationContext(), "Paricipant is at the starting point for route: " + route.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_SELECTED_LEFT:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										event = route.getEvents().get(cmds[3] - 1);
										Toast.makeText(getApplicationContext(), "Participant chose LEFT on event: " + event.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_SELECTED_STRAIGHT:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										event = route.getEvents().get(cmds[3] - 1);
										Toast.makeText(getApplicationContext(), "Participant chose STRAIGHT on event: " + event.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_SELECTED_RIGHT:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										event = route.getEvents().get(cmds[3] - 1);
										Toast.makeText(getApplicationContext(), "Participant chose RIGHT on event: " + event.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_ENTERED_AZIMUTH:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										event = route.getEvents().get(cmds[3] - 1);
										Toast.makeText(getApplicationContext(), "Participant finished azimuth on event: " + event.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_SELECTED_THINK_ALOUD:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										event = route.getEvents().get(cmds[3] - 1);
										Toast.makeText(getApplicationContext(), "Participant finished think aloud on event: " + event.toString(), Toast.LENGTH_LONG).show();
										break;
									case CommandCodes.PARTICIPANT_COMPLETED_ROUTE:
										route = mEventManager.getmRoutes().get(cmds[2] - 1);
										Toast.makeText(getApplicationContext(), "Paricipant completed route: " + route.toString(), Toast.LENGTH_LONG).show();
										break;
									default:
										break;
								}
								break;
							case CommandCodes.READY:
								btnSendCommand.setEnabled(true);
								break;
							default:
								break;
						}
						break;
					default:
						super.handleMessage(msg);
				}
			}
		});
	}
	
	private void addItemsToEventSpinner() {
		spinnerCmdExperimentEvent = (Spinner) findViewById(R.id.spinnerCmdExperimentEvent);
		ArrayAdapter dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mEventManager.getmRoute().getEvents());
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerCmdExperimentEvent.setAdapter(dataAdapter);
	}
	
	private void addListenerToSpinnerItemSelection() {
		spinnerCmdExperimentEvent = (Spinner) findViewById(R.id.spinnerCmdExperimentEvent);
		spinnerCmdExperimentEvent.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				Route route = mEventManager.getmRoute();
				Event event = route.getEvents().get(pos);
				mEventManager.setmEvent(event);
				updateScreen();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}
	
	private void addListenerToButton() {
		btnSendCommand = (Button) findViewById(R.id.btnSendCommand);
		btnSendCommand.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				int routeNumber = mEventManager.getmRoute().getRouteNumber();
				int eventNumber = mEventManager.getmEvent().getEventNumber();
				sendEventToParticipant(routeNumber, eventNumber);
				
				Toast.makeText(getApplicationContext(), "Selected route/event: " + routeNumber + "/" + eventNumber, Toast.LENGTH_LONG).show();
				
				mEventManager.nextEvent();
				
				if (mEventManager.getmRoute() == null) { // route finished TODO other side?!
					Intent intent = new Intent(ExCommandActivity.this, ExStartExperimentActivity.class);
					startActivity(intent);
				} else {
					updateScreen();
					btnSendCommand.setEnabled(false);
				}
			}
		});
	}
	
	private void updateScreen() {
		textVNextEvent = (TextView) findViewById(R.id.textVNextEvent);
		textView2 = (TextView) findViewById(R.id.textVGoal);
		textView3 = (TextView) findViewById(R.id.textView3);
		
		if (mEventManager.getmEvent().getEventType().equals("RouteStart")) {
			textVNextEvent.setText(" Route Start");
			textView2.setText("Route name: ");
			textView3.setText(mEventManager.getmRoute().getRouteName());
			btnSendCommand.setText("Start Route");
			btnSendCommand.setEnabled(true);
		} else if (mEventManager.getmEvent().getEventType().equals("Crossing")) {
			textVNextEvent.setText(" Crossing");
			textView2.setText("Event name: ");
			textView3.setText(mEventManager.getmEvent().getEventName());
			btnSendCommand.setText("Trigger Event");
		} else if (mEventManager.getmEvent().getEventType().equals("NewGoal")) {
			textVNextEvent.setText("New Goal");
			textView2.setText("Event name: ");
			textView3.setText(mEventManager.getmEvent().getEventName());
			btnSendCommand.setText("Trigger Event");
		} else if (mEventManager.getmEvent().getEventType().equals("RouteCompleted")) {
			textVNextEvent.setText("Route Completed");
			textView2.setText("Route name: ");
			textView3.setText(mEventManager.getmRoute().getRouteName());
			btnSendCommand.setText("End Route");
		}
		
		//spinnerCmdExperimentEvent.setSelection(mEventManager.getmEvent().getEventNumber() - 1);
	}
	
	private int[] getCommandCodesFromString(String s) {
		String[] cmds = s.split(":");
		int[] cmdCodes = new int[cmds.length];
		for (int i = 0; i < cmds.length; i++) {
			try {
				cmdCodes[i] = Integer.parseInt(cmds[i]);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				//TODO
			}
		}
		return cmdCodes;
	}
	
	private void sendStringToService(int msgCode, String s) {
		try {      
			Message msg = Message.obtain(null, msgCode, s);
			service.send(msg);
		} catch (RemoteException e) {
			// TODO
			e.printStackTrace();
		}
	}
	
	private void sendCommandToServer(int cmdCode) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_SM + ""+cmdCode);
	}
	
	private void sendCommandToParticipant(String s) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_PART + " " + s);
	}
	
	private void sendEventToParticipant(int routeNumber, int eventNumber) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_PART + " " + CommandCodes.EVENT_TRIGGER + ":" + routeNumber + ":" + eventNumber);
	}

}
