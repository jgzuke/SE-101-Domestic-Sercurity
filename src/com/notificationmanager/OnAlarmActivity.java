package com.notificationmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gc.materialdesign.views.ButtonFlat;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
	private FileOutputStream fileWrite;
	private FileInputStream fileRead;
	private int passwordEntered;
	private byte[] savedData = new byte[3];
	private int password;
	private AlertDialog.Builder builder;
	private AlertDialog passDialog;
	private EditText passInput;
	final private Context context = this;
	private LayoutInflater layoutInflater;
    String SENDER_ID = "durable-cacao-769";
    static final String TAG = "GCMDemo";
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
    static boolean alarmOn = true;
    private boolean[][] code;
    public static Activity activity;
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
		read();
		readSaveData();
		activity = this;
		Log.e("dsfsdfgjk", check);
		code = stringToBoolean(check);
		Toast.makeText(context, "Alarm Activated", Toast.LENGTH_LONG).show();
		layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
    	LinearLayout fullLayout = (LinearLayout) layoutInflater.inflate(R.layout.activity_on_alarm, null, false);
    	setContentView(fullLayout);
    	setKeepScreenOn(true);
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
    	((ButtonFlat) findViewById(R.id.cancelAlarm)).setOnClickListener(cancelAlarm);
    	((ButtonFlat) findViewById(R.id.callSercurity)).setOnClickListener(callSercurity);
	}
	private void setKeepScreenOn(boolean b) {
		// TODO Auto-generated method stub
		
	}
    /*
     * time finishes on alarm
     */
    public void timeUp()
    {
    	Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:2268688127"));
		context.startActivity(intent);
		cancelAlarm();
    }
    /*
     * creates alertdialog to choose a password
     */
    public void enterPasswordPrompt(boolean cancelable)
    {
    	buildPasswordPromptBox(cancelable, "Enter your 4 digit Passcode");
		passDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View set)
			{
				String passwordEnteredRaw = passInput.getText().toString();
					if(passwordEnteredRaw.length()!=4)
					{
						Toast.makeText(context, "Try Again (4 digits)", Toast.LENGTH_LONG).show();
						passInput.setText("");
					} else
					{
						passwordEntered = Integer.parseInt(passwordEnteredRaw);
						if(passwordEntered != password)
						{
							Toast.makeText(context, "Incorrect Passcode", Toast.LENGTH_LONG).show();
							passInput.setText("");
						} else
						{
							passDialog.dismiss();
							cancelAlarm();
						}
					}
			}
		});
    }
    public void buildPasswordPromptBox(boolean negativeButton, String title)
	{
		builder = new AlertDialog.Builder(this);
		passInput = new EditText(this);
		passInput.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		passInput.setText("");
		builder.setView(passInput);
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// User cancelled the dialog
			}
		});
		if(negativeButton)
		{
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int id)
				{
					// User cancelled the dialog
				}
			});
		}
		builder.setTitle(title);
		passDialog = builder.create();
		passDialog.show();
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
			 enterPasswordPrompt(true);
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
	/*
     * 			THIS IS THE CALL FROM THE ROBOT, INCLUDE 2D ARRAY WITH CODE, BOOLEAN B+W
     */
    public static void cancelAlarm()
    {
    	//Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_LONG).show();
    	alarmOn = false;
    	activity.finish();
    }
	public static class AlarmReciever extends WakefulBroadcastReceiver
	{
	    @Override
	    public void onReceive(Context context, Intent intent)
	    {
	    	Log.e("g",  intent.getStringExtra("identifier"));
	    	if(intent.getStringExtra("identifier").equals("0"))
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
	    	} else
	    	{
	    		if(intent.getStringExtra("correct_code").equals(0))
	    		{
	    			
	    		} else
	    		{
	    			cancelAlarm();
	    		}
	    	}
	        setResultCode(Activity.RESULT_OK);
	    }
	}
	/**
	 * set data to write it to save file
	 */
	public void setSaveData()
	{
		savedData[2] = (byte)(Math.floor(password/128));
		savedData[1] = (byte)(password-(128*savedData[2]));
	}
	/**
	 * read data once it has been put into savedData array
	 */
	public void readSaveData()
	{
		password = savedData[1]+(128*savedData[2]);
	}
    /**
	 * reads data from file and sets variables accordingly
	 */
	private void read()
	{
		openRead();
		try
		{
			fileRead.read(savedData, 0, 3);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		closeRead();
	}
	/**
	 * saves data to file
	 */
	private void write()
	{
		openWrite();
		try
		{
			fileWrite.write(savedData, 0, 3);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		closeWrite();
	}
	/**
	 * opens the save file to be read from
	 */
	private void openRead()
	{
		try
		{
			fileRead = openFileInput("ProjectSaveData");
		}
		catch(FileNotFoundException e)
		{
			openWrite();
			closeWrite();
			openRead();
		}
	}
	/**
	 * opens the save file to be written to
	 */
	private void openWrite()
	{
		try
		{
			fileWrite = openFileOutput("ProjectSaveData", Context.MODE_PRIVATE);
		}
		catch(FileNotFoundException e)
		{
		}
	}
	/**
	 * closes the save file from reading
	 */
	private void closeRead()
	{
		try
		{
			fileRead.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * closes the save file from writing
	 */
	private void closeWrite()
	{
		try
		{
			fileWrite.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}