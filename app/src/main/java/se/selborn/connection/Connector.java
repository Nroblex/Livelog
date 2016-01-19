package se.selborn.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;

import android.net.ConnectivityManager;
import android.util.Log;

public class Connector implements Runnable {

	private Socket _dataSocket ;
	private boolean _mustStop = false;
	
	String _jSonSend = null;
	String _serverIp = null;

	public void setJson(String json) {_jSonSend = json;	}
	public void setStop (boolean mustStop) { _mustStop = mustStop ;}
	
	
	public boolean setServerIp(String d) {
		if (_serverIp != null) {
			return _serverIp == d ? false : true;
		} else {
			_serverIp = d;
			return false;
		}
		
		
	}
	
	public boolean isServerAlive(String serverIp) {
		return false;
	}
	
	
	
	
	@Override
	public void run() {
	
		Log.i("Connector", String.format("Sending to serverip %s", _serverIp));
		while (!_mustStop) {
			
			if (_jSonSend != null) {
				
				send(_jSonSend);
				
				_jSonSend = null;
			}
		}
		
		if (_mustStop) { //Forcing the connection to go down!
			try {
				
				_dataSocket.close();
				_dataSocket = null;
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

	
	private boolean isSocketConnected() {
		
		if (_dataSocket == null){
			
			try {
				
				InetAddress addr = InetAddress.getByName(_serverIp);
				_dataSocket = new Socket(addr, 12111);
				
				
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return false;
			} catch (IOException e) {
				Log.e("SocketConnection", String.format("Kan inte ansluta socket på ip %s", _serverIp));
				return false;
			}

		}
		
		return _dataSocket.isConnected();
	}
	
	//Sending actual json to remote site.
	private void send(String _jSon) {
		
		
		byte[] barr = _jSon.getBytes();
		try {
	
			if (isSocketConnected()){
		
				Log.i("Actually Sending JSON", _jSon);
				
				OutputStream os = _dataSocket.getOutputStream();
				os.write(barr, 0, barr.length);
				os.flush();
			} else {
				Log.i("NOT connected!", "noJson to server");
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
