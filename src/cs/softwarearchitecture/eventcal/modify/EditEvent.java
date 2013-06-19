package cs.softwarearchitecture.eventcal.modify;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.RadioGroup;
import android.widget.ShareActionProvider;
import android.widget.TimePicker;
import android.widget.Toast;
import cs.softwarearchitecture.eventcal.DefaultView;
import cs.softwarearchitecture.eventcal.R;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.CurrentDateTimeConverter;

@SuppressLint("SimpleDateFormat")
public class EditEvent extends Activity implements OnClickListener {

	// Reminder value
	private String mTitle;
	private String mGroup;
	private int mFromDate = 0;
	private int mFromTime = 0;
	private int mToDate = 0;
	private int mToTime = 0;
	private int mReminder = 0;
	private String[] mIdArgs = new String[1];

	SimpleDateFormat mDateFormatter = new SimpleDateFormat("MMMM dd yyyy");
	SimpleDateFormat mTimeFormatter = new SimpleDateFormat("hh:mm a");

	// Share event provider
	private ShareActionProvider mShareActionProvider;

	public static String TAG = "Edit Event";
	
	Calendar mDateTime = Calendar.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  

		setContentView(R.layout.activity_add_event);

		// Back button functionality enabled
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		
		// Delete the previous entry
		mIdArgs[0] = Integer.toString(extras.getInt("id"));

		mGroup = extras.getString("group");
		String title = extras.getString("title");
		mTitle = title;

		// Setting text of the title
		EditText titleBox = (EditText)findViewById(R.id.editTitle);
		titleBox.setText(title);

		// Setting the text of the Buttons 
		Button txtDate = (Button) findViewById(R.id.fromDate);
		Button txtTime = (Button) findViewById(R.id.fromTime);

		setText(txtDate, txtTime);

		txtDate = (Button) findViewById(R.id.toDate);
		txtTime = (Button) findViewById(R.id.toTime);

		setText(txtDate, txtTime);

		// Set reminder option
		int reminder = extras.getInt("reminder");
		RadioGroup rdGroup = (RadioGroup) findViewById(R.id.reminderRadio);

		mReminder = reminder;
		
		switch(reminder){
		case 5:
			rdGroup.check(R.id.minReminder);
			break;
		case 10:
			rdGroup.check(R.id.medReminder);
			break;
		case 15:
			rdGroup.check(R.id.maxReminder);
			break;
		}

		Button save = (Button) findViewById(R.id.saveButton);
		save.setText("Update");

		// Update functionality
		((Button) findViewById(R.id.fromDate)).setOnClickListener(this);
		((Button) findViewById(R.id.fromTime)).setOnClickListener(this);
		((Button) findViewById(R.id.toDate)).setOnClickListener(this);
		((Button) findViewById(R.id.toTime)).setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.cancelButton);

		save.setOnClickListener(this);
		cancel.setOnClickListener(this);

	}

	/**
	 * @param txtDate
	 * @param txtTime
	 */
	private void setText(Button txtDate, Button txtTime) {

		Bundle extras = getIntent().getExtras();
		String start_time = extras.getString("start_time");
		String end_time = extras.getString("end_time");
		String date = extras.getString("date");

		int hourOfDay = 0;
		int minute = 0;
		int year = 0;
		int monthOfYear = 0;
		int dayOfMonth = 0;

		// Setting the date
		year = Integer.parseInt(date.substring(5,date.length()));
		monthOfYear = Integer.parseInt(date.substring(3,5));
		dayOfMonth = Integer.parseInt(date.substring(1,3));
		mDateTime.set(year, monthOfYear, dayOfMonth);
		mFromDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
		mToDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
		txtDate.setText(mDateFormatter.format(mDateTime.getTime()));

		// setting the time
		switch(txtTime.getId()){
		case R.id.fromTime:
			hourOfDay = Integer.parseInt(start_time.substring(0, 2));
			minute = Integer.parseInt(start_time.substring(2, 4));
			
			mFromTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");
			break;
		case R.id.toTime:
			hourOfDay = Integer.parseInt(end_time.substring(0, 2));
			minute = Integer.parseInt(end_time.substring(2, 4));
			
			mToTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");
			break;
		}

		mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mDateTime.set(Calendar.MINUTE, minute);
		txtTime.setText(mTimeFormatter.format(mDateTime.getTime()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_event, menu);

		// Locate MenuItem with ShareActionProvider
		MenuItem item = menu.findItem(R.id.menu_item_share);

		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();

		// Get the default share intent
		mShareActionProvider.setShareIntent(createShareIntent());

		return true;
	}

	private Intent createShareIntent() {

		Bundle extras = getIntent().getExtras();
		String title = extras.getString("title");
		String start_time = timeFormatted(extras.getString("start_time"));
		String end_time = timeFormatted(extras.getString("end_time"));

		String shareEventDetails = title + " @ " + start_time + " to " + end_time;

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");

		shareIntent.putExtra(Intent.EXTRA_TEXT, shareEventDetails);

		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.delete_event:
			// Deleting the current event
			getContentResolver().delete(DBEventsContentProvider.CONTENT_URI, "_ID =?", mIdArgs);
			
			Toast.makeText(this, "Event Deleted!", Toast.LENGTH_LONG).show();
		case android.R.id.home:
			super.onBackPressed();
			break;
		}
		return true;
	}


	private String timeFormatted(String time) {
		Log.d(DefaultView.TAG, "Unformatted String: " + time);

		time = time.substring(0, 2) + ":" + time.substring(2, 4);
		return time;
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
						mToDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
					else
						mFromDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));

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
						mToTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");
					else
						mFromTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");

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

				Log.d(TAG, "from : " + mFromTime + " to: " + mToTime);
				Log.d(TAG, "from date: " + mFromDate);
				ContentValues values = new ContentValues();
				values.put(DBSQLiteHelper.COLUMN_TABLE, mGroup);
				values.put(DBSQLiteHelper.COLUMN_TITLE, mTitle);
				values.put(DBSQLiteHelper.COLUMN_START_DATE, mFromDate);
				values.put(DBSQLiteHelper.COLUMN_START_TIME, mFromTime);
				if (mToTime != 0){
					values.put(DBSQLiteHelper.COLUMN_END_TIME, mToTime);
					values.put(DBSQLiteHelper.COLUMN_END_DATE, mFromDate);
				}
				if (mReminder != 0)
					values.put(DBSQLiteHelper.COLUMN_REMINDER_TIME, mReminder);

				Log.d(TAG, "from : " + Integer.toString(mFromTime) + 
						" to: " + Integer.toString(mToTime));

				// Delete the original entry
				getContentResolver().delete(DBEventsContentProvider.CONTENT_URI, "_ID =?", mIdArgs);
				
				// Insert the new entry
				getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);

				Toast successToast = Toast.makeText(this, "Event Updated!", Toast.LENGTH_LONG);
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
