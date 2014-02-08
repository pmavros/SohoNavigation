package com.ucl.sohonavigation.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ServiceManager {
	
	private final static String TAG = "ServiceManager";
	
	private Context mActivity;
	private Class<? extends Service> mServiceClass;
	private Handler mIncomingHandler = null;
	private boolean mIsBound;
	private Messenger mService = null;
	private final Messenger mMessenger = new Messenger(new IncomingHandler());
	
	/**
	 * 
	 */
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			Log.i(TAG, "Attached.");
			try {
				Message msg = Message.obtain(null, TcpService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been unexpectedly disconnected - process crashed.
			mService = null;
			Log.i(TAG, "Disconnected.");
		}
	};
	
	/**
	 * Constructor
	 */
	public ServiceManager(Context context, Class<? extends Service> serviceClass, Handler incomingHandler) {
		this.mActivity = context;
		this.mServiceClass = serviceClass;
		this.mIncomingHandler = incomingHandler;
		
		if (isRunning()) {
			doBindService();
		}
	}
	
	/**
	 * 
	 */
	public void start() {
		doStartService();
		doBindService();
	}
	
	/**
	 * 
	 */
	public void stop() {
		doUnbindService();
		doStopService();    	
	}
	
	/**
	 * Use with caution (only in Activity.onDestroy())! 
	 */
	public void unbind() {
		doUnbindService();
	}

	/**
	 * 
	 */
	public boolean isRunning() {
		ActivityManager manager = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
		
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (mServiceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 */
	public void send(Message msg) throws RemoteException {
		if (mIsBound) {
			if (mService != null) {
				mService.send(msg);
			}
		}
	}
	
	/**
	 * 
	 * */
	private void doStartService() {
		mActivity.startService(new Intent(mActivity, mServiceClass));    	
	}

	/**
	 * 
	 * */
	private void doStopService() {
		mActivity.stopService(new Intent(mActivity, mServiceClass));
	}
	
	/**
	 * 
	 * */
	private void doBindService() {
		mActivity.bindService(new Intent(mActivity, mServiceClass), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}
	
	/**
	 * 
	 * */
	private void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, TcpService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has crashed.
				}
			}
			
			// Detach our existing connection.
			mActivity.unbindService(mConnection);
			mIsBound = false;
			Log.i(TAG, "Unbinding.");
		}
	}
	
	/**
	 * 
	 * */
	private class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			if (mIncomingHandler != null) {
				Log.i(TAG, "Incoming message. Passing to handler: "+msg);
				mIncomingHandler.handleMessage(msg);
			}
		}
	}
	
}