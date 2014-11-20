package com.notificationmanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class AlarmReciever extends WakefulBroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	Log.e("g",  intent.getStringExtra("random_list"));
    	String check = intent.getStringExtra("random_list");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                OnAlarm.class.getName());
        // Start the service, keeping the device awake while it is launching.
        Intent sendToNext = (intent.setComponent(comp));
        sendToNext.putExtra("code", check);
        startWakefulService(context, sendToNext);
        setResultCode(Activity.RESULT_OK);
    }
}