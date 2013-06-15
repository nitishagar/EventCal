/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.services.samples.calendar.android;     

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Objects;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class GoogleService extends IntentService {

  final HttpTransport transport = AndroidHttp.newCompatibleTransport();

  final JsonFactory jsonFactory = new GsonFactory();

  GoogleAccountCredential credential;

  CalendarModel model = new CalendarModel();
  
  EventModel model_event = new EventModel();

  com.google.api.services.calendar.Calendar client;

  int numAsyncTasks;
  
  ContentValues value = new ContentValues();
  
  public GoogleService(){
    super("service");
    Log.d("info","google calendar service entry");
  }
  
  
  @Override
  protected void onHandleIntent(Intent arg0) {
    client = new com.google.api.services.calendar.Calendar.Builder(
        transport, jsonFactory, credential).setApplicationName("google calendar service")
        .build();
    
    new AsyncLoadCalendars().run();
  }
  
  
  
  
  

  
  /* Calendar Async Task */
  abstract class CalendarAsyncTask extends AsyncTask<Void, Void, Boolean> {

    CalendarAsyncTask() {
      Log.d("info","calendarAsyncTask entry");
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      numAsyncTasks++;
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
      try {
        doInBackground();
        return true;
      } catch (final GooglePlayServicesAvailabilityIOException e) {
        e.printStackTrace();
      } catch (UserRecoverableAuthIOException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected final void onPostExecute(Boolean success) {
      super.onPostExecute(success);
      if (success) {
        List<String> ID = model.calendarID;
        for (String s: ID){
          new AsyncLoadEvents(s).run();
        }
      }
    }

    abstract protected void doInBackground() throws IOException;
  }
  
  /*---------------------*/
  
  /* Event Async Task */
  abstract class EventAsyncTask extends AsyncTask<Void, Void, Boolean> {

    EventAsyncTask() {
      Log.d("info","eventAsyncTask entry");
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      numAsyncTasks++;
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
      try {
        doInBackground();
        return true;
      } catch (final GooglePlayServicesAvailabilityIOException e) {
        e.printStackTrace();
      } catch (UserRecoverableAuthIOException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return false;
    }

    @Override
    protected final void onPostExecute(Boolean success) {
      super.onPostExecute(success);
      if (success) { 
        // TO DO
        // PUT EVENTS IN DB
        Log.d("info","done everything!"); 
      }
    }

    abstract protected void doInBackground() throws IOException;
  }
  /*---------------------*/
  
  /* Load Calendars */
  class AsyncLoadCalendars extends CalendarAsyncTask {

    AsyncLoadCalendars() {
      Log.d("info","AsyncLoadCalendar entry");
    }

    @Override
    protected void doInBackground() throws IOException {
      CalendarList feed = client.calendarList().list().setFields(CalendarInfo.FEED_FIELDS).execute();
      model.reset(feed.getItems());
    }

    void run() {
      new AsyncLoadCalendars().execute();
    }
  }
  /*----------------*/
  
  
  /* Load Events */
  class AsyncLoadEvents extends EventAsyncTask{

    
    String id;
    
    AsyncLoadEvents(String id) {
      super();
      this.id = id;
    }

    @Override
    protected void doInBackground() throws IOException {
      Events feed = client.events().list(id).execute();
      model_event.reset(feed.getItems());
    }
    void run() {
      new AsyncLoadEvents(id).execute();
    }
  }
  /*-----------------*/
  
  /* CalendarInfo */
  class CalendarInfo implements Comparable<CalendarInfo>, Cloneable {

    static final String FIELDS = "id,summary";
    static final String FEED_FIELDS = "items(" + FIELDS + ")";

    String id;
    String summary;

    CalendarInfo(String id, String summary) {
      this.id = id;
      this.summary = summary;
    }

    CalendarInfo(Calendar calendar) {
      update(calendar);
    }

    CalendarInfo(CalendarListEntry calendar) {
      update(calendar);
    }

    @Override
    public String toString() {
      return Objects.toStringHelper(CalendarInfo.class).add("id", id).add("summary", summary)
          .toString();
    }

    public int compareTo(CalendarInfo other) {
      return summary.compareTo(other.summary);
    }

    @Override
    public CalendarInfo clone() {
      try {
        return (CalendarInfo) super.clone();
      } catch (CloneNotSupportedException exception) {
        // should not happen
        throw new RuntimeException(exception);
      }
    }

    void update(Calendar calendar) {
      id = calendar.getId();
      summary = calendar.getSummary();
    }

    void update(CalendarListEntry calendar) {
      id = calendar.getId();
      summary = calendar.getSummary();
    }
  }
  /*-------------------*/
  
  /* Event Info */
  class EventInfo implements Comparable<EventInfo>, Cloneable {

    static final String FIELDS = "id,summary";
    static final String FEED_FIELDS = "items(" + FIELDS + ")";

    String id;
    String summary;

    EventInfo(String id, String summary) {
      this.id = id;
      this.summary = summary;
    }

    EventInfo(Event e){
      update(e);
    }
    @Override
    public String toString() {
      return Objects.toStringHelper(EventInfo.class).add("id", id).add("summary", summary)
          .toString();
    }

    public int compareTo(EventInfo other) {
      return summary.compareTo(other.summary);
    }

    @Override
    public EventInfo clone() {
      try {
        return (EventInfo) super.clone();
      } catch (CloneNotSupportedException exception) {
        // should not happen
        throw new RuntimeException(exception);
      }
    }

    void update(Event calendar) {
      id = calendar.getId();
      summary = calendar.getSummary();
    }

  }
  
  /*--------------------*/
  
  /* hashmap for calendars */
  class CalendarModel {

    private final Map<String, CalendarInfo> calendars = new HashMap<String, CalendarInfo>();
    List<String> calendarID = new ArrayList<String>();
    
    int size() {
      synchronized (calendars) {
        return calendars.size();
      }
    }

    void remove(String id) {
      synchronized (calendars) {
        calendars.remove(id);
      }
    }

    CalendarInfo get(String id) {
      synchronized (calendars) {
        return calendars.get(id);
      }
    }

    void add(Calendar calendarToAdd) {
      synchronized (calendars) {
        CalendarInfo found = get(calendarToAdd.getId());
        if (found == null) {
          calendars.put(calendarToAdd.getId(), new CalendarInfo(calendarToAdd));
          calendarID.add(calendarToAdd.getId());
        } else {
          found.update(calendarToAdd);
        }
      }
    }

    void add(CalendarListEntry calendarToAdd) {
      synchronized (calendars) {
        CalendarInfo found = get(calendarToAdd.getId());
        if (found == null) {
          calendars.put(calendarToAdd.getId(), new CalendarInfo(calendarToAdd));
          calendarID.add(calendarToAdd.getId());
        } else {
          found.update(calendarToAdd);
        }
      }
    }

    void reset(List<CalendarListEntry> calendarsToAdd) {
      synchronized (calendars) {
        calendars.clear();
        calendarID.clear();
        for (CalendarListEntry calendarToAdd : calendarsToAdd) {
          add(calendarToAdd);
        }
      }
    }

    public CalendarInfo[] toSortedArray() {
      synchronized (calendars) {
        List<CalendarInfo> result = new ArrayList<CalendarInfo>();
        for (CalendarInfo calendar : calendars.values()) {
          result.add(calendar.clone());
        }
        Collections.sort(result);
        return result.toArray(new CalendarInfo[0]);
      }
    }
  }
  /*--------------------------*/
  
  /* hashmap for events */
  class EventModel {

    
    Map<String, EventInfo> calendars = new HashMap<String, EventInfo>();
    List<String> e = new ArrayList<String>();
    

    int size() {
      synchronized (calendars) {
        return calendars.size();
      }
    }

    void remove(String id) {
      synchronized (calendars) {
        calendars.remove(id);
      }
    }

    EventInfo get(String id) {
      synchronized (calendars) {
        return calendars.get(id);
      }
    }

    void add(Event calendarToAdd) {
      synchronized (calendars) {
        //EventInfo found = get(calendarToAdd.getId());
        //if (found == null) {
          calendars.put(calendarToAdd.getId(), new EventInfo(calendarToAdd));
          e.add(calendarToAdd.getId());
        //} else {
        //  found.update(calendarToAdd);
       // }
      }
    }


    void reset(List<Event> eventsToAdd) {
      synchronized (calendars) {
        calendars.clear();
        
        for (Event eventToAdd : eventsToAdd) {
          Log.d("Info","added " + eventToAdd.getSummary());
          Log.d("Info","added " + eventToAdd.getId());
          add(eventToAdd);
         // EventInfo e = new EventInfo(eventToAdd);
         // ret.add(e);
        }
      }
    }

    

    public ArrayList<String> toSortedArray() {
      synchronized (calendars) {
        ArrayList<String> result = new ArrayList<String>();
        for (EventInfo calendar : calendars.values()) {
          result.add(calendar.clone().summary);
          Log.d("info","adding");
        }
        Collections.sort(result);
        Log.d("info","size is "+ result.size());
        Log.d("info","map size is "+ calendars.size());
        return result;
      }
    }
     
  }
   /*--------------------------------------*/
}
