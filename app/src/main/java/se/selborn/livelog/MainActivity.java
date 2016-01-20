package se.selborn.livelog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import se.selborn.bt.BTScanner;
import se.selborn.connection.GlobalObjects;
import se.selborn.gps.GpsEngine;
import se.selborn.gps.GpsMock;
import se.selborn.storage.DbStorage;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private GpsEngine gpsEngine = null;
    private GpsMock gpsMock=null;

	private DbStorage mDbStorage = null;
	private LiveLogManager mLiveLogManager = null;
	private BTScanner mBtScanner = null;
	
	private String TAG = "ACTIVITY_INFO_MAIN";
	
	private boolean mbUseBT = false;
	private boolean mSaveLocalDatabase = false;
	private static final int RESULT_SETTINGS = 1;
	
	private IntentFilter intFilter = new IntentFilter();
	
	private Location mCurrentLocation = null;
	private ArrayList<Location> mLocations = new ArrayList<Location>(2);
	private InformationObject mInfoObject = new InformationObject();
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			gpsEngine = null;
            gpsMock=null;


			Log.e(TAG, "onServiceDisconnected_GPS");
            Log.e(TAG, "onServiceDisconnected_GPS_MOCK");
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			GpsEngine.LocalBinder binder = (GpsEngine.LocalBinder) service;
			gpsEngine = binder.getService();
			
			Log.e(TAG, "onServiceConnected_GPS!");
			
		}
	};
	
	
	//THIS IS the callback from the service! The LOCATION comes here!
	private BroadcastReceiver mBroadcastReceivermBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			
			//Log.e(TAG, "RECEVED in BroadCast!");
			mCurrentLocation = null;
			mCurrentLocation  = (Location)intent.getParcelableExtra("POS");
			
			setSatteliteCounts(intent, mCurrentLocation == null ? false : true);
			
			int secsDiff = GlobalObjects.getSecondsDifference(mCurrentLocation.getTime(), new Date().getTime());


			
			if (secsDiff < 15) {
				mLocations.add(mCurrentLocation);
				
				if (mLocations.size() > 2 ){
					
					//Information to user!
					mInfoObject.setLocations(mLocations);
					setInformation(mInfoObject);
					
					if (mSaveLocalDatabase)
						DbStorage.savePosition(mCurrentLocation, GlobalObjects.applicationGuid.toString());
					
					while (mLocations.size() > 1) {
						mLocations.remove(mLocations.size() -1);
					}
					
					mLocations.add(mCurrentLocation);
				}
			}			
			
		}

		private void setSatteliteCounts(Intent intent, boolean hasFix) {
			
			String satCounts = intent.getStringExtra("SAT_COUNT");
			TextView txSatCount = (TextView) findViewById(R.id.txSatteliteCount);
			
			if (hasFix)
				txSatCount.setTextColor(Color.GREEN);
			else
				txSatCount.setTextColor(Color.WHITE);
			
			txSatCount.setText(satCounts);
			
		}

		
	};
	
	
	@Override
	protected void onStart() {
		super.onStart();
		Log.e(TAG, "StartingService...");
		
		Intent intent = new Intent(this, GpsEngine.class);
		bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Intent intentGpsEngine = new Intent(this, GpsEngine.class);
		//final Intent intentLiveLogmanager = new Intent (this, LiveLogManager.class);
		
		mDbStorage = new DbStorage(getApplicationContext());
		
		
		ImageButton btStart = (ImageButton) findViewById(R.id.StartGPS);
		btStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startService(intentGpsEngine);
				//startService(intentLiveLogmanager);
			}
		});

		
		
		ImageButton btStop = (ImageButton) findViewById(R.id.StopGPS);
		btStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gpsEngine.stopRunningGps();
			}
		});

		ImageButton btExit = (ImageButton) findViewById(R.id.AppExit);
		btExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				stopService(intentGpsEngine); //stannar service
				//stopService(intentLiveLogmanager);
				finish();
			}
		});

		
		
		parseServerSettings();
		
		
		intFilter.addAction("GPS_POS");
		intFilter.addAction("ANDERS");
		
		registerReceiver(mBroadcastReceivermBroadcastReceiver, intFilter);
		
		
	}
	
	//Trying to start at BT instance
	private void startBTInstance() {
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			return;
		}
		
		mBtScanner = new BTScanner(this);
		
		if (!mBtScanner.isBTESupported()) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			return;
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();

		//Register the listener
		registerReceiver(mBroadcastReceivermBroadcastReceiver, intFilter);

		parseServerSettings();
		
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle saveState) {
		super.onSaveInstanceState(saveState);
	}

	// Just display information
	public void setInformation(InformationObject info) {

		TextView txLat = (TextView) findViewById(R.id.txLatitud);
		TextView txLong = (TextView) findViewById(R.id.txLongitud);
		TextView txSpeed = (TextView) findViewById(R.id.txSpeed);
		TextView txMaxSpeed = (TextView) findViewById(R.id.txMaxSpeed);
		TextView txDistance = (TextView) findViewById(R.id.txDistance);
		TextView txTotMinutes = (TextView) findViewById(R.id.txMinutes);
		
		double lat = info.getLatitud();
		double lon = info.getLongitud();

		DecimalFormat df = new DecimalFormat("#.000000");
		String dlat = df.format(lat);
		String dLon = df.format(lon);

		
		//Setting Lat/Long
		txLat.setText(dlat);
		txLong.setText(dLon);
		
		
		//Setting textBoxes
		
		
		//SPEED
		double dSpeed = roundDoubleTo(info.getSpeed(), 10);
		String speed = String.valueOf(dSpeed);
		txSpeed.setText(speed + " km/h");
		
		//MAXSPEED
		double mxSpeed = roundDoubleTo(info.getMaxSpeed(), 10);
		String maxSpeed = String.valueOf(mxSpeed);
		txMaxSpeed.setText(maxSpeed + " km/h");
		
		//MEDELHAST 
		
		//TID.MIN
		String totMinutes = String.valueOf(info.getTotMinutes());
		txTotMinutes.setText(totMinutes + " minuter");
		
		//STRÄCKA (meter)
		double dist = roundDoubleTo(info.getAccDistance(), 10);
		
		//String distance = String.valueOf(info.getAccDistance());
		String distance = String.valueOf(dist);
		txDistance.setText(distance + " meter");
		
		Log.e(TAG, "setInformationTouser");
		//PULS
		
	}

	//Funkar bara med 10, 100, 1000, 10000
	private double roundDoubleTo(double dbValue, int noDecimals) {
		return (double) Math.round(dbValue * noDecimals) / noDecimals;
	}
	
	@Override
	//
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.appsettings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.Tidigare_Pass:
			Intent a = new Intent(this, PassesActivity.class);
			startActivity(a);
			return true;

		case R.id.menu_settings:
			Intent ip = new Intent(this, UserSettings.class);
			startActivityForResult(ip, RESULT_SETTINGS);
			
			return true;

		case R.id.app_settings:
			Intent ips = new Intent(this, AppSettings.class);
			startActivityForResult(ips, 1);
			
		case R.id.app_export:
			boolean isExported = mDbStorage.exportDBToStorageCard();
			if (isExported)
				Toast.makeText(getApplicationContext(), "Databas exporterades", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getApplicationContext(), "Misslyckades att exportera databas", Toast.LENGTH_SHORT).show();
				
			
		default:
			return super.onOptionsItemSelected(item);

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_SETTINGS:
			showUserSettings();
		}
	}

	private void parseServerSettings() {
		
		SharedPreferences sharedPrfs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StringBuilder bu = new StringBuilder();
		bu.append("\nIP: ")
				.append(sharedPrfs.getString("instServerIp", "NULL"));
		
		bu.append(" :")
			.append(sharedPrfs.getString("instServerPort", "NULL"));
		
		mbUseBT = sharedPrfs.getBoolean("instUseBT", false);
		mSaveLocalDatabase = sharedPrfs.getBoolean("instSaveDB", false);
		
		TextView ipSetting = (TextView) findViewById(R.id.txServerIp);
		ipSetting.setText(bu.toString());
		
		
		if (mbUseBT) {
			startBTInstance();
		}
		
		//GUID
		if (GlobalObjects.applicationGuid == null) {
			GlobalObjects.applicationGuid = java.util.UUID.randomUUID();
		}
		
	}

	private void showUserSettings() {

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StringBuilder bu = new StringBuilder();

		bu.append("\n Username: ").append(
				sharedPrefs.getString("prefUsername", "NULL"));
		bu.append("\n Send report ").append(
				sharedPrefs.getBoolean("prefSendReport", false));
		bu.append("\n Sync Frequency: ").append(
				sharedPrefs.getString("prefSyncFrequency", "NULL"));

		// TextView settingsTextView = (TextView)
		// findViewById(R.id.textUserSettings);
		// settingsTextView.setText(bu.toString());

	}

	@Override 
	public void onDestroy() {
		super.onDestroy();
		try { 
			
			//Unbind Services
			unbindService(serviceConnection);
			unregisterReceiver(mBroadcastReceivermBroadcastReceiver);
			
		} catch (Exception ep) {
			ep.printStackTrace();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

}
