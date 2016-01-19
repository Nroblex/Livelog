package se.selborn.gps;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import se.selborn.connection.GlobalObjects;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class GpsEngine extends Service implements LocationListener, Listener {

	private static String GPS_TAG="GPSENGINE";
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	
	private final IBinder mBinder = new LocalBinder();
	final Intent in = new Intent("GPS_POS");
	
	
	private Timer timer = new Timer();
	
	private int mSattelitesCount = 0, mSattelitesCountInFix = 0;
	private float mAccuracy = 10; //Antal meter för fix
	Location mLastKnownLocation = null;
	LocationManager mLm ;
	
	

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(GPS_TAG, "Executing onBind");
		return mBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.e(GPS_TAG, "StartCommand: trying to start GPSReceiver");	
		
		mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		
		String provider = mLm.getBestProvider(criteria, false);
		mLastKnownLocation = mLm.getLastKnownLocation(provider);
		
		
		if (mLastKnownLocation != null) {
			onLocationChanged(mLastKnownLocation);
		}
		
		//Varannan sekund.
		mLm.requestLocationUpdates(mLm.GPS_PROVIDER, 2000, 2, this);
		mLm.addGpsStatusListener(this);
		
		
		if (timer == null ) timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				
				//in.putExtra("BLABB", counter++);
				//sendBroadcast(in);
				
				
				String satCount = GlobalObjects.getSatteliteCountString(mSattelitesCount, mSattelitesCountInFix);
				in.putExtra("SAT_COUNT", satCount);
				sendBroadcast(in);
			}
		}, new Date(), 1000L);
		
		return Service.START_STICKY;
	}
	
	@Override
	public void onCreate() {
		
		Log.e(GPS_TAG, "OnCreate!!");
		
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e(GPS_TAG, "Trying to shut down GPS Receiver");
		
		if (mLm != null) {
			mLm.removeUpdates(this);
			mLm.removeGpsStatusListener(this);
			mLm = null;
		}
	}

	public class LocalBinder extends Binder {
		public GpsEngine getService() {
			return GpsEngine.this;
		}
	}
	

	
	public void stopRunningGps() {
		
		Log.e(GPS_TAG, "Trying to stop Running Gps");
		
		if (mLm != null) {
			mLm.removeUpdates(this);
			mLm.removeGpsStatusListener(this);
			mLm = null;
		}
		
		if (timer != null) { 
			timer.cancel();
			timer = null;
		}
	}
	
	//////GPS STUFF//////
	
	@Override
	public void onLocationChanged(Location location) {
		
		//Sending positions if accuracy is fullfilled.
		if (location.hasAccuracy() && mAccuracy < location.getAccuracy()) {
			in.putExtra("POS", location);
			sendBroadcast(in);
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.e(GPS_TAG, "onProviderDisabled - gps turned off");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.e(GPS_TAG, "onProviderDisabled - gps turned on");
	}

	
	//GPS STATUS//
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		
	}

	@Override
	public void onGpsStatusChanged(int event) {
		if (mLm == null) return;
		
		mSattelitesCount=0;mSattelitesCountInFix=0;
		
		android.location.GpsStatus gpsStatus = mLm.getGpsStatus(null);
		if (gpsStatus == null) return;
		
		Iterable<GpsSatellite> gpsSattelites = gpsStatus.getSatellites();
		for (GpsSatellite sats : gpsSattelites) {
			mSattelitesCount++;
			if (sats.usedInFix()){
				mSattelitesCountInFix++;
			}
		}
		
	}
	
//////END GPS STUFF//////
}
