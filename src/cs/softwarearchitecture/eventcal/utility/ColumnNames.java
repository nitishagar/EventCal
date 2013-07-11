package cs.softwarearchitecture.eventcal.utility;

import android.database.sqlite.SQLiteDatabase;

public interface ColumnNames {

	// Table initialization constants
	public static final String TABLE_NAME = "EventCalDB";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TABLE = "table_source";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_START_TIME = "start_time";
	public static final String COLUMN_START_DATE = "start_date";
	public static final String COLUMN_END_TIME = "end_time";
	public static final String COLUMN_END_DATE = "end_date";
	public static final String COLUMN_LOCATION = "location";
	public static final String COLUMN_REMINDER_TIME = "reminder_time";
	public static final String COLUMN_REV_START_DATE = "reverse_date";

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	public abstract void onCreate(SQLiteDatabase db);

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion);

}