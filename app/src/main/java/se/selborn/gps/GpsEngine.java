package se.selborn.gps;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import se.selborn.connection.GlobalObjects;

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
import android.os.IBinder;
import android.util.Log;

public class GpsEngine extends Service implements LocationListener, Listener {

	private static String TAG ="GPSENGINE";
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SET_INT_VALUE = 3;
	public static final int MSG_SET_STRING_VALUE = 4;
	
	private final IBinder localBinder = new LocalBinder();
	final Intent intentPosition = new Intent("GPS_POS");
	
	
	private Timer timer = new Timer();
	
	private int sattelitesCount = 0, sattelitesCountInFix = 0;
	private float mAccuracy = 10; //fixmeter
	Location lastKnownLocation = null;
	LocationManager locationManager;
	
	

	@Override
	public IBinder onBind(Intent intent) {
		Log.e(TAG, "Executing onBind");
		return localBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		Log.e(TAG, "StartCommand: trying to start GPSReceiver");
		
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		
		Criteria criteria = new Criteria();
		
		String provider = locationManager.getBestProvider(criteria, false);
		lastKnownLocation = locationManager.getLastKnownLocation(provider);
		
		
		if (lastKnownLocation != null) {
			onLocationChanged(lastKnownLocation);
		}
		
		//Varannan sekund.
		//locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 2000, 2, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,2, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 2, this);

		locationManager.addGpsStatusListener(this);
		
		
		if (timer == null ) timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				
				//intentPosition.putExtra("BLABB", counter++);
				//sendBroadcast(intentPosition);
				String satCount = GlobalObjects.getSatteliteCountString(sattelitesCount, sattelitesCountInFix);
				GpsEngine.this.intentPosition.putExtra("SAT_COUNT", satCount);
				sendBroadcast(GpsEngine.this.intentPosition);
			}
		}, new Date(), 1000L);
		
		return Service.START_STICKY;
	}
	
	@Override
	public void onCreate() {
		
		Log.e(TAG, "OnCreate!!");
		
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.e(TAG, "Trying to shut down GPS Receiver");
		
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			locationManager.removeGpsStatusListener(this);
			locationManager = null;
		}
	}

	public class LocalBinder extends Binder {
		public GpsEngine getService() {
			return GpsEngine.this;
		}
	}
	

	
	public void stopRunningGps() {
		
		Log.e(TAG, "Trying to stop Running Gps");
		
		if (locationManager != null) {
			locationManager.removeUpdates(this);
			locationManager.removeGpsStatusListener(this);
			locationManager = null;
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
			intentPosition.putExtra("POS", location);
			sendBroadcast(intentPosition);
		}
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onProviderDisabled - gps turned off");
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onProviderDisabled - gps turned on");
	}

	
	//GPS STATUS//
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		
	}

	@Override
	public void onGpsStatusChanged(int event) {
		if (locationManager == null) return;
		
		sattelitesCount =0;
		sattelitesCountInFix =0;
		
		android.location.GpsStatus gpsStatus = locationManager.getGpsStatus(null);
		if (gpsStatus == null) return;
		
		Iterable<GpsSatellite> gpsSattelites = gpsStatus.getSatellites();
		for (GpsSatellite sats : gpsSattelites) {
			sattelitesCount++;
			if (sats.usedInFix()){
				sattelitesCountInFix++;
			}
		}
		
	}
	
//////END GPS STUFF//////
}
