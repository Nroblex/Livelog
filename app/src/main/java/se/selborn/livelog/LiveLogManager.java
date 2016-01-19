package se.selborn.livelog;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import se.selborn.connection.GlobalObjects;
import se.selborn.gps.GpsEngine;
import se.selborn.gps.GpsEngine.LocalBinder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class LiveLogManager extends Service {

	private final IBinder mBinder = new LocalBinder();
	private String LIVE_TAG = "LiveLogManager";
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		return mBinder;
	}

	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.e(LIVE_TAG, "StartCommand for LiveLogger");	
		
			
		return Service.START_STICKY;
	}
	
	@Override
	public void onCreate() {
		
		Log.e(LIVE_TAG, "OnCreate LiveLogger!!");
		
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(LIVE_TAG, "Trying to shut down LiveLogger!!");
	}

	
	public class LocalBinder extends Binder {
		public LiveLogManager getLiveLogManager() {
			return LiveLogManager.this;
		}
	}
}
