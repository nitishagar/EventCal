package cs.softwarearchitecture.eventcal.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import cs.softwarearchitecture.eventcal.DefaultView;
import cs.softwarearchitecture.eventcal.extras.ColumnNames;

/**
 * @author nitishagarwal
 *
 */
public class DBSQLiteHelper extends SQLiteOpenHelper implements ColumnNames {

	private static final String DATABASE_NAME = "eventcal.db";
	private static final int DATABASE_VERSION = 4;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "CREATE TABLE "
	      + TABLE_NAME + "(" + COLUMN_ID
	      + " integer primary key autoincrement, " + COLUMN_TABLE + " text not null, " 
	      + COLUMN_TITLE + " text not null, " + COLUMN_START_TIME + " integer not null , "
	      + COLUMN_START_DATE + " integer not null , " + COLUMN_END_TIME + " integer null, "  
	      + COLUMN_END_DATE + " integer null, " + COLUMN_REMINDER_TIME + " integer null, "
	      + COLUMN_REV_START_DATE + " integer not null , " + COLUMN_LOCATION 
	      + " text null, UNIQUE( " + COLUMN_TABLE + ", " 
	      + COLUMN_TITLE + ", " + COLUMN_START_DATE + ") ON CONFLICT REPLACE);";

	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DBSQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	public DBSQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DefaultView.TAG, "Upgrading database from version " + oldVersion + "to"
		            + newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}

}
