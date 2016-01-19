package se.selborn.bt;

import se.selborn.livelog.MainActivity;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Build;

public class BTScanner {

	private MainActivity _mainActivity;
	private BluetoothAdapter _btAdp;
	
	private BluetoothManager _bleMngr ;
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public BTScanner(MainActivity mainActivity) {
	
		_mainActivity = mainActivity;
		
		_bleMngr = (BluetoothManager) _mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);
		_btAdp = _bleMngr.getAdapter();
		
	}

	public boolean isBTESupported() {
		return _btAdp == null ? false : true;
	}
	
}
