package com.ucl.sohonavigation.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import android.util.Log;

public class TcpClient {
	
	private final static String TAG = "TCP-Client";
	
	public static final String SERVER_CMD_CODE_LOGIN = "/login";
	public static final String SERVER_CMD_CODE_OK = "/ok";
	public static final String SERVER_CMD_CODE_QUIT = "/quit";
	public static final String SERVER_CMD_CODE_DISCONNECTED = "/bye";
	public static final String SERVER_CMD_CODE_ECHO = "/echo";
	public static final String SERVER_CMD_CODE_SM = "/sm"; // server message
	public static final String SERVER_CMD_CODE_PM = "/pm"; // private message
	public static final String SERVER_CMD_CODE_GM = "/gm"; // global message
	
	public static final String SERVER_TEST_CONNECTION = "test";
	public static final String SERVER_TEST_CONNECTION_REPLY = "test_reply";
	public static final String SERVER_START_EXPERIMENT = "start";
	public static final String SERVER_STOP_EXPERIMENT = "stop";
	
	public static final String SERVER_TOAST = "toast";
	public static final String SERVER_READY = "ready";
	
	private String mServerIp;
	private int mServerPort;
	private String mName;
	private String mServerMessage;
	private OnMessageReceived mMessageListener = null;
	private boolean mRun = false;
	private PrintWriter mOut;
	private BufferedReader mIn;
	
	/**
	 * Constructor for TCP Client
	 * @param messageListener
	 * @param serverIp
	 * @param serverPort
	 */
	public TcpClient(OnMessageReceived messageListener, String serverIp, int serverPort, String name) {
		mMessageListener = messageListener;
		mServerIp = serverIp;
		mServerPort = serverPort;
		mName = name;
	}
	
	/**
	 * Send a message to the TCP Server
	 * @param msg
	 */
	public void sendMessage(String msg) {
		if (mOut != null && !mOut.checkError()) {
			mOut.println(msg);
			mOut.flush();
		}
	}
	
	/**
	 * Stop the Client
	 */
	public void stopClient() {
		sendMessage(SERVER_CMD_CODE_QUIT);
		mRun = false;
		if (mOut != null) {
			mOut.flush();
			mOut.close();
		}
		mMessageListener = null;
		mIn = null;
		mOut=null;
		mServerMessage = null;
	}
	
	/**
	 * Code to execute when TCP Client is running
	 */
	public void run() {
		mRun = true;
		try {
			// resolve Server IP Address
			InetAddress serverAddr = InetAddress.getByName(mServerIp);
			Log.i(TAG, mName + " connecting to TCP Server");
			
			// create client socket
			Socket socket = new Socket(serverAddr, mServerPort);
			
			try {
				// get IO Streams from socket
				mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
				mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				mOut.println(SERVER_CMD_CODE_LOGIN + " " + mName);
				mOut.flush();
				
				mServerMessage = mIn.readLine();
								
				if (!mServerMessage.equals(SERVER_CMD_CODE_OK)) {
					//TODO QUIT
					mRun = false;
				} else {
					if (mServerMessage != null && mMessageListener != null) {
						mMessageListener.messageReceived(mServerMessage);
					}
				}
				
				Log.i(TAG, "Server login response " + mServerMessage);
				
				while (mRun) {
					mServerMessage = mIn.readLine();
					if (mServerMessage != null && mMessageListener != null) {
						mMessageListener.messageReceived(mServerMessage);
						if (mServerMessage.equals(SERVER_CMD_CODE_DISCONNECTED)) {
							break;
						}
					}
				}
			} catch (Exception e) {
				Log.e("TCP", "S: Error", e);
			} finally {
				socket.close();
			}
		} catch (Exception e) {
			Log.e("TCP", "C: Error", e);
		}
	}
	
	/**
	 * Implement this in the Activity/Service
	 */
	public interface OnMessageReceived {
		public void messageReceived(String message);
	}
}