package se.selborn.gps;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by ueh093 on 1/20/16.
 */
public class GpsMock extends Service implements LocationListener {

    private static final String TAG = "GPS_MOCK_SERVICE";
    private final Activity activity;
    LocationManager locationManager;


    private final IBinder localBinder = new LocalBinder();
    final Intent intentPosition = new Intent("GPS_POS_MOCK");


    public GpsMock(Activity activity){
        this.activity = activity;
        initMock();
    }

    private void initMock() {
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        locationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                false, false, false, false, true, true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);

    }

    private void setMockLocation(double latitude, double longitude, float speed, double altitude, float bearing){

        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speed);
        location.setAltitude(altitude);
        location.setBearing(bearing);

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null, System.currentTimeMillis());

        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);



    }

    @Override
    public void onLocationChanged(Location location) {



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    //Service Implementation!

    public int onStartCommand(Intent intent, int flags, int startId) {

        

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "OnBindMockService");
        return localBinder;
    }

    public class LocalBinder extends Binder {
        public GpsMock getService() {
            return GpsMock.this;
        }
    }

}
