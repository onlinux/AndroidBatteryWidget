package fr.free.onlinux.AndroidBatteryWidget;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;
import android.widget.RemoteViews;

public class AndroidBatteryWidgetProvider extends AppWidgetProvider {
	private final static String TAG = "BAT";
	int rawlevelOld = 0;
	Boolean debug = false;
	SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss ");

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context,  intent);
		final String actionName = intent.getAction();
		if (debug) {
			Set<String> ks = intent.getExtras().keySet();
			Iterator<String> iterator = ks.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Log.d(TAG, "BAT "+ key + ": " + intent.getIntExtra(key, -1));
			}
			Log.d(TAG,"---------- onReceive " + actionName + " intent " + intent);
		}		
		if (actionName.equals(Intent.ACTION_BATTERY_CHANGED)) {						
			int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); 						
			if (rawlevel != rawlevelOld) {  			
				updateWidget(context, intent);
				Intent service = new Intent(context, BatteryIntentService.class);
				service.putExtras(intent);
				context.startService(service); // update DB SQlite
				rawlevelOld = rawlevel;
			}
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.i(TAG,"---------- onUpdate");
		context.getApplicationContext().registerReceiver(this,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));		
	}


	public void updateWidget(Context context, Intent batteryIntent){
		Log.i(TAG,"---------- updateWidget");
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

	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		Log.i(TAG,"---------- onDisabled ");		
		super.onDisabled(context);

	}
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		//super.onDeleted(context, appWidgetIds);
		Log.i(TAG,"---------- onDeleted ");
	}

}
