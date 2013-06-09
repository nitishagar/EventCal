package cs.softwarearchitecture.eventcal.contentprovider;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.text.TextUtils;

import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

/**
 * @author nitishagarwal
 *
 */
public class DBEventsContentProvider extends ContentProvider {

	// Database
	private DBSQLiteHelper eventDatabase;

	private static final String AUTHORITY = "com.eventsample.eventret.contentprovider";
	private static final String BASE_PATH = "eventret";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
		      + "/" + BASE_PATH);
	
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
		      + "/eventret";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
		      + "/EventCalDB";
		  
	// URI Matcher
	private static final int DB_EVENTS = 10;
	private static final int  DB_EVENT = 20;

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sURIMatcher.addURI(AUTHORITY, BASE_PATH, DB_EVENTS);
		sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", DB_EVENT);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		
	    SQLiteDatabase sqlDB = eventDatabase.getWritableDatabase();
	    
	    int rowsDeleted = 0;
	    
	    switch (uriType) {
	    case DB_EVENTS:
	      rowsDeleted = sqlDB.delete(DBSQLiteHelper.TABLE_NAME, selection,
	          selectionArgs);
	      break;
	    case DB_EVENT:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = sqlDB.delete(DBSQLiteHelper.TABLE_NAME,
	            DBSQLiteHelper.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsDeleted = sqlDB.delete(DBSQLiteHelper.TABLE_NAME,
	            DBSQLiteHelper.COLUMN_ID + "=" + id 
	            + " and " + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri arg0) {
		return null;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		
	    SQLiteDatabase sqlDB = eventDatabase.getWritableDatabase();
	    
	    int rowsDeleted = 0;
	    long id = 0;
	    
	    switch (uriType) {
	    case DB_EVENTS:
	      id = sqlDB.insert(DBSQLiteHelper.TABLE_NAME, null, values);
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return Uri.parse(BASE_PATH + "/" + id);
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		eventDatabase = new DBSQLiteHelper(getContext());
	    return false;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
		      String[] selectionArgs, String sortOrder) {
		// Using SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // Check if the caller has requested a column which does not exists
	    checkColumns(projection);

	    // Set the table
	    queryBuilder.setTables(DBSQLiteHelper.TABLE_NAME);

	    int uriType = sURIMatcher.match(uri);
	    switch (uriType) {
	    case DB_EVENTS:
	      break;
	    case DB_EVENT:
	      // Adding the ID to the original query
	      queryBuilder.appendWhere(DBSQLiteHelper.COLUMN_ID + "="
	          + uri.getLastPathSegment());
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

	    SQLiteDatabase db = eventDatabase.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection,
	        selectionArgs, null, null, sortOrder);
	    // Make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);

	    return cursor;
	}

	private void checkColumns(String[] projection) {
		String[] available = { DBSQLiteHelper.COLUMN_ID, DBSQLiteHelper.COLUMN_TABLE, DBSQLiteHelper.COLUMN_TITLE,
				DBSQLiteHelper.COLUMN_START_TIME, DBSQLiteHelper.COLUMN_END_TIME,
				DBSQLiteHelper.COLUMN_LOCATION };
		if (projection != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			// Check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns in projection");
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
		      String[] selectionArgs) {

	    int uriType = sURIMatcher.match(uri);
	    SQLiteDatabase sqlDB = eventDatabase.getWritableDatabase();
	    
	    int rowsUpdated = 0;
	    
	    switch (uriType) {
	    case DB_EVENTS:
	      rowsUpdated = sqlDB.update(DBSQLiteHelper.TABLE_NAME, 
	          values, 
	          selection,
	          selectionArgs);
	      break;
	    case DB_EVENT:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = sqlDB.update(DBSQLiteHelper.TABLE_NAME, 
	            values,
	            DBSQLiteHelper.COLUMN_ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = sqlDB.update(DBSQLiteHelper.TABLE_NAME, 
	            values,
	            DBSQLiteHelper.COLUMN_ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase db = eventDatabase.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		switch(match){
		case DB_EVENTS:
			int numInserted= 0;
			db.beginTransaction();
			try {
				//SQL insert or replace statement
				if (values.length == 5){
					SQLiteStatement insert = 
							db.compileStatement("insert or replace into " + DBSQLiteHelper.TABLE_NAME
									+ " ( " + DBSQLiteHelper.COLUMN_TABLE + ", " + DBSQLiteHelper.COLUMN_TITLE
									+ ", " + DBSQLiteHelper.COLUMN_START_TIME + ", " + DBSQLiteHelper.COLUMN_END_TIME 
									+ ", " + DBSQLiteHelper.COLUMN_LOCATION + " )"
									+" values " + "(?,?,?,?,?)");

					for (ContentValues value : values){
						//bind the 1-indexed ?'s to the values specified
						insert.bindString(1, value.getAsString(DBSQLiteHelper.COLUMN_TABLE));
						insert.bindString(2, value.getAsString(DBSQLiteHelper.COLUMN_TITLE));
						insert.bindString(3, value.getAsString(DBSQLiteHelper.COLUMN_START_TIME));
						insert.bindString(4, value.getAsString(DBSQLiteHelper.COLUMN_END_TIME));
						insert.bindString(5, value.getAsString(DBSQLiteHelper.COLUMN_LOCATION));
						insert.execute();
					}
				}
				else{
					SQLiteStatement insert = 
							db.compileStatement("insert or replace into " + DBSQLiteHelper.TABLE_NAME
									+ " ( " + DBSQLiteHelper.COLUMN_TABLE + ", " + DBSQLiteHelper.COLUMN_TITLE
									+ ", " + DBSQLiteHelper.COLUMN_START_TIME + ", " 
									+ DBSQLiteHelper.COLUMN_LOCATION + " )"
									+" values " + "(?,?,?,?)");

					for (ContentValues value : values){
						//bind the 1-indexed ?'s to the values specified
						insert.bindString(1, value.getAsString(DBSQLiteHelper.COLUMN_TABLE));
						insert.bindString(2, value.getAsString(DBSQLiteHelper.COLUMN_TITLE));
						insert.bindString(3, value.getAsString(DBSQLiteHelper.COLUMN_START_TIME));
						insert.bindString(4, value.getAsString(DBSQLiteHelper.COLUMN_LOCATION));
						insert.execute();
					}
				}
				db.setTransactionSuccessful();
				numInserted = values.length;
			} finally {
				db.endTransaction();
			}
			return numInserted;
		default:
			throw new UnsupportedOperationException("unsupported uri: " + uri);
		}
	}

}
