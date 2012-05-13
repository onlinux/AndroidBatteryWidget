package fr.free.onlinux.AndroidBatteryWidget;

import java.util.Iterator;
import java.util.Set;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class AndroidBatteryWidgetProvider extends AppWidgetProvider {
	private final static String TAG = "BAT";
	int rawlevelOld = 0;
	Boolean debug = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context,  intent);
		final String action = intent.getAction();
		if (debug) {
			Set<String> ks = intent.getExtras().keySet();
			Iterator<String> iterator = ks.iterator();
			while (iterator.hasNext()) {
				String key = iterator.next();
				Log.d(TAG, "BAT "+ key + ": " + intent.getIntExtra(key, -1));
			}
			Log.d(TAG,"---------- onReceive " + action + " intent " + intent);
		}		
		if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {						
			final int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); 						
			if (rawlevel != rawlevelOld) {  			
				Intent service = new Intent(context, BatteryIntentService.class);
				service.putExtras(intent);
				context.startService(service); // update DB SQlite and widget views
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

	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		Log.i(TAG,"---------- onDisabled ");		
		super.onDisabled(context);

	}

}
