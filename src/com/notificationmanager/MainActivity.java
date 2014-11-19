package com.notificationmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	private FileOutputStream fileWrite;
	private FileInputStream fileRead;
	private byte[] savedData = new byte[3];
	private int password;
	private int passwordEntered;
	private boolean alarmActive = false;
	final private Context context = this;
	final private MainActivity activity = this;
	private int timeTillCall = 0;
	private Handler mHandler = new Handler();
	private AlertDialog.Builder builder;
	private AlertDialog passDialog;
	private EditText passInput;
	private LayoutInflater layoutInflater;
	private TextView timeText;
	public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String SENDER_ID = "durable-cacao-769";
    static final String TAG = "GCMDemo";
    TextView mDisplay;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;
    String regid;
	private Runnable counterdownCaller = new Runnable()
	{
		public void run()
		{
			timeTillCall --;
			timeText.setText("Time left: "+Integer.toString(timeTillCall));
			if(timeTillCall==0)
			{
				activity.timeUp();
			} else if(alarmActive)
			{
				mHandler.postDelayed(this, 1000);
			}
		}
	};
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
		read();
		if(savedData[0] != 0) readSaveData();
		layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
	    getActionBar().hide();
		setContentView(R.layout.mainscreen);
		((ButtonFlat) findViewById(R.id.callAlarm)).setOnClickListener(callAlarm);
		((ButtonFlat) findViewById(R.id.changePass)).setOnClickListener(changePassword);
		if(savedData[0]==0) setNewPassword();
		
		
		//GOOGLE PLAY STUFF
        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);
        if (regid.isEmpty())
        {
            registerInBackground();
        }
    }
    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
    	new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }
    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    	//TODO with nakul do this 
    }
    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    /*
     * 			THIS IS THE CALL FROM THE ROBOT, INCLUDE 2D ARRAY WITH CODE, BOOLEAN B+W
     */
    public void activateAlarm(boolean [][] code)
    {
    	Toast.makeText(context, "Alarm Activated", Toast.LENGTH_LONG).show();
    	alarmActive = true;
    	LinearLayout fullLayout = (LinearLayout) layoutInflater.inflate(R.layout.activealarm, null, false);
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
    	alarmActive = false;
    	LinearLayout fullLayout = (LinearLayout) layoutInflater.inflate(R.layout.mainscreen, null, false);
    	setContentView(fullLayout);
    	Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_LONG).show();
    	((ButtonFlat) findViewById(R.id.callAlarm)).setOnClickListener(callAlarm);
		((ButtonFlat) findViewById(R.id.changePass)).setOnClickListener(changePassword);
    }
    /*
     * time finishes on alarm
     */
    public void timeUp()
    {
    	alarmActive = false;
    	Intent intent = new Intent(Intent.ACTION_CALL);
		intent.setData(Uri.parse("tel:2268688127"));
		context.startActivity(intent);
		cancelAlarm();
    }
    /*
     * creates alertdialog to choose a password
     */
    public void pickPasswordPrompt(boolean cancelable)
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
							setNewPassword();
						}
					}
			}
		});
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
    View.OnClickListener callAlarm = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			boolean [][] code = {{true, false, false},{false, true, false},{false, true, true}};
			activateAlarm(code);
		}
	};
	View.OnClickListener changePassword = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			pickPasswordPrompt(true);
		}
	};
	public void setNewPassword()
	{
		buildPasswordPromptBox(true, "Pick a 4 digit Passcode");
		passDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View set)
			{
				String passwordEnteredRaw = passInput.getText().toString();
					if(passwordEnteredRaw.length()!=4)
					{
						Toast.makeText(context, "Pick Again (4 digits)", Toast.LENGTH_LONG).show();
						passInput.setText("");
					} else
					{
						password = Integer.parseInt(passwordEnteredRaw);
						Log.e("test", Integer.toString(password));
						savedData[0] = 1;
						setSaveData();
						write();
						passDialog.dismiss();
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        		imm.hideSoftInputFromWindow(passInput.getWindowToken(), 0);
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
