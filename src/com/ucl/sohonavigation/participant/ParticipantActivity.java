package com.ucl.sohonavigation.participant;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ucl.sohonavigation.R;
import com.ucl.sohonavigation.SohoApplication;
import com.ucl.sohonavigation.helper.Event;
import com.ucl.sohonavigation.helper.EventManager;
import com.ucl.sohonavigation.service.CommandCodes;
import com.ucl.sohonavigation.service.ServiceManager;
import com.ucl.sohonavigation.service.TcpService;

public class ParticipantActivity extends Activity {
	
	// activity tag
	private final static String TAG = "ParticipantActivity";

	// private globals
	private FrameLayout mRootView;
	private SohoApplication mSohoApp;
	private EventManager mEventManager;
	private ServiceManager mService;
	private ParticipantToolbox mToolbox;
	
	private boolean ready;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_participant_root);
		mRootView = (FrameLayout) findViewById(R.id.partRootView);
		
		mSohoApp = (SohoApplication) getApplicationContext();
		mEventManager = mSohoApp.eM;
		ready = false;

		// init service instance
		initService();
		
		mToolbox = new ParticipantToolbox();
		mToolbox.setService(mService);
		
		// set the view to the waiting screen
		setViewToWait();
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// init service instance
		if (mService == null) {
			initService();
		}
	}
	
	@Override
    protected void onStop() {
    	super.onStop();
    	try {
    		mService.unbind();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        try {
        	mService.unbind();
        } catch (Throwable t) {
            Log.e(TAG, "Failed to unbind from the service", t);
        }
    }
	
	@Override
	public void onBackPressed() {
		// TODO
	}
	
	private void initService() {
		this.mService = new ServiceManager(this, TcpService.class, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*if (!ready) {
					return;
				}
				ready = false;
				*/
				switch (msg.what) {
					case TcpService.MSG_TCP_CMD:
						int[] cmds = mToolbox.getCommandCodesFromString((String) msg.obj);
						switch (cmds[0]) {
							case CommandCodes.EVENT_TRIGGER:
								Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
								v.vibrate(500);
								int routeNumber = cmds[1];
								int eventNumber = cmds[2];
								mEventManager.setRouteAndEvent(routeNumber, eventNumber);
								
								Event event = mEventManager.getmEvent();
								if (event.getEventType().equals("RouteStart")) {
									setViewToRouteStart();
								} else if (event.getEventType().equals("Crossing") || event.getEventType().equals("NewGoal")) {
									if (event.isAskDirections()) {
										setViewToAskDirections();
									}  else if (event.isAzimuth()) {
										setViewToGetAzimuth();
									} else if (event.isThinkAloutTime()) {
										setViewToAskTime();
									} else if (event.getEventType().equals("Crossing")){
										setViewToGiveDirections();
									} else {
										setViewToGoal();
									}
								} else if (mEventManager.getmEvent().getEventType().equals("RouteCompleted")) {
									setViewToRouteEnd();
								}
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
	
	private void setView(View view) {
		mRootView.removeAllViews();
		mRootView.addView(view);
		//setContentView(R.layout.activity_participant_root);
	}
	
	
	private void setViewToWait() {
		View view = View.inflate(this, R.layout.activity_participant_wait, null);
		
		setView(view);
		
		// send a message to the experimenter, that the participant is ready for the next event
		mToolbox.sendCommandToExperimenter(CommandCodes.READY + "");
		ready = true;
		// TODO EX send msg to ask if ready. answer
	}
	
	private void setViewToRouteStart() {
		View view = View.inflate(this, R.layout.activity_participant_start_route, null);
		
		setView(view);
		
		Button btnStartRoute = (Button) view.findViewById(R.id.btnStartRoute);
		btnStartRoute.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_STARTING_ROUTE + ":" + mEventManager.getmRoute().getRouteNumber());
				// choose next View
				setViewToGotoStart();
			}
		});
	}
	
	private void setViewToGotoStart() {
		View view = View.inflate(this, R.layout.activity_participant_goto_start, null);
		
		setView(view);
		
		// set text on view
		TextView textVStartPosition = (TextView) view.findViewById(R.id.textVInstructions);
		textVStartPosition.setText(mEventManager.getmRoute().getRouteStart());
				
		// set image
		File imageFile = new File(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmRoute().getRouteStartImage() + ".JPG");
		Bitmap bitmap = mToolbox.decodeBitmap(imageFile);
		//Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmRoute().getRouteStartImage() + ".JPG");
		ImageView imageVGoal =(ImageView) view.findViewById(R.id.imageVGoal1);
		imageVGoal.setImageBitmap(bitmap);
		
		Button btnOk = (Button) view.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_AT_ROUTE_START + ":" + mEventManager.getmRoute().getRouteNumber());
				
				Event event = mEventManager.getmEvent();
				if (event.isAskDirections()) {
					setViewToAskDirections();
				}  else if (event.isAzimuth()) {
					setViewToGetAzimuth();
				} else if (event.isThinkAloutTime()) {
					setViewToAskTime();
				} else if (event.getEventType().equals("Crossing")){
					setViewToGiveDirections();
				} else {
					setViewToGoal();
				}
			}
			
		});
	}
	
	private void setViewToAskDirections() {
		View view = View.inflate(this, R.layout.activity_participant_ask_directions, null);
		
		setView(view);
		
		// set text on view
		TextView textVGoal = (TextView) view.findViewById(R.id.textVGotoGoal5);
		textVGoal.setText(mEventManager.getmEvent().getGoal());
				
		// set image
		File imageFile = new File(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
		Bitmap bitmap = mToolbox.decodeBitmap(imageFile);
		//Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
        ImageView imageVGoal =(ImageView) view.findViewById(R.id.imageVGoal4);
        imageVGoal.setImageBitmap(bitmap);
        
        Button btnLeft = (Button) view.findViewById(R.id.btnLeft);
		btnLeft.setEnabled(mEventManager.getmEvent().isLeft());
		btnLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_SELECTED_LEFT + ":" + mEventManager.getmRoute().getRouteNumber() + ":" + mEventManager.getmEvent().getEventNumber());
		        
				setNextScreenFromAskDir();
			}
		});
		
		Button btnStraight = (Button) view.findViewById(R.id.btnStraight);
		btnStraight.setEnabled(mEventManager.getmEvent().isStraight());
		btnStraight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_SELECTED_STRAIGHT + ":" + mEventManager.getmRoute().getRouteNumber() + ":" + mEventManager.getmEvent().getEventNumber());
				
				setNextScreenFromAskDir();
			}
		});
		
		Button btnRight = (Button) view.findViewById(R.id.btnRight);
		btnRight.setEnabled(mEventManager.getmEvent().isRight());
		btnRight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_SELECTED_RIGHT + ":" + mEventManager.getmRoute().getRouteNumber() + ":" + mEventManager.getmEvent().getEventNumber());
				
				setNextScreenFromAskDir();
			}
		});
	}
	
	private void setNextScreenFromAskDir() {
		Event event = mEventManager.getmEvent();
		if (event.isAzimuth()) {
			setViewToGetAzimuth();
		} else if (event.isThinkAloutTime()) {
			setViewToAskTime();
		} else if (event.getEventType().equals("Crossing")){
			setViewToGiveDirections();
		} else {
			setViewToGoal();
		}
	}
	
	private void setViewToGetAzimuth() {
		View view = View.inflate(this, R.layout.activity_participant_azimuth, null);
		
		setView(view);
		
		TextView textVGoal = (TextView) view.findViewById(R.id.textVGoal);
		textVGoal.setText(mEventManager.getmEvent().getGoal());
		
		File imageFile = new File(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
		Bitmap bitmap = mToolbox.decodeBitmap(imageFile);
		//Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
        ImageView imageVGoal =(ImageView) view.findViewById(R.id.imageVGoal1);
        imageVGoal.setImageBitmap(bitmap);
        
        Button btnNext = (Button) view.findViewById(R.id.btnNext);
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				int azimuth = 0; // TODO!!!
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_ENTERED_AZIMUTH + ":" + mEventManager.getmRoute().getRouteNumber() + ":" + mEventManager.getmEvent().getEventNumber() + ":" + azimuth);
				
				Event event = mEventManager.getmEvent();
				if (event.isThinkAloutTime()) {
					setViewToAskTime();
				} else if (event.getEventType().equals("Crossing")){
					setViewToGiveDirections();
				} else {
					setViewToGoal();
				}
			}
			
		});
	}
	
	private void setViewToAskTime() {
		View view = View.inflate(this, R.layout.activity_participant_ask_time, null);
		
		setView(view);
		
		TextView textVGoal = (TextView) view.findViewById(R.id.textVGotoGoal);
		textVGoal.setText(mEventManager.getmEvent().getGoal());
		
		File imageFile = new File(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
		Bitmap bitmap = mToolbox.decodeBitmap(imageFile);
		//Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
        ImageView imageVGoal =(ImageView) view.findViewById(R.id.imageVGoal2);
        imageVGoal.setImageBitmap(bitmap);
        
        // set items on spinner
        String[] times = { "30s", "60s", "90s", "2m", "3m", "5m", "7m" };
		
        Spinner spinnerAskTime = (Spinner) view.findViewById(R.id.spinnerThinkAlout);
		ArrayAdapter dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, times);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerAskTime.setAdapter(dataAdapter);
		
		// set button
		Button btnNext = (Button) view.findViewById(R.id.btnNext2);
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Spinner spinnerAskTime = (Spinner) findViewById(R.id.spinnerThinkAlout);
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_SELECTED_THINK_ALOUD + ":" + mEventManager.getmRoute().getRouteNumber() + ":" + mEventManager.getmEvent().getEventNumber() + ":" + spinnerAskTime.getSelectedItem());
				
				Event event = mEventManager.getmEvent();
				if (event.getEventType().equals("Crossing")){
					setViewToGiveDirections();
				} else {
					setViewToGoal();
				}
			}
		});
	}
	
	private void setViewToGiveDirections() {
		View view = View.inflate(this, R.layout.activity_participant_give_directions, null);
		
		setView(view);
		
		TextView textVDir1 = (TextView) view.findViewById(R.id.textVDir1);
		TextView textVDir2 = (TextView) view.findViewById(R.id.textVDir2);
		textVDir1.setText("Please go " + mEventManager.getmEvent().getDirectionsOrientation() + " on");
		textVDir2.setText(mEventManager.getmEvent().getDirectionsStreet());
		
		// TODO set arrow image
		
		Button btnOk = (Button) view.findViewById(R.id.btnOk2);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// send a message to the experimenter, that the participant is ready for the next event
				mToolbox.sendCommandToExperimenter(CommandCodes.READY + "");
				ready = true;
				setViewToGoal();
			}
		});
	}
	
	private void setViewToGoal() {
		View view = View.inflate(this, R.layout.activity_participant_goal, null);
		
		setView(view);
		
		// send a message to the experimenter, that the participant is ready for the next event
		mToolbox.sendCommandToExperimenter(CommandCodes.READY + "");
		ready = true;
		
		TextView textVGoal = (TextView) view.findViewById(R.id.textVGotoGoal4);
		textVGoal.setText(mEventManager.getmEvent().getGoal());
		
		File imageFile = new File(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
		Bitmap bitmap = mToolbox.decodeBitmap(imageFile);
		//Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SohoNavigation/images/" + mEventManager.getmEvent().getGoalImage() + ".JPG");
        ImageView imageVGoal =(ImageView) view.findViewById(R.id.imageVGoal3);
        imageVGoal.setImageBitmap(bitmap);
	}
	
	private void setViewToRouteEnd() {
		View view = View.inflate(this, R.layout.activity_participant_end_route, null);
		
		setView(view);
		
		TextView textVRouteName = (TextView) view.findViewById(R.id.textVRouteName);
		textVRouteName.setText("Route " + mEventManager.getmRoute().getRouteName());
		
		Button btnOk = (Button) view.findViewById(R.id.btnCompletedOk);
		btnOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mToolbox.sendCommandToExperimenter(CommandCodes.PARTICIPANT_ACTION + ":" + CommandCodes.PARTICIPANT_COMPLETED_ROUTE + ":" + mEventManager.getmRoute().getRouteNumber());

				setViewToWait();
			}
			
		});
	}
	
	
	
	
	
	
	
	
	
	
	
}
