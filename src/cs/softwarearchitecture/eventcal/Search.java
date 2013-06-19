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

public class Search extends ListActivity { 
	
	private ArrayList<String> mSearchResults = new ArrayList<String>();
	
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		handleIntent(getIntent()); 
		setTitle("Results");
	} 

	public void onNewIntent(Intent intent) { 
		setIntent(intent); 
		handleIntent(intent); 
	} 

	public void onListItemClick(ListView l, 
			View v, int position, long id) { 
		// call detail activity for clicked entry 
	} 

	private void handleIntent(Intent intent) { 
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			String query = 
					intent.getStringExtra(SearchManager.QUERY); 
			doSearch(query); 
			displayResultList();
		} 
	}    

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

	private void displayResultList() {
		setListAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mSearchResults));
		getListView().setTextFilterEnabled(true);
	}
}