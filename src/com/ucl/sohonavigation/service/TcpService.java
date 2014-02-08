package com.ucl.sohonavigation.service;

import java.util.ArrayList;

import com.ucl.sohonavigation.R;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ucl.sohonavigation.MainActivity;
import com.ucl.sohonavigation.SohoApplication;

public class TcpService extends Service {
	
	public static final int MSG_REGISTER_CLIENT = 8001;
	public static final int MSG_UNREGISTER_CLIENT = 8002;
	public static final int MSG_ECHO = 8003;
	public static final int MSG_CONNECT_TCP = 8004;
	public static final int MSG_TCP_CMD = 8005;
	
	
	// service tag
	private final static String TAG = "TCP-Service";
	
	// ArrayList of all registered Clients
	ArrayList<Messenger> mClients = new ArrayList<Messenger>();
	// TODO
	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.
	private SohoApplication mSohoApp;
	private TcpClient mTcpClient;
	private NotificationManager nm;
	private String mServerIp;
	private int mServerPort;
	
	@Override
	public void onCreate() {
		super.onCreate();
		//Log.i(TAG, "Service created.");
		// show android notification that service started
		showNotification();
		

		mSohoApp = (SohoApplication) getApplicationContext();
		
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Received start id " + startId + ": " + intent);
		return START_STICKY; // run until explicitly stopped.
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		nm.cancel(getClass().getSimpleName().hashCode());
		Log.i(TAG, "Service Stopped.");
	}
	
	
	/**
	 * Method for sending a message to all clients (service -> activity)
	 * 
	 * @param msg
	 */
	protected void send(Message msg) {
		for (int i=mClients.size()-1; i>=0; i--) {
			try {
				Log.i(TAG, "Sending message to clients: "+msg);
				mClients.get(i).send(msg);
			} catch (RemoteException e) {
				// The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
				Log.e(TAG, "Client is dead. Removing from list: "+i);
				mClients.remove(i);
			}
		}
	}
	
	// 
	/**
	 * Handler for messages from clients ( activity -> service)
	 *
	 */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			
			Message response = null;
			
			switch (msg.what) {
				case MSG_REGISTER_CLIENT:
					Log.i(TAG, "Client registered: " + msg.replyTo);
					mClients.add(msg.replyTo);
					break;
				case MSG_UNREGISTER_CLIENT:
					Log.i(TAG, "Client un-registered: " + msg.replyTo);
					mClients.remove(msg.replyTo);
					break;
				case MSG_ECHO:
					Log.i(TAG, "Service echoing message: " + msg.obj);
					Message reply = Message.obtain(null, TcpService.MSG_ECHO, msg.obj);
					send(reply);
				case MSG_CONNECT_TCP:
					Log.i(TAG, "Service connecting to Tcp Server");
					String[] conDetails = ((String) msg.obj).split(" ");
					mServerIp = conDetails[0];
					mServerPort = Integer.parseInt(conDetails[1]);
					Log.i(TAG, "Connection Details: " + mServerIp + ":" + mServerPort);
					new ConnectTask().execute("");
					break;
				case MSG_TCP_CMD:
					Log.i(TAG, "Sending cmd to Tcp Server");
					mTcpClient.sendMessage((String) msg.obj);
					break;
				default:
					Log.e(TAG, "not a valid msg code: " + msg.what);
					break;
			}
		}
	}
	
	/**
	 * show a netification that the service has started
	 * 
	 */
	private void showNotification() {
		nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		String notificationText = getString(R.string.service_started, getClass().getSimpleName());
		Notification notification = new Notification(R.drawable.ic_launcher, notificationText, System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		notification.setLatestEventInfo(this, getClass().getSimpleName(), notificationText, contentIntent);
		
		nm.notify(getClass().getSimpleName().hashCode(), notification);
	}
	
	/**
	 * connecttask for tcpclient connection
	 *
	 */
	public class ConnectTask extends AsyncTask<String, String, TcpClient> {
		
		@Override
		protected TcpClient doInBackground(String... message) {
			mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
				@Override
				public void messageReceived(String message) {
					publishProgress(message);
				}
			}, mServerIp, mServerPort, mSohoApp.role);
			mTcpClient.run();
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			Log.i(TAG, "Msg received from Tcp Server: " + values[0]);
			Message msg = Message.obtain(null, MSG_TCP_CMD, values[0]);
			send(msg);
		}
	}
	
	
}
