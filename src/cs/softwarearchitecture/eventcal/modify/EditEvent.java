package cs.softwarearchitecture.eventcal.modify;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ShareActionProvider;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;

import cs.softwarearchitecture.eventcal.DefaultView;
import cs.softwarearchitecture.eventcal.R;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.utility.ColumnNames;
import cs.softwarearchitecture.eventcal.utility.CurrentDateTimeConverter;
import cs.softwarearchitecture.eventcal.utility.Geohasher;

@SuppressLint("SimpleDateFormat")
public class EditEvent extends FragmentActivity implements OnClickListener, OnMyLocationChangeListener {

	// Constants
	private static final int ZOOM_LEVEL_ON_MAP = 9;
	private static final int MAX_RESULTS = 15;

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
	SimpleDateFormat mTimeFormatter = new SimpleDateFormat("hh:mm aa");

	// Share event provider
	private ShareActionProvider mShareActionProvider;

	public static String TAG = "Edit Event";
	
	Calendar mDateTime = Calendar.getInstance();
	
	// Map for location 
	private GoogleMap mMap;

	// Location GeoCoding
	Geocoder mGeocoder;
	
	private static final Geohasher mGeoHasher = new Geohasher();

	private String mLocation;

	private boolean mLocationAvailable = false;
	private String mLocationString;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  

		setContentView(R.layout.activity_modify_event);

		// Back button functionality enabled
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// Maps setup
		setUpMapIfNeeded();
		
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
		titleBox.setText(mTitle);

		// Setting text of the location
		mGeocoder = new Geocoder(this, Locale.ENGLISH);
		mLocation = extras.getString("location"); 
		Log.d("LOCATION_EDIT", "Title: " + title);
		Log.d("LOCATION_EDIT", "Getting Location Strings: " + extras.getString("location"));
		if (mLocation != null) {
			if (mGroup.equals("PERSONAL")) {
				Log.d("LOCATION_EDIT", "Getting Location Strings");
				mLocationString = getLocationString(mLocation);
				EditText locationBox = (EditText)findViewById(R.id.locationString);
				locationBox.setText(mLocationString);
				mLocationAvailable = true;
			}
			else {
				mLocationString = mLocation;
				EditText locationBox = (EditText)findViewById(R.id.locationString);
				locationBox.setText(mLocationString);
				
				List<Address> storedAddr = new ArrayList<Address>();
				try {
					storedAddr = mGeocoder.getFromLocationName(mLocationString, 1);
					if (!(storedAddr.isEmpty())) {
						Address stAddr = storedAddr.get(0);
						LatLng latLng = new LatLng(stAddr.getLatitude(), 
								stAddr.getLongitude());

						// Set the location Value
						mLocation = mGeoHasher.encode(latLng);
						mLocationAvailable = true;
					}
				} catch (IOException e) {
					Log.e(DefaultView.TAG, "Exception Caught: " + e.getMessage());
				}
			}
			if (mLocationAvailable) {
				// Show on Map
				showLocation();
			}
		}
		
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

