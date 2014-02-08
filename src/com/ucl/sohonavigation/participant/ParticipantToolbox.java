package com.ucl.sohonavigation.participant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.os.RemoteException;

import com.ucl.sohonavigation.service.CommandCodes;
import com.ucl.sohonavigation.service.ServiceManager;
import com.ucl.sohonavigation.service.TcpClient;
import com.ucl.sohonavigation.service.TcpService;

public class ParticipantToolbox {

	private ServiceManager service;
	
	public ParticipantToolbox() {
		
	}
	
	public void setService(ServiceManager service) {
		this.service = service;
	}

	protected int[] getCommandCodesFromString(String s) {
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
	
	protected void sendStringToService(int msgCode, String s) {
		try {      
			Message msg = Message.obtain(null, msgCode, s);
			service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendIntToService(int msgCode, int i) {
		try {      
			Message msg = Message.obtain(null, msgCode, i, 0);
			service.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendCommandToServer(int cmdCode) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_SM + ""+cmdCode);
	}
	
	protected void sendCommandToExperimenter(String s) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_EX + " " + s);
	}
	
	protected void sendEventToExperimenter(int routeNumber, int eventNumber, String s) {
		sendStringToService(TcpService.MSG_TCP_CMD, TcpClient.SERVER_CMD_CODE_PM + " @" + CommandCodes.CLIENT_NAME_EX + " " + s + ":" + routeNumber + ":" + eventNumber);
	}
	
	protected Bitmap decodeBitmap(File f) {

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        o.inDither = false; // Disable Dithering mode

        o.inPurgeable = true; // Tell to gc that when it needs free memory,
                                // the Bitmap can be cleared

        o.inInputShareable = true; // Which kind of reference will be used to
                                    // recover the Bitmap data after being
                                    // clear, when it will be used in the future
        try {
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // Find the correct scale value. It should be the power of 2.
        final int REQUIRED_SIZE = 768;
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 1.5 < REQUIRED_SIZE && height_tmp / 1.5 < REQUIRED_SIZE)
                break;
            width_tmp /= 1.5;
            height_tmp /= 1.5;
            scale *= 1.5;
        }

        // decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        // o2.inSampleSize=scale;
        o.inDither = false; // Disable Dithering mode

        o.inPurgeable = true; // Tell to gc that whether it needs free memory,
                                // the Bitmap can be cleared

        o.inInputShareable = true; // Which kind of reference will be used to
                                    // recover the Bitmap data after being
                                    // clear, when it will be used in the future
        // return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        try {
            Bitmap bitmap= BitmapFactory.decodeStream(new FileInputStream(f), null, null);         
            int iW = width_tmp;
            int iH = height_tmp;

            return Bitmap.createScaledBitmap(bitmap, iW, iH, true);

        } catch (OutOfMemoryError e) {
            // TODO: handle exception
            e.printStackTrace();
            // clearCache();

            // System.out.println("bitmap creating success");
            System.gc();
            return null;
            // System.runFinalization();
            // Runtime.getRuntime().gc();
            // System.gc();
            // decodeFile(f);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }
}
