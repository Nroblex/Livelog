package se.selborn.gps;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import se.selborn.connection.Connector;
import se.selborn.connection.GlobalObjects;
import se.selborn.livelog.ClientLog;
import se.selborn.livelog.InformationObject;
import se.selborn.livelog.MainActivity;
import se.selborn.storage.DbStorage;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class GpsFetcher implements LocationListener {
	private MainActivity _mainActivity = null;
	
	private Gson gs = new Gson();
	//private Connector _appConnector = new Connector();
	private Position aPostion = new Position();
	private ArrayList<Location> _locations = new ArrayList<Location>();
	//private InformationObject _infoObject = InformationObject.initializeInformationObject();
	
	private boolean _saveLocalDB = false;
	private boolean _sendLiveData = false;
	private String _serverIp = null;
	
	private LocationManager _locationManager;
	private String _priovider = "";
	private Location _lastKnownLocation = null;
	
	public GpsFetcher(MainActivity main) {
		
		_mainActivity = main;
		
		parseSettings();
		
		if (GlobalObjects.getMessageThread().getState().compareTo(Thread.State.NEW) == 0) {
			GlobalObjects.getMessageThread().start();
		}
		
	}
	
	
	
	@Override
	public void onLocationChanged(Location location) {
		
		parseSettings();
		createNewGUID();
		
		aPostion = new Position(location, GlobalObjects.applicationGuid.toString());
		_locations.add(location);
		
		
		if (_locations.size() == 2){
			
			while (_locations.size() > 1) {
				_locations.remove(_locations.size() -1);
			}
			_locations.add(location);
		}
			
		//Information on the MainActivity
		if (location.getAccuracy() == 0.0)
		{
			Log.i("ACCURACY", "NO GPSFIX");
		}
		
		if (_locations.size() > 1  ) {
			
			//InformationObject.setLocations(_locations);
			//_mainActivity.setInformation(_infoObject);
						
			//Saving to local database ?
			if (_saveLocalDB) {
				
				if (GlobalObjects.applicationGuid != null)
					DbStorage.savePosition(location, GlobalObjects.applicationGuid.toString());
				
			}
			
			
			//ArrayList<Position> positions = DbStorage.getPositions(50, GlobalObjects.applicationGuid.toString());
			
			//Log.i("INFO_COUNT", String.valueOf(positions.size()));
			
			
			/*
			if (_sendLiveData) {
				
				Log.i("LIVE", "Sending livedata");
				String jsonSend = gs.toJson(aPostion);
				GlobalObjects.getConnector().setJson(jsonSend);
				
			}
			*/
		} 
		
		
		
		
		
	}



	private void createNewGUID() {
		if (GlobalObjects.applicationGuid == null) {			
			GlobalObjects.applicationGuid = java.util.UUID.randomUUID();
		}
	}

	private void parseSettings(){
		SharedPreferences prfs = PreferenceManager.getDefaultSharedPreferences(_mainActivity);
		
		_saveLocalDB = prfs.getBoolean("instSaveDB", false);
		_sendLiveData = prfs.getBoolean("instSendLive", false);
		_serverIp = prfs.getString("instServerIp", "NULL");
		
		boolean newConnectionNeeded = GlobalObjects.getConnector().setServerIp(_serverIp);
	
		if (newConnectionNeeded)
		{
			Log.i("ServerIp has changed", "New Connection requiered" );
			ClientLog.setClientLog(String.format("ServerConnection has changed , new connection needed to ip = %s", _serverIp) , 
					GlobalObjects.applicationGuid.toString()); 
			
			GlobalObjects.stopPump();
			Log.i("ParseSettings", "SettingServerIp to " +  _serverIp);
			GlobalObjects.getConnector().setServerIp(_serverIp);
			
			if (GlobalObjects.getMessageThread().getState().compareTo(State.NEW) == 0) {
				GlobalObjects.getMessageThread().start();
			}
		}
		
	}
	
	private boolean isGpsTimeValid(long gpsTime) {
		
		Date dtNow = Calendar.getInstance().getTime();
		Date dtGps = new Date(gpsTime);
		
		long dif = dtNow.getTime() - gpsTime;
		
		return dif > 25000 ? false : true;
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
	}

	
	public void startGPS() {
		
		Log.i("StartingGPS", "Trying to livelogging!");
		if (GlobalObjects.applicationGuid == null) { createNewGUID();}
		
		ClientLog.setClientLog("StartGPS was hit from user", GlobalObjects.applicationGuid.toString());
		
		_saveLocalDB = true;
		_locationManager = (LocationManager)_mainActivity.getSystemService(Context.LOCATION_SERVICE);
			
		Criteria criteria = new Criteria();
		
		_priovider = _locationManager.getBestProvider(criteria, false);
		_lastKnownLocation = _locationManager.getLastKnownLocation(_priovider);
		
		if (_lastKnownLocation != null) {
			onLocationChanged(_lastKnownLocation);
		}
		
		//Varannan sekund.
		_locationManager.requestLocationUpdates(_locationManager.GPS_PROVIDER, 2000, 0, this);
	}
	
	
		/* Remove the locationlistener updates when Activity is paused */
	  public void onStopGPS() {
		  
		  if (GlobalObjects.applicationGuid == null) {
			  return;
		  }
			  
		  ClientLog.setClientLog("StopGPS was hit from user", GlobalObjects.applicationGuid.toString());
		  _saveLocalDB = false;
		  _sendLiveData = false;
		  
		  if (_locationManager != null) {
			  _locationManager.removeUpdates(this);
		  }
		  
		  GlobalObjects.applicationGuid = null;
	  }
	
	 
}
