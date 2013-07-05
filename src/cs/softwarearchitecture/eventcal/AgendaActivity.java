package cs.softwarearchitecture.eventcal;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.modify.AddEvent;
import cs.softwarearchitecture.eventcal.modify.EditEvent;

public class AgendaActivity extends DefaultView implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener  {

	public static int AGENDA_VIEW = 2;
	
	// Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

//    String[] mFromColumns = { DBSQLiteHelper.COLUMN_TITLE };
//    int[] mToFields = { R.id.event_title };

	AgendaCursorAdapter mAdapter;
	
	ListView mListView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_agenda);
		
		/*
		 * Initializes the CursorLoader. The URL_LOADER value is eventually passed
		 * to onCreateLoader().
		 */
		getLoaderManager().initLoader(URL_LOADER, null, this);

	    // Gets a handle to a List View
		try {
			mListView = (ListView) findViewById(R.id.event_list);
			
			mAdapter = new AgendaCursorAdapter(this, null);
			
			// Sets the adapter for the view
			mListView.setAdapter(mAdapter);
			
			mListView.setOnItemClickListener(this);
		}
		catch(Exception e){
			Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_view, menu);
		actionBarViewSelector();
		getActionBar().setSelectedNavigationItem(AGENDA_VIEW);
		return true;
	}
	
	/**
	 * 
	 */
	private void actionBarViewSelector() {
		// View Selection aka Spinner
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(new ContextThemeWrapper(this, android.R.style.Theme_Holo), 
				R.array.view_list, android.R.layout.simple_spinner_dropdown_item);

		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter
			String[] viewList = getResources().getStringArray(R.array.view_list);
			Intent targetIntent = new Intent();
			
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				switch(position){
				case 0:
					targetIntent.setClass(getApplicationContext(), DefaultView.class);
					startActivity(targetIntent);
					finish();
					break;
				case 1:
					targetIntent.setClass(getApplicationContext(), MonthActivity.class);
					startActivity(targetIntent);
					finish();
					break;
				case 2:
					String className = "cs.softwarearchitecture.eventcal.AgendaActivity$1";
					if(!className.equals(this.getClass().getName())) {
						targetIntent.setClass(getApplicationContext(), AgendaActivity.class);
						startActivity(targetIntent);
						finish();
					}
					break;
				}
				return true;
			}
		};

		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
		
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		/*
	     * Takes action based on the ID of the Loader that's being created
	     */
	    switch (loaderID) {
	        case URL_LOADER:
	            // Returns a new CursorLoader
	        	String[] projection = { DBSQLiteHelper.COLUMN_ID, DBSQLiteHelper.COLUMN_TITLE, 
	        								DBSQLiteHelper.COLUMN_TABLE, DBSQLiteHelper.COLUMN_START_DATE, 
	        								DBSQLiteHelper.COLUMN_REV_START_DATE };
	        	String[] argValue = { "0" };
	        	
	            return new CursorLoader(
	                        this,						    		     // Parent activity context
	                        DBEventsContentProvider.CONTENT_URI,        // Table to query
	                        projection,                                // Projection to return
	                        DBSQLiteHelper.COLUMN_START_TIME + "!=?",            // Selection clause
	                        argValue,            							    // Selection arguments
	                        DBSQLiteHelper.COLUMN_REV_START_DATE + " ASC, " 
	                        + DBSQLiteHelper.COLUMN_START_TIME + " ASC"			  // Sort order
	        );
	        default:
	            // An invalid id was passed in
	        	Log.d(DefaultView.TAG, "Invalid ID!");
	            return null;
	    }
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		/*
	     * Moves the query results into the adapter, causing the
	     * ListView fronting this adapter to re-display
	     */
		mAdapter.changeCursor(cursor);

		// Set ListView position
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
		Date date = new Date();
		String currentDate = "1" + dateFormat.format(date);
		
		setDate(cursor, currentDate);
	}

	/**
	 * @param cursor
	 */
	protected void setDate(Cursor cursor, String date) {
		
	    int desiredPosition = 0;

		Log.d(DefaultView.TAG, "Current DATE: " + 
				date);
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				Log.d(DefaultView.TAG, "Date current found: " + 
						cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE)));
				String reverseDate = date.substring(5, date.length()) 
						+ date.substring(3, 5) + date.substring(0, 3);
				if(Integer.parseInt(reverseDate) >= cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_REV_START_DATE))){
					Log.d(DefaultView.TAG, "Position updated to: " + Integer.toString(desiredPosition));
					desiredPosition += 1;
				}
			}
		}
		
		Log.d(DefaultView.TAG, "Desired position: " + Integer.toString(desiredPosition));
		
		final int dPosition = desiredPosition;
		
		mListView.post(new Runnable() 
	    {
	        @Override
	        public void run() 
	        {
	            mListView.setSelection(dPosition);
	            View v = mListView.getChildAt(dPosition);
	            if (v != null) 
	            {
	                v.requestFocus();
	            }
	        }
	    });
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		 /*
	     * Clears out the adapter's reference to the Cursor.
	     * This prevents memory leaks.
	     */
	    mAdapter.changeCursor(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		TextView selectedEvent = (TextView) view.findViewById(R.id.event_title);
		String selectedEventTitle = selectedEvent.getText().toString();
		
		// Get all the details about that event
		String selection = DBSQLiteHelper.COLUMN_TITLE + " LIKE ? ";
		String[] selectionArgs = new String[] { selectedEventTitle };
		
		Cursor cursor = 
				getContentResolver().query(
						DBEventsContentProvider.CONTENT_URI, null, 
						selection, selectionArgs, DBSQLiteHelper.COLUMN_START_DATE + " ASC");
		
		// Intent for showing event details
		Intent editEventIntent = 
				new Intent(this, EditEvent.class);

		if (cursor.moveToFirst()) {
			Log.d(DefaultView.TAG, "loading selected events ");
			while(!cursor.isAfterLast()){
				int _id = cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_ID));
				String title = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
				String start_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_TIME));
				String end_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_TIME));
				String date = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));
				String group = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
				int reminder = cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_REMINDER_TIME));

				editEventIntent.putExtra("title", title);
				editEventIntent.putExtra("start_time", start_time);
				editEventIntent.putExtra("end_time", end_time);
				editEventIntent.putExtra("date", date);
				editEventIntent.putExtra("reminder", reminder);
				editEventIntent.putExtra("group", group);
				editEventIntent.putExtra("id", _id);

				cursor.moveToNext();
			}
			
			// Start detail activity aka edit event
			editEventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(editEventIntent);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.action_goto:
			showDialog(R.id.action_goto);
			break;
		case R.id.action_settings:
			Intent settingIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingIntent);
			break;
		case R.id.menu_add:
			Intent addEventIntent = new Intent(this, AddEvent.class);
			addEventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(addEventIntent);
			break;
		case R.id.today:
			SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");
			Date date = new Date();
			String currentDate = "1" + dateFormat.format(date);
			
			try {
				setDate(mAdapter.getCursor(), currentDate);
			}
			catch (Exception e){
				Log.e(DefaultView.TAG, "Cursor empty: " + e.getMessage());
			}
			break;
		}
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(final int iD) {
		
		switch (iD)
		{
		case R.id.action_goto:
			final Calendar calChanging = Calendar.getInstance(Locale.getDefault());
			return new DatePickerDialog(this, new OnDateSetListener()
			{
				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth)
				{
					calChanging.set(year, monthOfYear + 1, dayOfMonth);
					String reqDate = Integer.toString(
							CurrentDateTimeConverter.timeDateFormatter(calChanging.get(Calendar.DAY_OF_MONTH), 
									calChanging.get(Calendar.MONTH), Integer.toString(calChanging.get(Calendar.YEAR))));
					
					try {
						setDate(mAdapter.getCursor(), reqDate);
					}
					catch (Exception e){
						Log.e(DefaultView.TAG, "Cursor empty: " + e.getMessage());
					}
				}
			}, calChanging.get(Calendar.YEAR),
			calChanging.get(Calendar.MONTH),
			calChanging.get(Calendar.DAY_OF_MONTH));	
		}
		return null;
	}

}
