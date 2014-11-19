package com.notificationmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
