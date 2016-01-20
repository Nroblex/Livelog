package se.selborn.connection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalObjects {
	
	private static Thread _connectorThread = null;
	private static Connector _connectorObject = null;	
	private final AtomicBoolean started = new AtomicBoolean(false);

	private static long firstTimePoint;
    private static long currentTimePoint;



	public static Connector getConnector() {
		
		if (_connectorObject == null){
			_connectorObject = new Connector();
		}
		return _connectorObject;
	}

	public static Thread getMessageThread() {
		
		if (_connectorObject == null)
			_connectorObject = getConnector();
		
		if (_connectorThread == null){
				_connectorThread = new Thread(_connectorObject, "MessagePump");
		}
		
		return _connectorThread;
	}
	
	public static void stopPump() {
		
		_connectorObject.setStop(true);
		
		if (_connectorObject != null) {
			_connectorObject = null;
		}
		if (_connectorThread != null) {
			_connectorThread = null;
		}
	}
	
	public static UUID applicationGuid ;
	
	public static int getMinutesDifference(long startTime, long stopTime) {
		return (int) ( (stopTime - startTime) / 1000 / 60);
	}
	
	public static int getSecondsDifference(long startTime, long stopTime) {
		return (int) 60 * getMinutesDifference(startTime, stopTime);
	}
	
	public static String getTimeAsString(long millisecDate){
		SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return mDateFormat.format(new Date(millisecDate));
	}
	
	public static String getSatteliteCountString(int sats, int satsInFix) {
		String ss = String.valueOf(satsInFix).concat(" av ").concat(String.valueOf(sats));
		
		return ss;
	}
	
}
