package cs.softwarearchitecture.eventcal.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import android.content.Intent;
import android.util.JsonReader;
import android.util.Log;

public class UWEventService extends TemplateService {
	
	private static final String TAG = "UWEventService";

	/** 
	 * A constructor is required, and must call the super IntentService(String)
	 * constructor with a name for the worker thread.
	 */
	public UWEventService() {
		super();
	}

	@Override
	protected void parseEvent(InputStream in) throws IOException {
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));

		parseEventsArray(reader);
		reader.close();
	}

	@Override
	protected void parseEventsArray(JsonReader reader) throws IOException {
		Log.d(UWEventService.TAG, "i'm here");
		reader.beginObject(); //start
		reader.nextName();// response"
		reader.beginObject();
		reader.nextName();// meta
		reader.skipValue();
		Log.d(UWEventService.TAG, "i'm here 11");
		reader.nextName();// data
		Log.d(UWEventService.TAG, "i'm here 5");
		reader.beginObject();
		Log.d(UWEventService.TAG, "i'm here 6");
		reader.nextName();//result
		Log.d(UWEventService.TAG, "i'm here 7");
		reader.beginArray();

		while (reader.hasNext()) {
			Log.d(UWEventService.TAG, "i'm here 3");
			Event addEvent = getUWEvents(reader);
			if (addEvent.valid == 1){
				mEvents.add(addEvent);
			}
			Log.d(UWEventService.TAG, "i'm here 77");
		}
		Log.d(UWEventService.TAG, "i'm here 4");
		reader.endArray();
		reader.endObject();
		reader.endObject();
		reader.endObject();
	}

	protected Event getUWEvents(JsonReader reader) throws IOException {
		Event event = new Event();

		reader.beginObject();
		Log.d(UWEventService.TAG, "i'm here 88");
		while (reader.hasNext()) {
			String name = reader.nextName();
			Log.d(UWEventService.TAG, name);
			if (name.equals("Title")) {
				event.title = reader.nextString();
				Log.d(UWEventService.TAG, event.title);
			} else if(name.equals("Where")){
				event.location = reader.nextString();
				Log.d(UWEventService.TAG, event.location);
			} 
			else if(name.equals("When")){
				String timeDate = reader.nextString();
				if (timeDate.equals("")){
					event.startTime = 0;
					event.startDate = 0;
					event.endTime = 0;
					event.endDate = 0;
					event.valid = 0;
				}
				else {
					ArrayList<Integer> timeAndDate = parseEventTime(timeDate);
					event.startTime = timeAndDate.get(0);
					event.startDate = timeAndDate.get(1);
					event.endTime = timeAndDate.get(2);
					event.endDate = timeAndDate.get(3);
					event.valid = 1;
				}
			}
			else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return event;
	}

	protected ArrayList<Integer> parseEventTime(String timeString){	
		Log.d(UWEventService.TAG, timeString);
		ArrayList<Integer> returnTime = new ArrayList<Integer>();
		String[] splitString = timeString.split(" ");
		Log.d(UWEventService.TAG, splitString[0]);
		Log.d(UWEventService.TAG, splitString[1]);
		Log.d(UWEventService.TAG, splitString[2]);
		Log.d(UWEventService.TAG, splitString[3]);
		Log.d(UWEventService.TAG, splitString[4]);
		Log.d(UWEventService.TAG, splitString[5]);
		Log.d(UWEventService.TAG, splitString[6]);
		Log.d(UWEventService.TAG, splitString[7]);
		Log.d(UWEventService.TAG, splitString[8]);
		Log.d(UWEventService.TAG, splitString[9]);
		Log.d(UWEventService.TAG, splitString[10]);
		Log.d(UWEventService.TAG, splitString[11]);
		Log.d(UWEventService.TAG, splitString[12]);
		Integer startTime;
		Integer startDate;
		Integer endTime;
		Integer endDate;
		Log.d(UWEventService.TAG,  "before parsing time and date");
		if (splitString[5].equals("am") || splitString[5].equals("pm")){
			Log.d(UWEventService.TAG,  "UW different format");

			String[] startTimeSplit = splitString[4].split(":");
			Log.d(UWEventService.TAG,  startTimeSplit[0]);
			Log.d(UWEventService.TAG,  startTimeSplit[1]);
			String[] endTimeSplit = splitString[12].split(":");
			startTime =  Integer.parseInt(findTime(startTimeSplit[0], startTimeSplit[1], splitString[5]));
			startDate = Integer.parseInt(findDate(splitString[1],splitString[2],splitString[3]));
			endTime = Integer.parseInt(findTime(endTimeSplit[0], endTimeSplit[1] , splitString[13]));
			endDate = Integer.parseInt(findDate(splitString[9],splitString[10],splitString[11]));
		}
		else {
			String[] startTimeSplit = splitString[4].split(":");
			String[] endTimeSplit = splitString[11].split(":");
			startTime = Integer.parseInt("1" + startTimeSplit[0] + startTimeSplit[1] + startTimeSplit[2]);
			Log.d(UWEventService.TAG, splitString[1]);
			Log.d(UWEventService.TAG, splitString[2]);
			Log.d(UWEventService.TAG, splitString[3]);
			startDate = Integer.parseInt(findDate(splitString[1],splitString[2],splitString[3]));
			endTime = Integer.parseInt("1" + endTimeSplit[0] + endTimeSplit[1] + endTimeSplit[2]);
			endDate = Integer.parseInt(findDate(splitString[8],splitString[9],splitString[10]));
		}
		returnTime.add(startTime);
		returnTime.add(startDate);
		returnTime.add(endTime);
		returnTime.add(endDate);
		Log.d(UWEventService.TAG, "lalala");
		return returnTime;
	}

	protected String findTime(String hour, String minutes, String amOrPm){

		String returnTime;
		if (amOrPm.equals("pm") && !hour.equals("12")){
			int intHour = Integer.parseInt(hour);
			intHour += 12;
			String returnHour = Integer.toString(intHour);
			returnTime = "1" + returnHour + minutes + "00";
		} else if (amOrPm.equals("pm") && hour.equals("12")){
			returnTime =  "1" + hour + minutes + "00";
		} else if (amOrPm.equals("am") && (hour.equals("10") || hour.equals("11") || hour.equals("12"))){
			returnTime =  "1" + hour + minutes + "00";
		} 
		else {
			returnTime = "10" + hour + minutes + "00";
		}
		Log.d(UWEventService.TAG, returnTime);
		return returnTime;

	}

	public String findDate(String day, String month, String year){
		String date = "";
		if (month.equals("Jan")){
			date = day + "01" + year;
		} else if (month.equals("Feb")){
			date = day + "02" + year;
		} else if (month.equals("Mar")){
			date = day + "03" + year;
		} else if (month.equals("Apr")){
			date = day + "04" + year;
		} else if (month.equals("May")){
			date = day + "05" + year;
		} else if (month.equals("Jun")){
			date = day + "06" + year;
		} else if (month.equals("Jul")){
			date = day + "07" + year;
		} else if (month.equals("Aug")){
			date = day + "08" + year;
		} else if (month.equals("Sep")){
			date = day + "09" + year;
		} else if (month.equals("Oct")){
			date = day + "10" + year;
		} else if (month.equals("Nov")){
			date = day + "11" + year;
		} else if (month.equals("Dec")){
			date = day + "12" + year;
		}
		date = "1" + date;
		Log.d(UWEventService.TAG, date);
		return date;
	}
	
	/**
	 * The IntentService calls this method from the default worker thread with
	 * the intent that started the service. When this method returns, IntentService
	 * stops the service, as appropriate.
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(UWEventService.TAG, "I'm here");
		mAPIKey = "caeecfb4db9804ec82b9adbfbdd151a3";
		try {
			mEventURL = new URL("http://api.uwaterloo.ca/public/v1/?key=" + mAPIKey + "&service=CalendarEvents&output=json");
			feedingDatabase("UW", mEventURL);

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