		// Location Search 
		Button search = (Button) findViewById(R.id.searchButton);
		search.setOnClickListener(this);
		
	}

	private String getLocationString(String location) {
		LatLng latLng = mGeoHasher.decode(location);
		List<Address> storedAddr = new ArrayList<Address>();
		try {
			storedAddr = mGeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
			Address stAddr = storedAddr.get(0);
			return addressToStringConversion(stAddr);
		} catch (Exception e) {
			Log.e("LOCATION_EDIT", "Exception caught: " + e.getMessage());
		}
		return null;
	}

	private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
	
	private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
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
		mDateTime.set(year, monthOfYear - 1, dayOfMonth);
		mFromDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
		mToDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear, Integer.toString(year));
		txtDate.setText(mDateFormatter.format(mDateTime.getTime()));

		// setting the time
		switch(txtTime.getId()){
		case R.id.fromTime:
			hourOfDay = Integer.parseInt(start_time.substring(1, 3));
			minute = Integer.parseInt(start_time.substring(3, 5));
			
			mFromTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");
			Log.d("TIME VALUE", "Time: " + Integer.toString(hourOfDay) + ":" + Integer.toString(minute) + "--" + start_time);
			break;
		case R.id.toTime:
			if (end_time != null){
				hourOfDay = Integer.parseInt(end_time.substring(1, 3));
				minute = Integer.parseInt(end_time.substring(3, 5));
			
				mToTime = CurrentDateTimeConverter.timeDateFormatter(hourOfDay, minute, "00");
				Log.d("TIME VALUE", "Time: " + Integer.toString(hourOfDay) + ":" + Integer.toString(minute) + "--" + end_time);
			}
			else{
				mToTime = mFromTime;
			}
			break;
		}

		mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
		mDateTime.set(Calendar.MINUTE, minute);
		mDateTime.set(Calendar.SECOND, 0);
		txtTime.setText(mTimeFormatter.format(mDateTime.getTime()));
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
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
		
		String shareEventDetails;
		
		if(extras.getString("end_time") != null) {
			String end_time = timeFormatted(extras.getString("end_time"));

			shareEventDetails = title + " @ " + start_time + " to " + end_time;
		}
		else {
			shareEventDetails = title + " @ " + start_time;
		}

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");

		shareIntent.putExtra(Intent.EXTRA_TEXT, shareEventDetails);

		return shareIntent;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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


	/**
	 * Formatted Time String for Share 
	 * @param time
	 * @return time (String)
	 */
	private String timeFormatted(String time) {
		Log.d(DefaultView.TAG, "Unformatted String: " + time);

		try{
			time = time.substring(0, 2) + ":" + time.substring(2, 4);
		}
		catch (NullPointerException e){
			Log.e(DefaultView.TAG, "Exception caught: (NullPointer) " + e.getMessage());
		}
		return time;
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
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
						mToDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear + 1, Integer.toString(year));
					else
						mFromDate = CurrentDateTimeConverter.timeDateFormatter(dayOfMonth, monthOfYear + 1, Integer.toString(year));

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

	/**
	 * @param view
	 */
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
				values.put(ColumnNames.COLUMN_TABLE, mGroup);
				values.put(ColumnNames.COLUMN_TITLE, mTitle);
				values.put(ColumnNames.COLUMN_START_DATE, mFromDate);
				values.put(ColumnNames.COLUMN_START_TIME, mFromTime);
				if (mToTime != 0){
					values.put(ColumnNames.COLUMN_END_TIME, mToTime);
					values.put(ColumnNames.COLUMN_END_DATE, mFromDate);
				}
				if (mReminder != 0)
					values.put(ColumnNames.COLUMN_REMINDER_TIME, mReminder);

				if (mLocationAvailable)
					values.put(ColumnNames.COLUMN_LOCATION, mLocation);
				
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
		case R.id.searchButton:
			EditText locationString = (EditText) findViewById(R.id.locationString);
			
			String searchString = locationString.getText().toString();
			try {
				List<Address> searchResultsAddress = new ArrayList<Address>();
				
				searchResultsAddress = mGeocoder.getFromLocationName(searchString, MAX_RESULTS);
				
				Log.d("LOCATION", "Search String: " + searchString);
				
				// Converting the location to Strings (human readable addresses)
				List<String> searchResults = new ArrayList<String>();
				for (Address searchResult : searchResultsAddress) {
					searchResults.add(addressToStringConversion(searchResult));
				}
				
				// Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				
				final String[] resultsString = searchResults.toArray(new String[0]);
				builder.setTitle(R.string.location_results_)
					   .setItems(resultsString, new DialogInterface.OnClickListener() {
						   public void onClick(DialogInterface dialog, int arrayIndex) {
							   InputMethodManager inputManager = (InputMethodManager)
									   getSystemService(Context.INPUT_METHOD_SERVICE); 

							   inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
									   InputMethodManager.HIDE_NOT_ALWAYS);
							   try {
								   List<Address> addr = new ArrayList<Address>();
								   addr = mGeocoder.getFromLocationName(resultsString[arrayIndex], 1);
								   Address addressSelected = addr.get(0);
								   LatLng latLng = new LatLng(addressSelected.getLatitude(), 
										   						addressSelected.getLongitude());
								   
								   // Set the location Value
								   mLocation = mGeoHasher.encode(latLng);
								   mLocationString = resultsString[arrayIndex];
								   mLocationAvailable = true;
								   
								   showLocation();
								   
							   } catch (Exception e) {
								   Log.e("LOCATION", "Exception caught: " + e.getMessage());
							   }
						   }

					   });
				
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				
				// Show the Dialog
				dialog.show();
				
			} catch (Exception e) {
				Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
			}
			break;
		}
	}

	/**
	 * 
	 */
	private void showLocation() {
		Log.d("LOCATION_EDIT", "Location: " + mLocation);
		LatLng latLng = mGeoHasher.decode(mLocation);
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL_ON_MAP);
		mMap.animateCamera(update);
		mMap.addMarker(new MarkerOptions().position(latLng).title(mLocationString));
	}
	
	private String addressToStringConversion(Address address) {
		int index = 0;
		String addressVal = "";
		while (address.getAddressLine(index) != null) {
			addressVal += address.getAddressLine(index);
			if (index <= address.getMaxAddressLineIndex() - 1) {
				 addressVal += ", ";
			}
			else {
				addressVal += " ";
			}
			Log.d("LOCATION", "Location search result: " + addressVal);
			index ++;
		}
		return addressVal;
	}
	/**
	 * Check the Event validity
	 * @return event validity
	 */
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
	
	@Override
	public void onMyLocationChange(Location location) {
		mLocation = mGeoHasher.encode(location);
		mLocationAvailable  = true;
	}

}
