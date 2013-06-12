package cs.softwarearchitecture.eventcal;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

public class AddEventActivity extends Activity implements OnClickListener {
	
	// Event Group
	public static final String PERSONAL = "PERSONAL";
	
	Calendar mDateTime = Calendar.getInstance();

	SimpleDateFormat mDateFormatter = new SimpleDateFormat("MMMM dd yyyy");
	SimpleDateFormat mTimeFormatter = new SimpleDateFormat("hh:mm");
	
	// Reminder value
	private String mTitle;
	private int mFromDate = 0;
	private int mFromTime = 0;
	private int mToDate = 0;
	private int mToTime = 0;
	private int mReminder = 0;
	
	public static String TAG = "Add Event";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// Setting the text of the Buttons to current date and time
		Button txtDate = (Button) findViewById(R.id.fromDate);
		Button txtTime = (Button) findViewById(R.id.fromTime);

		txtDate.setText(mDateFormatter.format(mDateTime.getTime()));   
		txtTime.setText(mTimeFormatter.format(mDateTime.getTime()));
		
		txtDate = (Button) findViewById(R.id.toDate);
		txtTime = (Button) findViewById(R.id.toTime);
		
		// Setting initial value of variables
		mFromDate = timeDateFormatter(mDateTime.DAY_OF_MONTH, mDateTime.MONTH, Integer.toString(mDateTime.YEAR));
		mFromTime = timeDateFormatter(mDateTime.HOUR_OF_DAY, mDateTime.MINUTE, "00");

		txtDate.setText(mDateFormatter.format(mDateTime.getTime()));   
		txtTime.setText(mTimeFormatter.format(mDateTime.getTime()));
		
		((Button) findViewById(R.id.fromDate)).setOnClickListener(this);
		((Button) findViewById(R.id.fromTime)).setOnClickListener(this);
		((Button) findViewById(R.id.toDate)).setOnClickListener(this);
		((Button) findViewById(R.id.toTime)).setOnClickListener(this);
		
		Button save = (Button) findViewById(R.id.saveButton);
		Button cancel = (Button) findViewById(R.id.cancelButton);
		
		save.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			super.onBackPressed();
			break;
		}
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(final int iD)
	{
		switch (iD)
		{
		case R.id.fromDate:
		case R.id.toDate:
			final Button txtDate = (Button) findViewById(iD);
			return new DatePickerDialog(this, new OnDateSetListener()
			{

				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth)
				{
					mDateTime.set(year, monthOfYear, dayOfMonth);
					
//					String dateFormatted = Integer.toString(dayOfMonth) + Integer.toString(monthOfYear) 
//							+ Integer.toString(year);
					
					if (iD == R.id.toDate)
						mToDate = timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
					else
						mFromDate = timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));

					txtDate.setText(mDateFormatter.format(mDateTime.getTime()));
				}
			}, mDateTime.get(Calendar.YEAR),
			mDateTime.get(Calendar.MONTH),
			mDateTime.get(Calendar.DAY_OF_MONTH));

		case R.id.fromTime:
		case R.id.toTime:
			final Button txtTime = (Button) findViewById(iD);
			return new TimePickerDialog(this, new OnTimeSetListener()
			{

				@Override
				public void onTimeSet(TimePicker view, int hourOfDay,
						int minute)
				{
					mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
					mDateTime.set(Calendar.MINUTE, minute);
					
//					String timeFormatted = Integer.toString(hourOfDay) + Integer.toString(minute) + "00";
					
					if (iD == R.id.toTime)
						mToTime = timeDateFormatter(hourOfDay, minute, "00");
					else
						mFromTime = timeDateFormatter(hourOfDay, minute, "00");
					
					txtTime.setText(mTimeFormatter.format(mDateTime.getTime()));
				}
			}, mDateTime.get(Calendar.HOUR_OF_DAY),
			mDateTime.get(Calendar.MINUTE), false);

		}
		return null;
	}

	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.minReminder:
	            if (checked)
	                mReminder = 5;
	            break;
	        case R.id.medReminder:
	            if (checked)
	                mReminder = 10;
	            break;
	        case R.id.maxReminder:
	        	if (checked)
	        		mReminder = 15;
	        	break;
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.add_event, menu);
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onClick(View viewArg) {
		switch(viewArg.getId()){
		case R.id.fromDate:
		case R.id.fromTime:
		case R.id.toDate:
		case R.id.toTime:
			showDialog(viewArg.getId());
			break;
		case R.id.cancelButton:
			super.onBackPressed();
			break;
		case R.id.saveButton:
			EditText titleBox = (EditText)findViewById(R.id.editTitle);
			mTitle = titleBox.getText().toString();
			
			if (mandatoryValuesSpecified()){
				
				Log.v(TAG, "from : " + mFromTime + " to: " + mToTime);
				ContentValues values = new ContentValues();
				values.put(DBSQLiteHelper.COLUMN_TABLE, PERSONAL);
				values.put(DBSQLiteHelper.COLUMN_TITLE, mTitle);
				values.put(DBSQLiteHelper.COLUMN_START_DATE, mFromDate);
				values.put(DBSQLiteHelper.COLUMN_START_TIME, mFromTime);
				if (mToTime != 0){
					values.put(DBSQLiteHelper.COLUMN_END_TIME, mToTime);
					values.put(DBSQLiteHelper.COLUMN_END_DATE, mFromDate);
				}
				if (mReminder != 0)
					values.put(DBSQLiteHelper.COLUMN_REMINDER_TIME, mReminder);
				
				
				
				Log.v(TAG, "from : " + Integer.toString(mFromTime) + 
						" to: " + Integer.toString(mToTime));
				getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);
				
				Toast successToast = Toast.makeText(this, "Event Added!", Toast.LENGTH_LONG);
				successToast.show();
				
				// Go back to the previous activity
				super.onBackPressed();
			}
			break;
		}
	}

	private boolean mandatoryValuesSpecified() {
		if (mTitle == ""){
			Toast toast = Toast.makeText(this, "Title for event must be provided!", Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		if (mToTime != 0){
			if (mFromDate < mToDate && mFromTime <= mToTime ) {
				Toast toast = Toast.makeText(this, "Check Start and End Date Time!", Toast.LENGTH_LONG);
				toast.show();
				return false;
			}
		}
		if (mFromDate == 0 || mFromTime == 0){
			Toast toast = Toast.makeText(this, "Start Date and Time must be provided!", Toast.LENGTH_LONG);
			toast.show();
			return false;
		}
		return true;
	}

	private int timeDateFormatter(int firstVal, int secondVal, String thirdVal) {
		int formattedValue = 0;
		String firstString = null;
		String secondString = null;
		
		if (firstVal < 10)
			firstString = "0" + Integer.toString(firstVal);
		else
			firstString = Integer.toString(firstVal);
		
		if (secondVal < 10)
			secondString = "0" + Integer.toString(secondVal);
		else
			secondString = Integer.toString(secondVal);
	
		formattedValue = Integer.parseInt(firstString + secondString 
				+ thirdVal);
		
		return formattedValue;
	}
}
