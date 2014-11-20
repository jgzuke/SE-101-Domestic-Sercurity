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
    	String check = intent.getStringExtra("identifier");
    	String check1 = intent.getStringExtra("random_list");
    	String check2 = intent.getStringExtra("correct_code");
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                OnAlarm.class.getName());
        // Start the service, keeping the device awake while it is launching.
        Intent sendToNext = (intent.setComponent(comp));
        sendToNext.putExtra("whichCall", check);
        sendToNext.putExtra("codeString", check1);
        sendToNext.putExtra("isCorrectCode", check2);
        startWakefulService(context, sendToNext);
        setResultCode(Activity.RESULT_OK);
    }
}