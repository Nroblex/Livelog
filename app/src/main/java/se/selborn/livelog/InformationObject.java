package se.selborn.livelog;

import java.util.ArrayList;

import se.selborn.connection.GlobalObjects;

import android.location.Location;

public class InformationObject {

	private static double _maxSpeed =0.0;
	private static int _totMinutes;
	private static double _accDistanceMeters = 0.0;
	
	private static Location _firstLocation, _currentLocation;
	private static InformationObject _infoObject;
	
	public InformationObject (){}
	
	//Set locations!
	public void setLocations(ArrayList<Location> locations) {	
		
		_currentLocation = locations.get(locations.size() -1);
		
		if (_firstLocation == null) {
			_firstLocation = locations.get(0);
		}
		
		if (_maxSpeed ==0.0) {
			_maxSpeed = _currentLocation.getSpeed();
		} else {
			_maxSpeed = _currentLocation.getSpeed() > _maxSpeed ? 
					_currentLocation.getSpeed() : _maxSpeed;
		}
		
		
		float distM = locations.get(locations.size() -1 ).distanceTo(locations.get(locations.size() -2));
		
		
		//Min avstånd mellan punkterna.
		if (distM > 5.0) {
			_accDistanceMeters += distM;
		}
		
		
		
	}

	public double getSpeed() {
		double kmh = 1.852 * _currentLocation.getSpeed();
		return kmh;
	}

	
	public double getLatitud() {
		return _currentLocation.getLatitude();
	}

	
	public double getLongitud() {
		return _currentLocation.getLongitude();
	}

	public double getAccDistance(){
		return _accDistanceMeters;
	}
	
	public double getMaxSpeed() {
		return _maxSpeed;
	}

	public double getAvgSpeed() {
		return 0.0;
	}

	public int getTotMinutes() {
		_totMinutes = GlobalObjects.getMinutesDifference(_firstLocation.getTime(), _currentLocation.getTime());
		return _totMinutes;
	}

	
	
}
