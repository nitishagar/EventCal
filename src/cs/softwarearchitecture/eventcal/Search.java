package cs.softwarearchitecture.eventcal;

import java.util.ArrayList;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.modify.EditEvent;

public class Search extends ListActivity { 
	
	private ArrayList<String> mSearchResults = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		handleIntent(getIntent()); 
		setTitle("Results");
	} 

	@Override
	public void onNewIntent(Intent intent) { 
		setIntent(intent); 
		handleIntent(intent); 
	} 

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) { 
		String selectedTitle = mSearchResults.get(position);
		
		// Get all the details about that event
		String selection = DBSQLiteHelper.COLUMN_TITLE + " LIKE ?";
		String[] selectionArgs = new String[] { selectedTitle };
		
//		Log.d(DefaultView.TAG, queryStr + columnsReturn[0] + selectionArgs[0]);
		Cursor cursor = 
				getContentResolver().query(
						DBEventsContentProvider.CONTENT_URI, null, 
						selection, selectionArgs, DBSQLiteHelper.COLUMN_START_DATE + " ASC");
		
		// Intent for showing event details
		Intent editEventIntent = 
				new Intent(this, EditEvent.class);
		
		if (cursor.moveToFirst()) {
			Log.d(DefaultView.TAG, "loading selected events");
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
			Log.d(DefaultView.TAG, mSearchResults.toString());
		}
		
		// Start detail activity aka edit event
		editEventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(editEventIntent);
		
	} 

	private void handleIntent(Intent intent) { 
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			String query = 
					intent.getStringExtra(SearchManager.QUERY); 
			doSearch(query); 
			displayResultList();
		} 
	}    

	/**
	 * Search string taken as input
	 * @param queryStr
	 */
	private void doSearch(String queryStr) {
		mSearchResults = new ArrayList<String>();
		String[] columnsReturn = new String[] { DBSQLiteHelper.COLUMN_TITLE };
		String selection = DBSQLiteHelper.COLUMN_TITLE + " LIKE ?";
		String[] selectionArgs = new String[] { queryStr+"%" };
		
//		Log.d(DefaultView.TAG, queryStr + columnsReturn[0] + selectionArgs[0]);
		Cursor cursor = 
				getContentResolver().query(
						DBEventsContentProvider.CONTENT_URI, columnsReturn, 
						selection, selectionArgs, DBSQLiteHelper.COLUMN_START_DATE + " ASC");
		if (cursor.moveToFirst()) {
			Log.d(DefaultView.TAG, "loading events");
			while(!cursor.isAfterLast()){
				String title = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
				mSearchResults.add(title);
				cursor.moveToNext();
			}
			Log.d(DefaultView.TAG, mSearchResults.toString());
		}
	}

	/**
	 * Result display on a Simple List
	 */
	private void displayResultList() {
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mSearchResults));
		getListView().setTextFilterEnabled(true);
	}
}