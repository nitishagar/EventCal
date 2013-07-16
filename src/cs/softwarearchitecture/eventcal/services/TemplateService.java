package cs.softwarearchitecture.eventcal.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.JsonReader;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.utility.ColumnNames;

class TemplateService extends IntentService {
	
	public class Event{
		String title;
		String location;
		Integer startTime;
		Integer startDate;
		Integer endTime;
		Integer endDate;
		int valid;
	}
	
	InputStream mInputStream;
	List<Event> mEvents = new ArrayList<Event>();
	URL mEventURL;
	HttpURLConnection mURLConnection;
	String mAPIKey;
	
	/** 
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public TemplateService() {
		super("TemplateService");
	}

	protected void parseEvents(InputStream in) throws IOException {
	}

	protected void parseEventsArray(JsonReader reader) throws IOException {
	}


	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {

	}
	
	/**
	 * Database fed with the data 
	 * @param eventType 
	 * @throws IOException
	 */
	protected void feedingDatabase(String eventType) throws IOException {
		mURLConnection = (HttpURLConnection)mEventURL.openConnection();
		mInputStream = mURLConnection.getInputStream();
		parseEvents(mInputStream);
		int eventSize = mEvents.size();
		int iter = 0;
		while (iter < eventSize){
			ContentValues values = new ContentValues();
			Event insertEvent = mEvents.get(iter);
			values.put(ColumnNames.COLUMN_TABLE, "UW");
			values.put(ColumnNames.COLUMN_TITLE, insertEvent.title);
			values.put(ColumnNames.COLUMN_START_DATE, insertEvent.startDate);
			values.put(ColumnNames.COLUMN_START_TIME, insertEvent.startTime);
			values.put(ColumnNames.COLUMN_END_TIME, insertEvent.endTime);
			values.put(ColumnNames.COLUMN_END_DATE, insertEvent.endDate);
			values.put(ColumnNames.COLUMN_LOCATION, insertEvent.location);
			values.put(ColumnNames.COLUMN_REMINDER_TIME,"");
			getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);
			iter += 1;
		}
	}
}