package com.notificationmanager;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;













import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class OnAlarmActivity extends Activity
{
	private TextView timeText;
	final private Context context = this;
	private Handler mHandler = new Handler();
	private int timeTillCall = 0;
	private LayoutInflater layoutInflater;
    String SENDER_ID = "durable-cacao-769";
    static final String TAG = "GCMDemo";
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
    private boolean[][] code;
	private Runnable counterdownCaller = new Runnable()
	{
		public void run()
		{
			timeTillCall --;
			timeText.setText("Time left: "+Integer.toString(timeTillCall));
			if(timeTillCall==0)
			{
				timeUp();
			} else
			{
				mHandler.postDelayed(this, 1000);
			}
		}
	};
	private boolean [][] stringToBoolean(String s)
	{
		boolean [][] code = new boolean[3][3];
		for(int i = 0; i < 3; i ++)
		{
			for(int j = 0; j < 3; j++)
			{
				int index = 1+(6*i)+(2*j);
				code[i][j] = (s.charAt(index)=='1');
			}
		}
		return code;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Log.e("dsfsdfgjk", check);
		code = stringToBoolean(check);
		Toast.makeText(context, "Alarm Activated", Toast.LENGTH_LONG).show();
		layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    	LinearLayout fullLayout = (LinearLayout) layoutInflater.inflate(R.layout.activity_on_alarm, null, false);
    	setContentView(fullLayout);
    	TableLayout grid = (TableLayout) fullLayout.getChildAt(0);
    	for(int i = 0; i < 3; i++)
    	{
    		TableRow row = (TableRow) layoutInflater.inflate(R.xml.checkerrow, grid, false);
    		for(int j = 0; j < 3; j++)
        	{
    			if(code[i][j])row.getChildAt(j).setBackgroundColor(Color.BLACK);
    			else row.getChildAt(j).setBackgroundColor(Color.WHITE);
        	}
    		grid.addView(row);
    	}
    	timeText = (TextView) findViewById(R.id.time);
    	((ButtonFlat) findViewById(R.id.cancelAlarm)).setOnClickListener(cancelAlarm);
    	((ButtonFlat) findViewById(R.id.callSercurity)).setOnClickListener(callSercurity);
    	timeTillCall = 10;
    	counterdownCaller.run();
	}
	/*
     * 			THIS IS THE CALL FROM THE ROBOT, INCLUDE 2D ARRAY WITH CODE, BOOLEAN B+W
     */
    public void cancelAlarm()
    {
    	Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_LONG).show();
		//Sends message back to server saying it was cancelled
		//TODO mod with nakul
    	//Maybe just this, or do we need more?
		/*new AsyncTask<Void, Void, String>()
		{
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                        data.putString("my_message", "ALARM CANCELLED");
                        String id = Integer.toString(msgId.incrementAndGet());
                        gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);//TODO change
                        msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);*/
    	finish();
    }
    /*
     * time finishes on alarm
     */
    public void timeUp()
    {
    	Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:2268688127"));
		//context.startActivity(intent);
		cancelAlarm();
    }
    
    
    
    

    View.OnClickListener callSercurity = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			 timeUp();
		}
	};
    View.OnClickListener cancelAlarm = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			 cancelAlarm();
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.on_alarm, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	static String check;
	public static class AlarmReciever extends WakefulBroadcastReceiver
	{
	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
	    	Log.e("g",  intent.getStringExtra("random_list"));
	    	check = intent.getStringExtra("random_list");
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
}
