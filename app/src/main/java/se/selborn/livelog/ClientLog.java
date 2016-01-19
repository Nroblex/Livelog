package se.selborn.livelog;


import java.util.Date;

import se.selborn.common.MessageType;
import se.selborn.connection.GlobalObjects;

import com.google.gson.Gson;

public class ClientLog extends MessageType  {
	
	private String guid ;
	private String clientLogMessage ;
	private long timePoint;
	//public ClientLog() {}
	
	public long getTimePoint() { return timePoint ;}
	public void setTimePoint(long d) { timePoint = d;}
	
	public ClientLog() { this.msgType = "CLIENTLOG"; }
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String guid) {
		this.guid = guid;
	}
	public String getClientLogMessage() {
		return clientLogMessage;
	}
	public void setClientLogMessage(String clientLogMessage) {
		this.clientLogMessage = clientLogMessage;
	}

	
	public static void setClientLog(String logMsg, String guid) {
		
		Gson g = new Gson();
		ClientLog lg = new ClientLog();
		
		lg.setGuid(guid);
		lg.setClientLogMessage(logMsg);
		lg.setTimePoint(new Date().getTime());
		String jSon = g.toJson(lg);
		try {
			GlobalObjects.getConnector().setJson(jSon);
		} catch (Exception p) {
			p.printStackTrace();
		}
	}

	
}
