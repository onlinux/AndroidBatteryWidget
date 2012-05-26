package fr.free.onlinux.AndroidBatteryWidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryInfo extends BroadcastReceiver {
    private int prevLevel = -1;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        try
        {
			String action = intent.getAction();		
	        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {  
	        	final int level = intent.getIntExtra("level", 0);
	        	if (prevLevel != level )
	                {
	        			Log.d(AndroidBatteryWidgetProvider.TAG,"---------- Bmi onReceive " + action + " prevLevel " + prevLevel);
	        			prevLevel = level;
	                    Intent serviceIntent = new Intent(context, AndroidBatteryWidgetProvider.UpdateService.class);
	                    serviceIntent.putExtras(intent);
	                    context.startService(serviceIntent);
	               }
	        }
        }catch(Exception e){Log.e(AndroidBatteryWidgetProvider.TAG, "", e);}
	}

}
