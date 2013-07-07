package cs.softwarearchitecture.eventcal.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.util.JsonReader;
import android.util.Log;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.CurrentDateTimeConverter;


public class getEventBriteEventService extends IntentService {
	public class Event{
	      String title;
	      String location;
	      Integer startTime;
	      Integer startDate;
	      Integer endTime;
	      Integer endDate;
	  }
  /** 
   * A constructor is required, and must call the super IntentService(String)
   * constructor with a name for the worker thread.
   */
  public getEventBriteEventService() {
      super("getEventBriteEventService");
  }
  
  private static final String TAG = "getEventBriteEventService";
  InputStream in;
  List<Event> EventBriteEvents = new ArrayList();
  //private ListView eventsListView;
  //private ArrayAdapter arrayAdapter;
  URL url;
  HttpURLConnection urlConnection;
  String EventBriteAPIKey = "SCGKMFBZ2BGVSH5XL2";
  
	public void parseEventBriteEvents(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	   // try {
	    	
	    parseEventBriteEventsArray(reader);
	    //}
	    //finally {
	    	reader.close();
	  //  }
	}

	public void parseEventBriteEventsArray(JsonReader reader) throws IOException {
		//List eventList = new ArrayList();
	    Log.d(getEventBriteEventService.TAG, "i'm here");
	    reader.beginObject(); 
	    reader.nextName();// events
	    reader.beginArray();
	    reader.skipValue();
	    while (reader.hasNext()) {
	    	EventBriteEvents.add(getEventBriteEvents(reader));
	    }
	    Log.d(getEventBriteEventService.TAG, "i'm here 4");

	    reader.endArray();
	    reader.endObject();
	    //return eventList;
	}
	
	
		
	public Event getEventBriteEvents(JsonReader reader) throws IOException {
		Event event = new Event();
		
	    reader.beginObject();
	    Log.d(getEventBriteEventService.TAG, "i'm here 88");
	    reader.nextName();
	    reader.beginObject();
	    while (reader.hasNext()) {
	    	String name = reader.nextName();
	    	 Log.d(getEventBriteEventService.TAG, name);
	        if (name.equals("title")) {
	        	event.title = reader.nextString();
	        	Log.d(getEventBriteEventService.TAG, event.title);
	        } else if(name.equals("venue")){
	        	event.location = getLoc(reader);
	        	Log.d(getEventBriteEventService.TAG, event.location);
	        } 
	        else if(name.equals("start_date")){
	        	ArrayList<Integer> startTimeAndDate = formatTimeAndDate(reader.nextString());
	        	event.startTime = startTimeAndDate.get(0);
	        	event.startDate = startTimeAndDate.get(1);
	        }
	        else if(name.equals("end_date")){
	        	ArrayList<Integer> endTimeAndDate = formatTimeAndDate(reader.nextString());
	        	event.endTime = endTimeAndDate.get(0);
	        	event.endDate = endTimeAndDate.get(1);
	        }
	        else {
	        	reader.skipValue();
	        }
	    }
	    reader.endObject();
	    reader.endObject();
	    return event;
	}
	public String getLoc(JsonReader reader) throws IOException{
		String location = "";
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("address")) {
				location = reader.nextString();
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return location;
	}		
	public ArrayList<Integer> formatTimeAndDate(String timeDate){
		Log.d(getEventBriteEventService.TAG, timeDate);
		ArrayList<Integer> returnFormatted = new ArrayList<Integer>();
		String[] splitTimeDate = timeDate.split(" "); // [0] = date, [1] = time
		String[] splitDate = splitTimeDate[0].split("-");
		String[] splitTime = splitTimeDate[1].split(":");
		Integer date = CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]),splitDate[0]);
		Integer time = CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]),splitTime[2]);
		//String date = splitDate[1] + splitDate[2] + splitDate[0];
		//String time = splitTime[0] + splitTime[1] + splitTime[2];
		//Log.d(getEventBriteEventService.TAG, time);
		//Log.d(getEventBriteEventService.TAG, date);
		returnFormatted.add(time);
		returnFormatted.add(date);
		return returnFormatted;
	}


  /**
   * The IntentService calls this method from the default worker thread with
   * the intent that started the service. When this method returns, IntentService
   * stops the service, as appropriate.
   */
  @Override
  protected void onHandleIntent(Intent intent) {
	  Log.d(getEventBriteEventService.TAG, "I'm here");
	  
		try {
    		url = new URL("https://www.eventbrite.com/json/event_search?app_key=" + EventBriteAPIKey + "&city=Waterloo&country=CA");
    		urlConnection = (HttpURLConnection)url.openConnection();
    		in = urlConnection.getInputStream();
    		parseEventBriteEvents(in);
    		int i = EventBriteEvents.size();
    		int j = 0;
    		
    		
    		while (j < i){
	    		ContentValues values = new ContentValues();
	    		Event insertEvent = EventBriteEvents.get(j);
	    		/*Log.d(getEventBriteEventService.TAG, insertEvent.title);
	    		Log.d(getEventBriteEventService.TAG, insertEvent.location);
	    		Log.d(getEventBriteEventService.TAG, insertEvent.startTime);
	    		Log.d(getEventBriteEventService.TAG, insertEvent.startDate);
	    		Log.d(getEventBriteEventService.TAG, insertEvent.endTime);
	    		Log.d(getEventBriteEventService.TAG, insertEvent.endDate);
	    		int insertStartTime = Integer.parseInt(insertEvent.startTime);
	    		int insertStartDate = Integer.parseInt(insertEvent.startDate);
	    		int insertEndTime = Integer.parseInt(insertEvent.endTime);
	    		int insertEndDate = Integer.parseInt(insertEvent.endDate);*/
	    		
	            values.put(DBSQLiteHelper.COLUMN_TABLE, "EVENTBRITE");
	            values.put(DBSQLiteHelper.COLUMN_TITLE, insertEvent.title);
	            values.put(DBSQLiteHelper.COLUMN_START_DATE,  insertEvent.startDate);
	            values.put(DBSQLiteHelper.COLUMN_START_TIME, insertEvent.startTime);
	            values.put(DBSQLiteHelper.COLUMN_END_TIME,  insertEvent.endTime);
	            values.put(DBSQLiteHelper.COLUMN_END_DATE,  insertEvent.endDate);
	            values.put(DBSQLiteHelper.COLUMN_LOCATION, insertEvent.location);

	            getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);
	            j += 1;
    		}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			urlConnection.disconnect();
		}
		
		
		/* eventsListView = (ListView) findViewById(R.id.calendarEvent_list);

	        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, UWEvents);
	 
	        eventsListView.setAdapter(arrayAdapter); 
	    */
  }
}