package fr.free.onlinux.AndroidBatteryWidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryInfo extends BroadcastReceiver {
    private int prevLevel = -1;
    private Context mContext;
    
    public BatteryInfo(Context context){
    	mContext = context;
    	
    	IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(this, mIntentFilter);
    }
    
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.d(AndroidBatteryWidgetProvider.TAG,"---------- mBI onReceive() " );
        try
        {
			final String action = intent.getAction();		
	        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {  
	        	final int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
	        	if (prevLevel != level )
	                {
	        			Log.d(AndroidBatteryWidgetProvider.TAG,"---------- mBI onReceive() " + action + " prevLevel " + prevLevel);
	        			prevLevel = level;
	                    Intent serviceIntent = new Intent(context, AndroidBatteryWidgetProvider.UpdateService.class);
	                    serviceIntent.putExtras(intent);
	                    context.startService(serviceIntent);
	               }
	        }
        }catch(Exception e){Log.e(AndroidBatteryWidgetProvider.TAG, "", e);}
	}

}
