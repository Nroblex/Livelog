package se.selborn.gps;


import se.selborn.common.ITickListener;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

public class GpsStatus implements LocationListener, Listener {

	static final int _HIST_LEN = 3;
	boolean _isFixed = false;
	Context _context = null;
	Location _historyLocations[] =null;
	LocationManager _locationManager = null;
	ITickListener _listner = null;
	
	float _fixAccurancy = 10;
	int _fixSattelites = 2;
	int _knownSattelitesCount = 0;
	int _usedInLastFixSattelitesCount = 0;
	int _fixTime=3;
	
	public GpsStatus(Context ctx) {
		this._context = ctx;
		_historyLocations = new Location[_HIST_LEN];
	}
	
	
	//START locationlistner
	public void start(ITickListener listner) {
		this._listner = listner;
		LocationManager lm = (LocationManager) _context.getSystemService(_context.LOCATION_SERVICE);
		try {
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		} catch (Exception ep) {
			lm = null;
		}
		
		if (lm==null){
			_locationManager = lm;
			_locationManager.addGpsStatusListener(this);
		}
	}
	
	//STOP locationListner
	public void stop(ITickListener listner) {
		this._listner = null;
		if (_locationManager != null){
			_locationManager.removeGpsStatusListener(this);
			_locationManager.removeUpdates(this);
			_locationManager = null;
		}
		clear();
	}
	
	private void clear() {
		_isFixed = false;
		_knownSattelitesCount = 0;
		_usedInLastFixSattelitesCount = 0;
		for (int i = 0; i < _HIST_LEN; i++)
			_historyLocations[i] = null;
	}
	
	@Override
	public void onGpsStatusChanged(int event) {
		if (_locationManager == null) {
			return;
		}
		
		android.location.GpsStatus gpsStatus = 
				_locationManager.getGpsStatus(null);
		
		if (gpsStatus ==null) 
			return;
		
		int cnt1=0, cnt2=0;
		Iterable<GpsSatellite> gpsSatteliteList = gpsStatus.getSatellites();
		for (GpsSatellite sattelite : gpsSatteliteList) {
			cnt1++;
			if (sattelite.usedInFix()){
				cnt2++;
			}
		}
		
		_knownSattelitesCount = cnt1;
		_usedInLastFixSattelitesCount = cnt2;
		
		if (_listner != null) {
			_listner.onTick();
		}
		
	}

	public boolean isFixed () { return _isFixed;}
	public int getSattelitesAvailable() { return _knownSattelitesCount;}
	public int getSattelitesInFix() { return _usedInLastFixSattelitesCount;}
	
	public boolean isEnabled() {
		LocationManager lm = (LocationManager) _context
				.getSystemService(_context.LOCATION_SERVICE);
		return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		for (int i = 1; i< _HIST_LEN; i++) {
			_historyLocations[i] = _historyLocations[i-1];
		}
		
		_historyLocations[0] = location;
		
		if (location.hasAccuracy() && location.getAccuracy() < _fixAccurancy){
			_isFixed = true;
		} else if (_historyLocations[1] != null 
				&& (location.getTime() - _historyLocations[1].getTime()) <= (1000 * _fixTime)) {
			_isFixed = true;
		} else if (_knownSattelitesCount >= _fixSattelites) {
			_isFixed=true;
		}
		
		if (_listner != null) {
			_listner.onTick();
		}
	
	}

	@Override
	public void onProviderDisabled(String provider) {
		if (provider.equalsIgnoreCase("gps")) {
			clear();
			if (_listner != null) {
				_listner.onTick();
			}
		}
	}

	@Override
	public void onProviderEnabled(String provider) {
		if (provider.equalsIgnoreCase("gps")) {
			clear();
			if (_listner != null) {
				_listner.onTick();
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		
		if (provider.equalsIgnoreCase("gps")) {
			if (status == LocationProvider.OUT_OF_SERVICE ||
					status == LocationProvider.TEMPORARILY_UNAVAILABLE){
				clear();
			}
			if (_listner != null){
				_listner.onTick();
			}
		}
		
	}

	

}
