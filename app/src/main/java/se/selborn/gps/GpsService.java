package se.selborn.gps;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.R;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/*
 * Anders Selborn
 *  The purpose of this is to run even when App is in sleep.
 */

public class GpsService extends Service implements LocationListener {

	private final String GPS_TAG = "GPS";
	private static boolean isRunning = false;
	private LocationManager mLm;
	private boolean xmlSuccessful = false;
	private boolean locationTimeExpired=false;
	
	private NotificationManager mNotificationManager = null;
	
	private double latitude = 0.0;
	private double longitude = 0.0;
	
	private int counter = 0, incrementby = 1;
	private Timer mTimer = new Timer();
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	
	final Messenger mMessenger = new Messenger(new IncommingHandler());
	
	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
	int mValue = 0; // Holds last value set by a client.
	 
	
	
	
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(GPS_TAG, "onBind");
		return mMessenger.getBinder();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.i(GPS_TAG, "onLocationChanged");
		
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		
		new SubmitLocationTask().execute(latitude, longitude);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i(GPS_TAG, "onProviderDisabled");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	/*
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(GPS_TAG, "ServiceSTarted");
		showNotification();
		
		mTimer.scheduleAtFixedRate(new TimerTask(){ public void run() {onTimerTick();}}, 0, 1000L);
		isRunning=true;
		
		android.os.Debug.waitForDebugger();
		
	}
	*/
	private void onTimerTick() {
		Log.i(GPS_TAG, "Timer doing work");
		try {
			
			counter += incrementby;
			sendMessageToUI(counter);
			
		} catch (Throwable t) {
			Log.e(GPS_TAG, "timer tick failed", t);
		}
		
	}
		
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
	
		
		Log.e(GPS_TAG, "onStart");
		
		mLm = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
		mLm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		mLm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 300f, this);
		
		Log.i(GPS_TAG, mLm.toString());
		
		
		return START_STICKY;
	}
	
	
	
	public static boolean isRunning()
    {
        return isRunning;
    }

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		Log.i(GPS_TAG, "ONDESTROY!!");
		if (mTimer != null) { mTimer.cancel();}
		counter = 0;
		
		isRunning=false;

		if (mLm != null)
			mLm.removeUpdates(GpsService.this);
	}
	
	private void sendMessageToUI(int intvaluetosend) {
		
		for (int i = mClients.size() -1; i>=0; i--) {
			
			try {
				
				mClients.get(i).send(Message.obtain(null, 
						MSG_SET_INT_VALUE, intvaluetosend, 0));
				
				Bundle bundle = new Bundle();
				bundle.putString("str1",  "ab" + intvaluetosend + "cd");
				Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
				msg.setData(bundle);
				mClients.get(i).send(msg);
				
				
			} catch (RemoteException e) {
				mClients.remove(i);
			}
			
		}
	}
	
	private void showNotification(){
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	}

	private class SubmitLocationTask extends AsyncTask<Double, Double, Position> {

		
		
		
		@Override
		protected Position doInBackground(Double... params) {
			
			Position pos = new Position();
			pos.setLatitude(latitude);
			pos.setLongitude(longitude);
			
			Log.e(GPS_TAG, "RETURNING DATA");
			
			return pos;
		}

	}
	
	class IncommingHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_SET_INT_VALUE:
				incrementby = msg.arg1;
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	
	
}
