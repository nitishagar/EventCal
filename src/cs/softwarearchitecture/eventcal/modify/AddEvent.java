package cs.softwarearchitecture.eventcal.modify;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import cs.softwarearchitecture.eventcal.DefaultView;
import cs.softwarearchitecture.eventcal.R;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.utility.ColumnNames;
import cs.softwarearchitecture.eventcal.utility.CurrentDateTimeConverter;
import cs.softwarearchitecture.eventcal.utility.Geohasher;

public class AddEvent extends FragmentActivity implements OnClickListener, OnMyLocationChangeListener {
	
	// Constants
	private static final int ZOOM_LEVEL_ON_MAP = 9;
	private static final int MAX_RESULTS = 15;

	// Event Group
	public static final String PERSONAL = "PERSONAL";
	
	Calendar mDateTime = Calendar.getInstance();

	SimpleDateFormat mDateFormatter = new SimpleDateFormat("MMMM dd yyyy");
	SimpleDateFormat mTimeFormatter = new SimpleDateFormat("hh:mm a");
	
	// Reminder value
	private String mTitle;
	private int mFromDate = 0;
	private int mFromTime = 0;
	private int mToDate = 0;
	private int mToTime = 0;
	private int mReminder = 0;
	
	// Location GeoCoding
	Geocoder mGeocoder;
	
	public static String TAG = "Add Event";
	
	// Map for location 
	private GoogleMap mMap;

	private static final Geohasher mGeoHasher = new Geohasher();

	private String mLocation;

	private boolean mLocationAvailable = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_event);
		
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// Setting the text of the Buttons to current date and time
		Button txtDate = (Button) findViewById(R.id.fromDate);
		Button txtTime = (Button) findViewById(R.id.fromTime);

		txtDate.setText(mDateFormatter.format(DefaultView.mCalendarChanging.getTime()));   
		txtTime.setText(mTimeFormatter.format(DefaultView.mCalendarChanging.getTime()));
		
		txtDate = (Button) findViewById(R.id.toDate);
		txtTime = (Button) findViewById(R.id.toTime);
		
		// Setting initial value of variables
		mFromDate = CurrentDateTimeConverter.timeDateFormatter(
				DefaultView.mCalendarChanging.get(Calendar.DATE),
				DefaultView.mCalendarChanging.get(Calendar.MONTH) + 1, 
				Integer.toString(DefaultView.mCalendarChanging.get(Calendar.YEAR)));
		
		mFromTime = CurrentDateTimeConverter.timeDateFormatter(
				DefaultView.mCalendarChanging.get(Calendar.HOUR_OF_DAY), 
				DefaultView.mCalendarChanging.get(Calendar.MINUTE), 
				"00");
		
		Log.v(TAG, "From Date: " + Integer.toString(mFromDate));
		Log.v(TAG, "From Time: " + Integer.toString(mFromTime));
		
		txtDate.setText(mDateFormatter.format(DefaultView.mCalendarChanging.getTime()));   
		txtTime.setText(mTimeFormatter.format(DefaultView.mCalendarChanging.getTime()));
		
		((Button) findViewById(R.id.fromDate)).setOnClickListener(this);
		((Button) findViewById(R.id.fromTime)).setOnClickListener(this);
		((Button) findViewById(R.id.toDate)).setOnClickListener(this);
		((Button) findViewById(R.id.toTime)).setOnClickListener(this);
		
		Button save = (Button) findViewById(R.id.saveButton);
		Button cancel = (Button) findViewById(R.id.cancelButton);
		
		save.setOnClickListener(this);
		cancel.setOnClickListener(this);
		
		// Maps setup
		setUpMapIfNeeded();
		
		// Location Search 
		mGeocoder = new Geocoder(this, Locale.ENGLISH);
		
		Button search = (Button) findViewById(R.id.searchButton);
		search.setOnClickListener(this);
		
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case android.R.id.home:
			super.onBackPressed();
			break;
		}
		return true;
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
	 * Radio Button selection for time
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
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		// getMenuInflater().inflate(R.menu.add_event, menu);
		return true;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
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
			EditText titleBox = (EditText) findViewById(R.id.editTitle);
			mTitle = titleBox.getText().toString();
			
			if (mandatoryValuesSpecified()){
				
				Log.v(TAG, "from : " + mFromTime + " to: " + mToTime);
				Log.v(TAG, "from date: " + mFromDate);
				ContentValues values = new ContentValues();
				values.put(ColumnNames.COLUMN_TABLE, PERSONAL);
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
				
				getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);
				
				Toast successToast = Toast.makeText(this, "Event Added!", Toast.LENGTH_LONG);
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
				int index = 0;
				for (Address searchResult : searchResultsAddress) {
					String addressVal = "";
					while (searchResult.getAddressLine(index) != null) {
						addressVal += searchResult.getAddressLine(index);
						if (index <= searchResult.getMaxAddressLineIndex() - 1) {
							 addressVal += ", ";
						}
						else {
							addressVal += " ";
						}
						Log.d("LOCATION", "Location search result: " + addressVal);
						index ++;
					}
					
					searchResults.add(addressVal);
					index = 0;
				}

				// Clear any previous markers
				mMap.clear();
				
				// Instantiate an AlertDialog.Builder with its constructor
				popupListViewForSearchResults(searchResults);
				
			} catch (Exception e) {
				Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
			}
			break;
		}
	}

	/**
	 * @param searchResults
	 */
	private void popupListViewForSearchResults(List<String> searchResults) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		final String[] resultsString = searchResults.toArray(new String[0]);
		builder.setTitle(R.string.location_results_)
			   .setItems(resultsString, new DialogInterface.OnClickListener() {
				   @Override
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
						   mLocationAvailable = true;
						   
						   CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 
								   												ZOOM_LEVEL_ON_MAP);
						   mMap.animateCamera(update);
						   mMap.addMarker(new MarkerOptions().position(latLng).title(resultsString[arrayIndex]));
						   
					   } catch (Exception e) {
						   Log.e("LOCATION", "Exception caught: " + e.getMessage());
					   }
				   }
			   });
		
		// Create the AlertDialog
		AlertDialog dialog = builder.create();
		
		// Show the Dialog
		dialog.show();
	}

	/**
	 * Check the Event validity
	 * @return event validity
	 */
	protected boolean mandatoryValuesSpecified() {
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
