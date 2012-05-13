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
		final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);        		       
		final int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
//		final int voltage = intent.getIntExtra("voltage", 0);
//		final int temperature = intent.getIntExtra("temperature", -1);
		final int status = intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
		
		DBHelper db = new DBHelper( this);
		db.record( level, status, plugged );
		db.deleteOldEntries();
		db.close();
		Log.i( TAG, "---------- Add record: " + level + " time: "+ Calendar.getInstance().getTimeInMillis() );	
	}
	
	public void updateWidget(Context context, Intent batteryIntent){
		Log.i(TAG,"---------- updateWidget");
		SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss ");
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.androidbatterywidget_layout);
		updateViews.setTextViewText(R.id.level, "waiting!");
		
		//final boolean plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0;		
		
		final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		updateViews.setTextViewText(R.id.level, "" + level + " %" );
		updateViews.setTextViewText(R.id.time, formatter.format(new Date()));
		final int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
		String tempString= String.format("%.0f°C", new Float(temperature/10));
		Log.d(TAG,"BAT:" + tempString + " " + level + "%");
		updateViews.setTextViewText(R.id.temperature, tempString );
		final int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
		updateViews.setTextViewText(R.id.voltage, "" + voltage + " mV" );

		updateViews.setOnClickPendingIntent(R.id.layout ,
				PendingIntent.getActivity(context, 0, 
						new Intent(context, AndroidBatteryActivity.class),Intent.FLAG_ACTIVITY_NEW_TASK));

		ComponentName myComponentName = new ComponentName(context, AndroidBatteryWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myComponentName, updateViews);
	}

}
