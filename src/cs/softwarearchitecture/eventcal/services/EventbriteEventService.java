package cs.softwarearchitecture.eventcal.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.Log;
import cs.softwarearchitecture.eventcal.utility.CurrentDateTimeConverter;


public class EventbriteEventService extends TemplateService {

	private String mEventbriteID;


	/** 
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public EventbriteEventService() {
		super();
	}

	private static final String TAG = "EventbriteEventService";


	protected void parseEvent(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		parseEventsArray(reader);

		reader.close();

	}

	@Override
	protected void parseEventsArray(JsonReader reader) throws IOException {
		Log.d(EventbriteEventService.TAG, "i'm here");
		reader.beginObject(); 
		reader.nextName();// events
		reader.beginArray();
		reader.skipValue();
		while (reader.hasNext()) {
			mEvents.add(getEventBriteEvents(reader));
		}
		Log.d(EventbriteEventService.TAG, "i'm here 4");

		reader.endArray();
		reader.endObject();
	}



	protected Event getEventBriteEvents(JsonReader reader) throws IOException {
		Event event = new Event();

		reader.beginObject();
		Log.d(EventbriteEventService.TAG, "i'm here 88");
		reader.nextName();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			Log.d(EventbriteEventService.TAG, name);
			if (name.equals("title")) {
				event.title = reader.nextString();
				Log.d(EventbriteEventService.TAG, event.title);
			} else if(name.equals("venue")){
				event.location = getLocation(reader);
				Log.d(EventbriteEventService.TAG, event.location);
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
		event.valid = 1;
		return event;
	}
	
	protected String getLocation(JsonReader reader) throws IOException{
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
	
	
	protected ArrayList<Integer> formatTimeAndDate(String timeDate){
		Log.d(EventbriteEventService.TAG, timeDate);
		ArrayList<Integer> returnFormatted = new ArrayList<Integer>();
		String[] splitTimeDate = timeDate.split(" "); // [0] = date, [1] = time
		String[] splitDate = splitTimeDate[0].split("-");
		String[] splitTime = splitTimeDate[1].split(":");
		Integer date = CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(splitDate[2]), Integer.parseInt(splitDate[1]),splitDate[0]);
		Integer time = CurrentDateTimeConverter.timeDateFormatter(Integer.parseInt(splitTime[0]), Integer.parseInt(splitTime[1]),splitTime[2]);
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
		SharedPreferences eventbritePreference = getSharedPreferences("eventbrite-session", Context.MODE_PRIVATE);
		mEventbriteID = eventbritePreference.getString("user_id", null);

		Log.d(EventbriteEventService.TAG, "I'm here");
		mAPIKey = "SCGKMFBZ2BGVSH5XL2";
		try {
			mEventURL = new URL("https://www.eventbrite.com/json/event_search?app_key=" + mAPIKey + "&user_key=" + mEventbriteID + "&city=Waterloo&country=CA");
			feedingDatabase("EVENTBRITE");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			mURLConnection.disconnect();
		}



	}
}