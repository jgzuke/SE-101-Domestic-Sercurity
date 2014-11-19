package com.notificationmanager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class OnAlarm extends IntentService
{
    public OnAlarm() {
        super("OnAlarm");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);
        if (!extras.isEmpty())
        {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
            	Intent intentMakeActivity = new Intent(this, OnAlarmActivity.class);
            	//intentMakeActivity.putExtra("code", code);
            	//TODO with nakul get that code from bundle, maybe it can be in there from start
            	startActivity(intent);
            }
        }
    	AlarmReciever.completeWakefulIntent(intent);
    }
}