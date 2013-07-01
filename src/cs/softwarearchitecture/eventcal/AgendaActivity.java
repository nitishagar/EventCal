package cs.softwarearchitecture.eventcal;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

public class AgendaActivity extends DefaultView implements LoaderManager.LoaderCallbacks<Cursor>  {

	public static int AGENDA_VIEW = 2;
	
	// Identifies a particular Loader being used in this component
    private static final int URL_LOADER = 0;

    String[] mFromColumns = { DBSQLiteHelper.COLUMN_TITLE };
    int[] mToFields = { R.id.event_title };

	AgendaCursorAdapter mAdapter;
	
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
			ListView mListView = (ListView) findViewById(R.id.event_list);
			
			mAdapter = new AgendaCursorAdapter(this, null);
//					new SimpleCursorAdapter(
//							this,                // Current context
//							R.layout.agenda_item,  // Layout for a single row
//							null,                // No Cursor yet
//							mFromColumns,        // Cursor columns to use
//							mToFields,           // Layout fields to use
//							0                    // No flags
//							);
			
			// Sets the adapter for the view
			mListView.setAdapter(mAdapter);
			
//			mListView.setOnItemClickListener(eventDetail);
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
	        	String[] projection = { DBSQLiteHelper.COLUMN_ID, DBSQLiteHelper.COLUMN_TITLE, DBSQLiteHelper.COLUMN_TABLE };
	        	
	            return new CursorLoader(
	                        this,						    		     // Parent activity context
	                        DBEventsContentProvider.CONTENT_URI,        // Table to query
	                        projection,                                // Projection to return
	                        null,            						  // No selection clause
	                        null,            									  // No selection arguments
	                        DBSQLiteHelper.COLUMN_START_DATE + " ASC, " 
	                        + DBSQLiteHelper.COLUMN_START_TIME + " ASC"			  // Default sort order
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
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		 /*
	     * Clears out the adapter's reference to the Cursor.
	     * This prevents memory leaks.
	     */
	    mAdapter.changeCursor(null);
	}

}
