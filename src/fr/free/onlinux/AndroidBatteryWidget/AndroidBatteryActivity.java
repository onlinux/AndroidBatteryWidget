package fr.free.onlinux.AndroidBatteryWidget;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class AndroidBatteryActivity extends Activity {
    public Intent intent ;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		intent = new BatteryChart().execute(this);
	       startActivity(intent);
	       finish();
	}
	
	public void myfinish(){
		Log.i("AndroidBatteryActivity", "finish");
		finish();
	}
}
