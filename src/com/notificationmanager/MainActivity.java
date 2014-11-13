package com.notificationmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.gc.materialdesign.views.ButtonFloatSmall;
import com.gc.materialdesign.views.ProgressBarIndeterminate;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
	protected ImageLibrary imageLibrary;
	private boolean alarmActive = false;
	final private Context context = this;
	final private MainActivity activity = this;
	private int timeTillCall = 0;
	private Handler mHandler = new Handler();
	private AlertDialog.Builder builder;
	private AlertDialog dialog;
	private LayoutInflater layoutInflater;
	private int view = 0; //0:main, 1:
	private TextView timeText;
	private Runnable counterdownCaller = new Runnable()
	{
		public void run()
		{
			timeTillCall --;
			timeText.setText(Integer.toString(timeTillCall));
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
		setContentView(R.layout.mainscreen);
		((ButtonFloatSmall) findViewById(R.id.callAlarm)).setOnClickListener(callAlarm);
		passwordPrompt(savedData[0] != 0);
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
    	timeText = (TextView) fullLayout.getChildAt(1);
    	timeText.setTextSize(50);
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
    }
    /*
     * time finishes on alarm
     */
    public void timeUp()
    {
    	builder = new AlertDialog.Builder(this);
		ProgressBarIndeterminate progressBar = (ProgressBarIndeterminate) layoutInflater.inflate(R.xml.progressbar, null, false);
		builder.setView(progressBar);
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// User cancelled the dialog
			}
		});
		builder.setTitle("Calling Security");
		final AlertDialog dialog = builder.create();
		dialog.show();
    }
    /*
     * creates alertdialog to choose a password
     */
    public void passwordPrompt(final boolean alreadyChosen)
    {
    	builder = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		input.setText("");
		builder.setView(input);
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				// User cancelled the dialog
			}
		});
		builder.setTitle("Pick a 4 digit PassCode");
		if(alreadyChosen) builder.setTitle("Enter your 4 digit PassCode");
		final AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View set)
			{
				String passwordEnteredRaw = input.getText().toString();
				if(alreadyChosen)
				{
					if(passwordEnteredRaw.length()!=4)
					{
						Toast.makeText(context, "Try Again (4 digits)", Toast.LENGTH_LONG).show();
						input.setText("");
					} else
					{
						passwordEntered = Integer.parseInt(passwordEnteredRaw);
						if(passwordEntered != password)
						{
							Toast.makeText(context, "Incorrect Passcode", Toast.LENGTH_LONG).show();
							input.setText("");
						} else
						{
							dialog.dismiss();
							if(alarmActive)
							{
								alarmActive = false;
							}
							InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
						}
					}
				} else
				{
					if(passwordEnteredRaw.length()!=4)
					{
						Toast.makeText(context, "Pick Again (4 digits)", Toast.LENGTH_LONG).show();
						input.setText("");
					} else
					{
						password = Integer.parseInt(passwordEnteredRaw);
						Log.e("test", Integer.toString(password));
						savedData[0] = 1;
						setSaveData();
						write();
						dialog.dismiss();
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
					}
				}
			}
		});
    }
    View.OnClickListener callAlarm = new View.OnClickListener()
	{
		public void onClick(View v)
		{
			boolean [][] code = {{true, false, false},{false, true, false},{false, true, true}};
			activateAlarm(code);
		}
	};
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
