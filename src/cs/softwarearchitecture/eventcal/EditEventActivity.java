package cs.softwarearchitecture.eventcal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.ParseException;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
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

public class EditEventActivity  extends Activity implements OnClickListener {
	
	
	// Event id
	int _id;
	String title;
	String start_time;
	String end_time;
	String start_date;
	String end_date;
	String location;
	String reminder;
	String group;
	
	
	// Event Group
	public static final String PERSONAL = "PERSONAL";
	
	Calendar mDateTime = Calendar.getInstance();

	SimpleDateFormat mDateFormatter = new SimpleDateFormat("MMMM dd yyyy");
	SimpleDateFormat mTimeFormatter = new SimpleDateFormat("hh:mm a");
	
	// Content Resolver
	private static ContentResolver mEventContentResolver;

	// Reminder value
	private String mTitle;
	private int mFromDate = 0;
	private int mFromTime = 0;
	private int mToDate = 0;
	private int mToTime = 0;
	private int mReminder = 0;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_event);
		
		Bundle b = getIntent().getExtras();
		_id = b.getInt("event_id");
		
		mEventContentResolver = getContentResolver();
		
		
		Cursor cursor = 
				mEventContentResolver.query(
						DBEventsContentProvider.CONTENT_URI, null, 
						"ID =?", null , Integer.toString(_id), null);
		
		if (cursor.moveToFirst()) {
			title = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
			start_time = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_TIME));
			end_time = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_TIME));
			start_date = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));
			end_date = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_DATE));
			location = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_LOCATION));
			reminder = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_REMINDER_TIME));
			group = 
					cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
		}

		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		EditText titleBox = (EditText)findViewById(R.id.editTitle);
		titleBox.setText(title);
		
		// Setting the text of the Buttons to current date and time
		Button txtTime = (Button) findViewById(R.id.fromTime);
		Button txtDate = (Button) findViewById(R.id.fromDate);

		SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		
		Date fromTime = null;
		Date fromDate = null;
		Date toTime = null;
		Date toDate = null;
		
		try {
			fromTime = timeFormat.parse(start_time);
			fromDate = dateFormat.parse(start_date);
			toTime = timeFormat.parse(end_time);
			toDate = dateFormat.parse(end_date);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		txtTime.setText(mTimeFormatter.format(fromTime.getTime()));
		txtDate.setText(mDateFormatter.format(fromDate.getTime()));   
		
		txtTime = (Button) findViewById(R.id.toTime);
		txtDate = (Button) findViewById(R.id.toDate);
		
		txtTime.setText(mTimeFormatter.format(toTime.getTime()));
		txtDate.setText(mDateFormatter.format(toDate.getTime()));   
		
		// Setting initial value of variables
		mFromDate = Integer.parseInt(start_date);
		mFromTime = Integer.parseInt(start_time);

		
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
					
					String dateFormatted = Integer.toString(dayOfMonth) + Integer.toString(monthOfYear) 
							+ Integer.toString(year);
					
					if (iD == R.id.toDate)
						mToDate = Integer.parseInt(dateFormatted);
					else
						mFromDate = Integer.parseInt(dateFormatted);

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
					
					String timeFormatted = Integer.toString(hourOfDay) + Integer.toString(minute) + "00";
					
					if (iD == R.id.toTime)
						mToTime = Integer.parseInt(timeFormatted);
					else
						mFromTime = Integer.parseInt(timeFormatted);
					
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
				String where = "ID=?";
				String[] args = new String[] {Integer.toString(_id)};
				mEventContentResolver.delete(DBEventsContentProvider.CONTENT_URI, where, args);
				
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
				
				mEventContentResolver.insert(DBEventsContentProvider.CONTENT_URI, values);
				
				Toast successToast = Toast.makeText(this, "Event Editted!", Toast.LENGTH_LONG);
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

}