package fr.free.onlinux.AndroidBatteryWidget;
import java.util.Calendar;

import android.app.IntentService;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;


public class BatteryIntentService extends IntentService {
	private final static String TAG = "BAT-S";
	
	public BatteryIntentService(){
		super("BatteryIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "BAT onHandleIntent: " + intent.getExtras());
		int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);        		
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		int batterylevel =  -1;
        if (rawlevel >= 0 && scale > 0) {
            batterylevel = (rawlevel * 100) / scale;                                     	
        }
        
		int plugged = intent.getIntExtra("plugged", 0);
//		int voltage = intent.getIntExtra("voltage", 0);
//		int temperature = intent.getIntExtra("temperature", -1);
		int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		
		DBHelper db = new DBHelper( this);
		db.record( batterylevel, status, plugged );
		db.deleteOldEntries();
		db.close();
		Log.i( TAG, "---------- Add record: " + batterylevel + " time: "+ Calendar.getInstance().getTimeInMillis() );
		

	}

}
