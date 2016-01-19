package se.selborn.gps;

import java.util.UUID;

import se.selborn.common.MessageType;

import android.location.Location;

public class Position extends MessageType {

	private double latitude ;
	private double longitude;
	private double gpsSpeed;
	private long gpsDateTime;
    private double gpsBearing;
    private double gpsAltitude;

	private String guid ;
	
	public Position() {}
	
	public Position(Location loc, String appGuid) {
		
		this.setGpsDateTime(loc.getTime());
		this.setLatitude(loc.getLatitude());
		this.setLongitude(loc.getLongitude());
		this.setSpeed(loc.getSpeed());
        this.setGpsAltitude(loc.getAltitude());
        this.setGpsBearing(loc.getBearing());
        this.msgType = "POSITION";
        this.guid = appGuid;
		
	}

    public void setGpsBearing(double d) { gpsBearing = d;}
    public double getGpsBearing() { return gpsBearing; }

    public void setGpsAltitude (double d) { gpsAltitude = d;}
    public double getGpsAltitude () { return gpsAltitude; }

	public void setLatitude(double d){
		latitude = d;
	}
	public double getLatitude() {
		return latitude;
	}
	
	public void setLongitude(double d){
		longitude = d;
	}
	public double getLongitude() {
		return longitude;
	}
	
	public void setSpeed(double d) {
		gpsSpeed=d;
	}
	public double getSpeed(){
		return gpsSpeed;
	}

	public long getGpsDateTime() {
		return gpsDateTime;
	}

	public void setGpsDateTime(long gpsDateTime) {
		this.gpsDateTime = gpsDateTime;
	}

	public String getGuid() {
		return this.guid;
	}

}
