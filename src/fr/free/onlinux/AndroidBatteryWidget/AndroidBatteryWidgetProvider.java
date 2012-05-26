package fr.free.onlinux.AndroidBatteryWidget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.app.PendingIntent;
import android.app.Service;


public class AndroidBatteryWidgetProvider extends AppWidgetProvider {
	public final static String TAG = "Bat";

	//private static final String TAG = AndroidBatteryWidgetProvider.class.getSimpleName();

	public static Boolean debug 	= true;	

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {		
		Log.i(TAG,"---------- onUpdate");	
		Intent intent = new Intent(context, UpdateService.class);
    	context.startService(intent);
	}
	

	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		Log.i(TAG,"---------- onDisabled ");		
		super.onDisabled(context);
		try
		{
			context.stopService(new Intent(context, UpdateService.class));//unregisterReceiver(mBI);
		}catch(Exception e){Log.e(TAG,"",e);}

	}
	
	
    public static class UpdateService extends Service {       
    	BatteryInfo mBI = null;
    	
    	public void updateWidget(Context context, Intent batteryIntent){
    		if (debug) Log.i(TAG,"---------- updateWidget");
    		SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss ");
    		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.androidbatterywidget_layout);
    		updateViews.setTextViewText(R.id.level, "waiting!");
    		final int status = batteryIntent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
    		final int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
    		final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
    		updateViews.setTextViewText(R.id.level, "" + level + " %" );
    		updateViews.setTextViewText(R.id.time, formatter.format(new Date()));
    		final int temperature = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
    		String tempString= String.format("%.0f°C", new Float(temperature/10));
    		if (debug) Log.d(TAG,"BAT:" + tempString + " " + level + "%");
    		updateViews.setTextViewText(R.id.temperature, tempString );
    		final int voltage = batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
    		updateViews.setTextViewText(R.id.voltage, "" + voltage + " mV" );

    		updateViews.setOnClickPendingIntent(R.id.layout ,
    				PendingIntent.getActivity(context, 0, 
    						new Intent(context, AndroidBatteryActivity.class),Intent.FLAG_ACTIVITY_NEW_TASK));

    		ComponentName myComponentName = new ComponentName(context, AndroidBatteryWidgetProvider.class);
    		AppWidgetManager manager = AppWidgetManager.getInstance(context);
    		manager.updateAppWidget(myComponentName, updateViews);
    		
    		//Second, update database 
    		

    		
    		DBHelper db = new DBHelper( this);
    		db.record( level, status, plugged );
    		db.deleteOldEntries();
    		db.close();
    		if (debug) Log.i( TAG, "---------- Add record: " + level + " time: "+ Calendar.getInstance().getTimeInMillis() );	
    	}

    	public void  handleCommand(Intent intent){
    		if(mBI == null)
	        {
	        	mBI = new BatteryInfo();
	        	IntentFilter mIntentFilter = new IntentFilter();
	            mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
	            registerReceiver(mBI, mIntentFilter);
	            // After registering mBI, another update is immediately processed.
	            // So, skip double update processing.
	            return;
	        } 
    		//update widget views and database
    		updateWidget(getApplicationContext(), intent);
    		
    	}
    	
    	
    	@Override
        public void onStart(Intent intent, int startId) {
    		handleCommand(intent);            
        }
    	
    	@Override
    	public int onStartCommand(Intent intent, int flags, int startId) {
    		if (debug)
    			Log.d(TAG, "----------------- onStartCommand intent ->" + intent.getAction());	
    		handleCommand(intent);     
            // We do not want this service to continue running until it is explicitly
            // stopped, so return not sticky.
    		// stopSelf();
            return START_STICKY;
    	}
    	
    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    		try{  				
    			if(mBI != null) {
    				if (debug)
        			Log.d(TAG, "----------------- onDestroy: unregisterReceiver(mBI)" );
    				unregisterReceiver(mBI);
    			}
    			
    		}catch(Exception e)
    		{Log.e(TAG, "", e);}
    	}
		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    }	

}
