package fr.free.onlinux.AndroidBatteryWidget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import android.app.PendingIntent;
import android.app.Service;


public class AndroidBatteryWidgetProvider extends AppWidgetProvider {
	public final static String TAG = "Bat";
	private int prevLevel = -1;
	//private static final String TAG = AndroidBatteryWidgetProvider.class.getSimpleName();
	
	
	public static Boolean debug 	= true;	
	public static BroadcastReceiver mBI = null;
	
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		mBI= this;
		Log.i(TAG,"---------- onEnabled");		
    	IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        context.getApplicationContext().registerReceiver(mBI, mIntentFilter);      
        Log.i(TAG,"---------- registerReceiver:"+ mBI);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		super.onReceive(context,  intent);
		final String action = intent.getAction();	
		Log.i(TAG,"--------------- onReceive " + action );
		try
        {		
	        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {  
	        	final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
	        	if (prevLevel != level )
	                {
	        			Log.d(AndroidBatteryWidgetProvider.TAG,"---------- onReceive() " + action + " prevLevel " + prevLevel);
	        			prevLevel = level;
	                    Intent serviceIntent = new Intent(context, UpdateService.class);
	                    serviceIntent.putExtras(intent);
	                    context.startService(serviceIntent);
	               }
	        }
        }catch(Exception e){Log.e(AndroidBatteryWidgetProvider.TAG, "", e);}
		
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {		
		Log.i(TAG,"---------- onUpdate");
	}
	

	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		Log.i(TAG,"---------- onDisabled unregisterReceiver: " + mBI);				
		try
		{
			context.getApplicationContext().unregisterReceiver(mBI);
		}catch(Exception e){Log.e(TAG,"",e);}
		super.onDisabled(context);
	}
	
	
    public static class UpdateService extends Service {       
    	//BatteryInfo mBI = null;
    	
    	public void updateWidget(Context context, Intent batteryIntent){
    		if (debug) Log.i(TAG,"---------- updateWidget");
    		SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss ");
    		RemoteViews updateViews = new RemoteViews(context.getPackageName(), R.layout.androidbatterywidget_layout);
    		updateViews.setTextViewText(R.id.level, "waiting!");
    		final int status = batteryIntent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN);
    		final int plugged = batteryIntent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
    		final int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
    		updateViews.setTextViewText(R.id.level, "" + level + " %" );
    		//updateViews.setTextViewText(R.id.time, formatter.format(new Date()));
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
    		
    		//Second, update database in a thread  		

    		new Thread (new Runnable(){
    			public void run(){
    				final Context c=getApplicationContext();
	    			DBHelper db = new DBHelper(c);
	    			db.record( level, status, plugged );
	    			db.deleteOldEntries();
	    			db.close();
	    			if (debug) Log.i( TAG, "---------- Add record: " + level + " time: "+ Calendar.getInstance().getTimeInMillis() );	
    			}
    		}).start();

    	}

    	public void  handleCommand(Intent intent){
    		/*if(mBI == null)
	        {
	        	//mBI = new BatteryInfo(this);
	            // After registering mBI, another update is immediately processed.
	            // So, skip double update processing.
	            return;
	        } */
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
    			Log.d(TAG, "----------------- onStartCommand"); 	
    		handleCommand(intent);     
    		stopSelf();
			return START_NOT_STICKY;
    	}
    	
    	@Override
    	public void onDestroy() {
    		super.onDestroy();

    	}
		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}
    }	

}
