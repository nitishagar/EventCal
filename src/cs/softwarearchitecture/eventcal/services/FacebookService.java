/**
 * 
 */
package cs.softwarearchitecture.eventcal.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

import cs.softwarearchitecture.eventcal.CurrentDateTimeConverter;
import cs.softwarearchitecture.eventcal.DefaultView;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

/**
 * @author nitishagarwal
 *
 */
@SuppressWarnings("deprecation")
public class FacebookService extends IntentService {

	// Application ID from Facebook Developers site
	public static final String APP_ID = "377349995710558";

	// URI
	private Uri eventURI = null;
	
	// Async Task runner
	@SuppressWarnings("deprecation")
	protected AsyncFacebookRunner mAsyncRunner;

	public FacebookService() {
		super("FacebookService");
	}

	/**
	 * Retrieve Session token and launch event data request thread
	 * @pre mFacebook != null (session token)
	 * @post mAsyncRunner(request)
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(DefaultView.TAG, "Handle Intent Activated!");
		try{
			if(DefaultView.mFacebook.isSessionValid()){
				Log.d(DefaultView.TAG, "Handle Intent with Async");
				mAsyncRunner = new AsyncFacebookRunner(DefaultView.mFacebook);
				mAsyncRunner.request("me/events", new EventRequenstListener());
			}
		}
		catch(Exception e){
			Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
		}
	}

	/**
	 * @author nitishagarwal
	 *
	 */
	/**
	 * Callback function to interact with Content Provider
	 * @post bulkInsert(ContentValues)
	 */
	private class EventRequenstListener implements RequestListener {

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onComplete(java.lang.String, java.lang.Object)
		 */
		@Override
		public void onComplete(String response, Object state) {
			try {
				// Process the response here: executed in background thread
				Log.d(DefaultView.TAG, "Response: " + response.toString());

				final JSONObject json = new JSONObject(response);
				JSONArray jsonArray = json.getJSONArray("data");

				// Content values array for bulk insert or update
				ContentValues[] values = new ContentValues[jsonArray.length()];
				CurrentDateTimeConverter currentDateTimeConverter;
				
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject event = jsonArray.getJSONObject(i);
					
					Log.d(DefaultView.TAG, event.getString("start_time"));
					currentDateTimeConverter = new CurrentDateTimeConverter(event.getString("start_time"));
					
					//					FacebookEvent newEvent;
					values[i] = new ContentValues();
					
					values[i].put(DBSQLiteHelper.COLUMN_TABLE, "FACEBOOK");
					values[i].put(DBSQLiteHelper.COLUMN_TITLE, event.getString("name"));
					values[i].put(DBSQLiteHelper.COLUMN_LOCATION, event.getString("location"));

					try{
						String end_time = event.getString("end_time");
						values[i].put(DBSQLiteHelper.COLUMN_START_DATE, currentDateTimeConverter.getDate());
						values[i].put(DBSQLiteHelper.COLUMN_START_TIME, currentDateTimeConverter.getTime());
						
						CurrentDateTimeConverter endCurrentTimeConverter = new CurrentDateTimeConverter(end_time);
						values[i].put(DBSQLiteHelper.COLUMN_END_DATE, endCurrentTimeConverter.getDate());
						values[i].put(DBSQLiteHelper.COLUMN_END_TIME, endCurrentTimeConverter.getTime());
						
						//						newEvent = new FacebookEvent(event.getString("id"),
						//								event.getString("name"),
						//								event.getString("start_time"),
						//								event.getString("location"));
					}catch (JSONException e){

						values[i].put(DBSQLiteHelper.COLUMN_START_DATE, currentDateTimeConverter.getDate());
						values[i].put(DBSQLiteHelper.COLUMN_START_TIME, currentDateTimeConverter.getTime());
						values[i].put(DBSQLiteHelper.COLUMN_END_DATE, currentDateTimeConverter.getDate());
						if (currentDateTimeConverter.getDate() != 0)
							values[i].put(DBSQLiteHelper.COLUMN_END_TIME, 
									((currentDateTimeConverter.getTime() + 30000) > 1240000) ?(currentDateTimeConverter.getTime() + 30000 - 240000):(currentDateTimeConverter.getTime() + 30000) );
						else
							values[i].put(DBSQLiteHelper.COLUMN_END_TIME, 0);
						
					
						//						newEvent = new FacebookEvent(event.getString("id"),
						//								event.getString("name"),
						//								event.getString("start_time"),
						//								event.getString("end_time"),
						//								event.getString("location"));
					}
					//					try{
					//						eventURI = getContentResolver().insert(DBEventsContentProvider.CONTENT_URI, values);
					//					}
					//					catch(Exception e){
					//						Log.e(TAG, "Exception caught: " + e.getMessage());
					//					}
					//					events.add(newEvent);
				}

				getContentResolver().bulkInsert(DBEventsContentProvider.CONTENT_URI, values);
				// Then post the processed results to the UI thread
				// if we do not do this a runtime exception will be
				// generated e.g. CalledFromWrongThreadException
				// only the original thread that created the thread hierarchy
				// can touch its views.
				//				MainActivity.this.runOnUiThread(new Runnable() {
				//					
				//					@Override
				//					public void run() {
				//						for (FacebookEvent event : events) {
				//							
				//							TextView view = new TextView(getApplicationContext());
				//							view.setText(event.getTitle());
				//							view.setTextSize(16);
				//							
				//							mRootView.addView(view);
				//						}
				//						
				//					}
				//				});
			} catch (JSONException e) {
				Log.e(DefaultView.TAG, "JSON error in response! Message: " + e.getMessage());
			}

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onIOException(java.io.IOException, java.lang.Object)
		 */
		@Override
		public void onIOException(IOException e, Object state) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onFileNotFoundException(java.io.FileNotFoundException, java.lang.Object)
		 */
		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onMalformedURLException(java.net.MalformedURLException, java.lang.Object)
		 */
		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onFacebookError(com.facebook.android.FacebookError, java.lang.Object)
		 */
		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub

		}

	}


}
