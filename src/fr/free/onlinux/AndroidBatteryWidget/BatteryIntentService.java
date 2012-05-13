package fr.free.onlinux.AndroidBatteryWidget;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.RemoteViews;


public class BatteryIntentService extends IntentService {
	private final static String TAG = "BAT-S";
	
	public BatteryIntentService(){
		super("BatteryIntentService");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		//First update widget views
		updateWidget(getApplicationContext(), intent);
		//Second, update database
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
	
	public void updateWidget(Context context, Intent batteryIntent){
		Log.i(TAG,"---------- updateWidget");
		SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss ");
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.androidbatterywidget_layout);
		updateViews.setTextViewText(R.id.level, "waiting!");

		int rawlevel = batteryIntent.getIntExtra("level", -1);
		double scale = batteryIntent.getIntExtra("scale", -1);
		double level = -1;
		if (rawlevel >= 0 && scale > 0) {
			level = rawlevel  * 100 / scale;
		}
		updateViews.setTextViewText(R.id.level, "" + level + " %" );
		updateViews.setTextViewText(R.id.time, formatter.format(new Date()));
		int temperature = batteryIntent.getIntExtra("temperature", -1);
		String tempString= String.format("%.1f°C", new Float(temperature/10));
		Log.d(TAG,"BAT:" + tempString + " " + level + "%");
		updateViews.setTextViewText(R.id.temperature, tempString );
		int voltage = batteryIntent.getIntExtra("voltage", -1);
		updateViews.setTextViewText(R.id.voltage, "" + voltage + " mV" );

		updateViews.setOnClickPendingIntent(R.id.layout ,
				PendingIntent.getActivity(context, 0, 
						new Intent(context, AndroidBatteryActivity.class),Intent.FLAG_ACTIVITY_NEW_TASK));

		ComponentName myComponentName = new ComponentName(context, AndroidBatteryWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myComponentName, updateViews);
	}

}
