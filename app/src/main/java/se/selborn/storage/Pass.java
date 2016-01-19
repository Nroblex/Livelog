package se.selborn.storage;

import java.util.Date;

public class Pass {

	private String guid;
	private Date startTime;
	private Date stopTime;
	private double distance;
	private double avgspeed;
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getStopTime() {
		return stopTime;
	}
	public void setStopTime(Date stopTime) {
		this.stopTime = stopTime;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public double getAvgspeed() {
		return avgspeed;
	}
	public void setAvgspeed(double avgspeed) {
		this.avgspeed = avgspeed;
	}
	
	
	
}
