package com.ucl.sohonavigation;

import java.io.File;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.ucl.sohonavigation.helper.EventManager;

public class SohoApplication extends Application {
	
	private final static String TAG = "SohoApplication";
	
	public String role = null;
	public EventManager eM = null;
	
	@Override
	public void onCreate () {
		
	}

}
