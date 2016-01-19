package se.selborn.livelog;

import android.app.Activity;
import android.os.Bundle;

public class PassesActivity extends Activity {

	  protected void onCreate(Bundle savedInstanceState) {
	        
		  super.onCreate(savedInstanceState);
	      //setContentView(R.layout.passes);
	  }
	        
	  @Override
	  public void onPause() {
		  super.onPause();
	  }
	    
	  @Override
	  public void onStop () {
	  	 super.onStop();
	  }
	
	  @Override
	  public void onDestroy() {
		  super.onDestroy();
	  }
}
