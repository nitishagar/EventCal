/**
 * 
 */
package cs.softwarearchitecture.eventcal.services;

import java.text.Format;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Reminders;
import android.text.format.DateFormat;
import android.util.Log;
import cs.softwarearchitecture.eventcal.CurrentDateTimeConverter;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

/**
 * @author nitishagarwal
 *
 */
public class GoogleService extends IntentService {

	// Projection array. Creating indices for this array instead of doing
	// dynamic lookups improves performance.
	public static final String[] EVENT_PROJECTION = new String[] {
		Calendars._ID,                           // 0
		Calendars.ACCOUNT_NAME,                  // 1
		Calendars.CALENDAR_DISPLAY_NAME,         // 2
		Calendars.OWNER_ACCOUNT                  // 3
	};

	// The indices for the projection array above.
	private static final int PROJECTION_ID_INDEX = 0;
	private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
	private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
	private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
	
	private static final String TAG = "GOOGLE";

	public GoogleService() {
		super("GoogleService");
	}

	/* (non-Javadoc)
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "Google Service init");

		// Run query to read all the visible calendars
		Cursor calCursor = 
			      getContentResolver().
			            query(Calendars.CONTENT_URI, 
			                  EVENT_PROJECTION, 
			                  Calendars.VISIBLE + " = 1", 
			                  null, 
			                  Calendars._ID + " ASC");
		if (calCursor.moveToFirst()) {
			do {
				long id = calCursor.getLong(0);
				String displayName = calCursor.getString(2);
				
				Log.d(TAG, "Calendar Names: " + displayName);
				
			} while (calCursor.moveToNext());
		}
		
		String[] proj = 
				new String[]{
				Events._ID, 
				Events.DTSTART, 
				Events.DTEND, 
				Events.TITLE,
				Events.EVENT_LOCATION};
		Cursor cursor = 
				getContentResolver().
				query(
						Events.CONTENT_URI, 
						proj, 
						null, 
						null, 
						null);
		

		// Content values array for bulk insert or update
		ContentValues[] values = new ContentValues[cursor.getCount()];
		int i = 0;
		
		try {
			if (cursor.moveToFirst()) {
				// read event data
				do {

					Format df = DateFormat.getDateFormat(this);
					Format tf = DateFormat.getTimeFormat(this);

					// event details
					Long event_id = cursor.getLong(0);
					String event_title = cursor.getString(3);
					String start_date = df.format(cursor.getLong(1));
					String start_time = tf.format(cursor.getLong(1));
					String end_date = df.format(cursor.getLong(2));
					String end_time = tf.format(cursor.getLong(2));
					String location = cursor.getString(4);

					// For reminder time we have to see another table
					String[] reminder_projection = 
							new String[]{
							Reminders._ID, 
							Reminders.MINUTES};
					Cursor cursor_reminder = 
							getContentResolver().
							query(
									Reminders.CONTENT_URI, 
									reminder_projection, 
									Reminders._ID + " = ? ", 
									new String[]{Long.toString(event_id)}, 
									null);
					int reminder = 0;
					try {
						if (cursor_reminder.moveToFirst()) {
							Log.d(TAG, "Reminder value: " + cursor_reminder.getString(1));
							reminder = Integer.parseInt(cursor_reminder.getString(1));
						}
					} finally {
						if( cursor_reminder != null && !cursor_reminder.isClosed() )
							cursor_reminder.close();
					}

					if (event_title != null) {
						try {
							values[i] = new ContentValues();

							values[i].put(DBSQLiteHelper.COLUMN_TABLE, "GOOGLE");
							values[i].put(DBSQLiteHelper.COLUMN_TITLE, event_title);

							if (location != null)
								values[i].put(DBSQLiteHelper.COLUMN_LOCATION, location);
							else
								values[i].put(DBSQLiteHelper.COLUMN_LOCATION, " ");

							values[i].put(DBSQLiteHelper.COLUMN_START_DATE, 
									CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(start_date.substring(3, 5)), 
											Integer.parseInt(start_date.substring(0, 2)), start_date.substring(6, start_date.length())));
							values[i].put(DBSQLiteHelper.COLUMN_START_TIME, timeFormat(start_time));

							if (end_date != null)
								values[i].put(DBSQLiteHelper.COLUMN_END_DATE, CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(end_date.substring(3, 5)), 
										Integer.parseInt(end_date.substring(0, 2)), end_date.substring(6, end_date.length())));

							if(end_time != null)
								values[i].put(DBSQLiteHelper.COLUMN_END_TIME, timeFormat(end_time));

							values[i].put(DBSQLiteHelper.COLUMN_REMINDER_TIME, reminder);

						}
						catch(Exception e) {
							Log.e(TAG, "Exception caught: " + e.getMessage());
						}
						//				Log.d(TAG, cursor.getString(0) + " ; " + df.format(cursor.getLong(1)) + ":" 
						//						+ tf.format(cursor.getLong(1)).length() + " ; " 
						//						+ df.format(cursor.getLong(2)) + ":" + tf.format(cursor.getLong(2))
						//						+ " ; " + cursor.getString(3));
						i++;
					}
				} while (cursor.moveToNext());

				// Insert event
				getContentResolver().bulkInsert(DBEventsContentProvider.CONTENT_URI, values);
				Log.d(TAG, "Insert Complete!");
			}
		}
		finally {
			if( cursor != null && !cursor.isClosed() )
				cursor.close();
		}
	}

	private int timeFormat(String event_time) {
		String am_pm = event_time.substring(event_time.length() - 2, event_time.length());
		
		int hour = 0;
		int min = 0;
		
		String[] split_val = event_time.split(":"); 
		
		if (am_pm.equals("PM")) {
			hour = Integer.parseInt(split_val[0]) + 12;
		}
		else {
			hour = Integer.parseInt(split_val[0]);
		}
		
		String[] split_last_part = split_val[1].split(" ");
		
		min = Integer.parseInt(split_last_part[0]);
		return CurrentDateTimeConverter.timeDateFormatter(hour, min, "00");
	}

}
