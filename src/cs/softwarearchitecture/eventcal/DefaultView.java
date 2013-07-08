package cs.softwarearchitecture.eventcal;

import java.security.acl.NotOwnerException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SearchView;
import android.widget.SpinnerAdapter;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.model.Event;
import cs.softwarearchitecture.eventcal.modify.AddEvent;
import cs.softwarearchitecture.eventcal.modify.EditEvent;
import cs.softwarearchitecture.eventcal.services.FacebookService;
import cs.softwarearchitecture.eventcal.services.GoogleService;
import cs.softwarearchitecture.eventcal.services.getEventBriteEventService;
import cs.softwarearchitecture.eventcal.services.getUWEventService;
import cs.softwarearchitecture.eventcal.viewpagerindicator.TitlePageIndicator;

public class DefaultView extends FragmentActivity {  

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	CalendarPagerAdapter mCalendarPagerAdapter;

	int currentDay;
	int currentMonth;
	int currentYear;

	int previousDay;
	int previousMonth;
	int previousYear;

	int nextDay;
	int nextMonth;
	int nextYear;

	int currentPage;

	private String mCurrentDate;
	private String mPreviousDate;
	private String mNextDate;


	//Calendar
	public static Calendar mCalendarChanging;

	private static Values values;

	// Content Resolver
	private static ContentResolver mEventContentResolver;

	// Pref. storage
	protected static SharedPreferences mPreference;
	protected static Editor mEditor;

	// Facebook vars
	public static Facebook mFacebook;
	@SuppressWarnings("deprecation")
	public static AsyncFacebookRunner mAsyncRunnner;

	// Notification broadcast
	public AsyncAlarmRunner mAlarmSetup = new AsyncAlarmRunner();

	// Service start
	AsyncServiceRunner mStartServices = new AsyncServiceRunner();

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager dayViewPager;
	TitlePageIndicator pageIndicator;

	// TAG for logCat
	public static String TAG = "EVENT CALENDAR";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_view);

		// Service Start Listener
		mStartServices.execute();

		mEventContentResolver = getContentResolver();

		values = new Values();
		mCalendarChanging = Calendar.getInstance(Locale.getDefault());

		// Notification setup
		mAlarmSetup.execute();

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mCalendarPagerAdapter = new CalendarPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		dayViewPager = (ViewPager) findViewById(R.id.dayViewPager);
		dayViewPager.setAdapter(mCalendarPagerAdapter);
		pageIndicator = 
				(TitlePageIndicator) findViewById(R.id.pageIndicator);
		pageIndicator.setViewPager(dayViewPager,values.getCURRENT_PAGE());
		pageIndicator.setSelectedColor(Color.BLACK);

		// set today's view
		updateDate(0);

		pageIndicator.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int state) {

				if (state == ViewPager.SCROLL_STATE_IDLE) {	
					if (currentPage < 1){
						updateDate(-1);	
					}
					else if (currentPage > 1){
						updateDate(1);
					}
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageSelected(int position) {
				currentPage = position;
			}

		});
	}

	public class AsyncAlarmRunner extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			SharedPreferences settingsPreference = PreferenceManager.getDefaultSharedPreferences(DefaultView.this);

			if(settingsPreference.getBoolean("notifications_new_message", false)) {
				Log.d("NOTIFICATION", "notification enabled");
				
				// Calculate current date
				mCalendarChanging.add(Calendar.DAY_OF_MONTH, 0);

				currentDay = mCalendarChanging.get(Calendar.DATE);
				currentMonth = mCalendarChanging.get(Calendar.MONTH) + 1;
				currentYear = mCalendarChanging.get(Calendar.YEAR);

				String[] dateString = { Integer.toString(CurrentDateTimeConverter.timeDateFormatter(currentDay, currentMonth, Integer.toString(currentYear))) };

				Log.d(TAG, dateString[0] );

				Cursor notifCursor = 
						mEventContentResolver.query(
								DBEventsContentProvider.CONTENT_URI, null, 
								"START_DATE =? AND REMINDER_TIME IS NOT NULL", dateString, null);

				Calendar cal = Calendar.getInstance();
				int notifIterator = 0;

				if (notifCursor.getCount() > 0) {
					while (notifCursor.moveToNext()) {
						Log.d("NOTIFICATION", "EventCount : " + Integer.toString(notifCursor.getCount()));

						int _id = notifCursor.getInt(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_ID));
						String title = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
						String start_time = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_TIME));
						String end_time = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_TIME));
						String start_date = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));
						String location = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_LOCATION));
						String group = notifCursor.getString(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
						int reminder = notifCursor.getInt(notifCursor.getColumnIndex(DBSQLiteHelper.COLUMN_REMINDER_TIME));

						Intent notificationIntent = new Intent(DefaultView.this,
								EventNotificationReceiver.class);
						notificationIntent.putExtra("title", title);
						notificationIntent.putExtra("start_time", start_time);
						notificationIntent.putExtra("end_time", end_time);
						notificationIntent.putExtra("date", start_date);
						notificationIntent.putExtra("reminder", reminder);
						notificationIntent.putExtra("group", group);
						notificationIntent.putExtra("id", _id);

						SharedPreferences preference = getSharedPreferences("setting-pref", Context.MODE_PRIVATE);
						notificationIntent.putExtra("notification_tone", preference.getString("notification_tone", null));

						// As the same intent cancels the previously set alarm having
						// same intent
						// changing the intent for every alarm event so that every alarm
						// gets
						// scheduled properly.
						notificationIntent.setData(Uri.parse("timer:" + _id));

						// notification broadcast call
						PendingIntent sender = PendingIntent.getBroadcast(
								DefaultView.this, 0, notificationIntent,
								Intent.FLAG_GRANT_READ_URI_PERMISSION);

						// getting notification time (Calculation)
						Date notificationTime = new Date();
						notificationTime.setHours(Integer.parseInt(start_time.substring(1,3)));
						notificationTime.setMinutes(Integer.parseInt(start_time.substring(3,5)));
						//					cal.setTimeInMillis((( * 60) +  ((notificationTime.getMinutes() - reminder) * 60)) * 60 * 1000);

						cal.set(Integer.parseInt(start_date.substring(5, 9)), Integer.parseInt(start_date.substring(3, 5)) - 1, 
								Integer.parseInt(start_date.substring(1,3)), notificationTime.getHours(), 
								(notificationTime.getMinutes() - reminder));
						Log.d("NOTIFICATION", "Time set: " + cal.getTime());

						// only broadcast event to come (event which have passed are ignored)
						if(cal.getTimeInMillis() - mCalendarChanging.getTimeInMillis() > 0) {
							Log.d("NOTIFICATION", "Difference in time: " + (cal.getTimeInMillis() - mCalendarChanging.getTimeInMillis()));
							AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
							am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

							// number of notification
							notifIterator++;
						}
					}
				}
			}
			return null;
		}
	}

	private class AsyncServiceRunner extends AsyncTask<Void, Void, Void> {

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			/*
			 *  Facebook service init
			 */
			mPreference = getSharedPreferences("facebook-session", Context.MODE_PRIVATE);

			// Setup Facebook Session
			mFacebook = new Facebook(getString(R.string.app_id));
			mAsyncRunnner = new AsyncFacebookRunner(mFacebook);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected Void doInBackground(Void... params) {
			SharedPreferences settingsPreference = PreferenceManager.getDefaultSharedPreferences(DefaultView.this);
			if(settingsPreference.getBoolean("facebook_login", false)) {
				// Facebook service kickoff
				Log.d(TAG, "Facebook Logged in Kickoff the service...");
				String access_token = mPreference.getString("access_token", null);
				Long expires = mPreference.getLong("access_expires", 0);

				if(access_token != null){
					//mAsyncRunnner.request("me", new IDRequestListener());
					Log.d(TAG, "You are already logged in :)");
					mFacebook.setAccessToken(access_token);
					Intent intent = new Intent(DefaultView.this, FacebookService.class);
					startService(intent);
				}
				if(expires != 0)
					mFacebook.setAccessExpires(expires);
			}

			if(settingsPreference.getBoolean("google_login", false)) {
				// Google service kickoff
				Log.d(TAG, "Google Service kickoff!");
				Intent intent = new Intent(DefaultView.this, GoogleService.class);
				startService(intent);
			}

			if(settingsPreference.getBoolean("eventbrite_login", false)) {
				// Eventbrite service kickoff
				Intent intent = new Intent(DefaultView.this, getEventBriteEventService.class);
				startService(intent);
			}

			// UW service kickoff
			Intent intent = new Intent(DefaultView.this, getUWEventService.class);
			startService(intent);
			return null;
		}

	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume(){
		super.onResume();
		mCalendarPagerAdapter.notifyDataSetChanged();
		mFacebook.extendAccessTokenIfNeeded(this, null);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	protected void updateDate(int changedDays) {

		// Calculate current date
		mCalendarChanging.add(Calendar.DAY_OF_MONTH, changedDays);

		currentDay = mCalendarChanging.get(Calendar.DATE);
		currentMonth = mCalendarChanging.get(Calendar.MONTH);
		currentYear = mCalendarChanging.get(Calendar.YEAR);

		mCurrentDate = currentDay + " " 
				+ values.getMONTH_VALUES()[currentMonth] +
				", " + currentYear;

		Log.v(TAG, "current Date is " + mCurrentDate);

		// Calculate previous date
		mCalendarChanging.add(Calendar.DAY_OF_MONTH, -1);

		previousDay = mCalendarChanging.get(Calendar.DATE);
		previousMonth = mCalendarChanging.get(Calendar.MONTH);
		previousYear = mCalendarChanging.get(Calendar.YEAR);

		mPreviousDate = previousDay + " " 
				+ values.getMONTH_VALUES()[previousMonth] +
				", " + previousYear;

		// Calculate next date
		mCalendarChanging.add(Calendar.DAY_OF_MONTH, +2);

		nextDay = mCalendarChanging.get(Calendar.DATE);
		nextMonth = mCalendarChanging.get(Calendar.MONTH);
		nextYear = mCalendarChanging.get(Calendar.YEAR);

		mNextDate = nextDay + " " 
				+ values.getMONTH_VALUES()[nextMonth] + 
				", " + nextYear;

		// Reset calChanging to current day
		mCalendarChanging.add(Calendar.DAY_OF_MONTH, -1);
		mCalendarPagerAdapter.notifyDataSetChanged();
		pageIndicator.setCurrentItem(values.getCURRENT_PAGE(), false);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_view, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager =
				(SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView =
				(SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(
				searchManager.getSearchableInfo(getComponentName()));

		actionBarViewSelector();
		return true;
	}

	/**
	 * 
	 */
	private void actionBarViewSelector() {
		// View Selection aka Spinner
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(new ContextThemeWrapper(this, android.R.style.Theme_Holo), 
				R.array.view_list, android.R.layout.simple_spinner_dropdown_item);

		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter
			String[] viewList = getResources().getStringArray(R.array.view_list);
			Intent targetIntent = new Intent();

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				switch(position){
				case 0:
					String className = "cs.softwarearchitecture.eventcal.DefaultView$2";
					if(!className.equals(this.getClass().getName())) {
						targetIntent.setClass(getApplicationContext(), DefaultView.class);
						startActivity(targetIntent);
						finish();
					}
					break;
				case 1:
					Log.d(TAG, this.getClass().getName());
					Log.d(TAG, getApplicationContext().getClass().getName());
					targetIntent.setClass(getApplicationContext(), MonthActivity.class);
					startActivity(targetIntent);
					finish();
					break;
				case 2:
					targetIntent.setClass(getApplicationContext(), AgendaActivity.class);
					startActivity(targetIntent);
					finish();
					break;
				}
				return true;
			}
		};

		actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);

	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.action_goto:
			showDialog(R.id.action_goto);
			break;
		case R.id.action_settings:
			Intent settingIntent = new Intent(this, SettingsActivity.class);
			startActivity(settingIntent);
			break;
		case R.id.menu_add:
			Intent addEventIntent = new Intent(this, AddEvent.class);
			addEventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(addEventIntent);
			break;
		case R.id.today:
			mCalendarChanging = Calendar.getInstance(Locale.getDefault());
			updateDate(0);
			break;
		}
		return true;
	}

	@Override
	protected Dialog onCreateDialog(final int iD)
	{
		switch (iD)
		{
		case R.id.action_goto:
			return new DatePickerDialog(this, new OnDateSetListener()
			{
				@Override
				public void onDateSet(DatePicker view, int year,
						int monthOfYear, int dayOfMonth)
				{
					mCalendarChanging.set(year, monthOfYear, dayOfMonth);
					updateDate(0);
				}
			}, mCalendarChanging.get(Calendar.YEAR),
			mCalendarChanging.get(Calendar.MONTH),
			mCalendarChanging.get(Calendar.DAY_OF_MONTH));	
		}
		return null;
	}

	/**
	 * 
	 */
	private void startIntent(String className) {
		String targetClass = className + ".class"; 

	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class CalendarPagerAdapter extends FragmentStatePagerAdapter {

		public CalendarPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DayViewFragment();
			Bundle args = new Bundle();
			args.putInt(DayViewFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);

			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return values.getPAGE_NUMBER();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			switch (position) {
			case 0:
				return mPreviousDate.toUpperCase(l);
			case 1:
				return mCurrentDate.toUpperCase(l); 
			case 2:
				return mNextDate.toUpperCase(l); 
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DayViewFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		private Context mContext;
		RelativeLayout dayEventRelative;

		public DayViewFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			ViewGroup rootView = (ViewGroup) inflater.inflate(
					R.layout.dayview, container, false);

			mContext = getActivity().getApplicationContext();	

			dayEventRelative = 
					(RelativeLayout) rootView.findViewById(R.id.dayEventRelative);

			//Log.v(TAG, "onCreateView");

			loadDataForDay();

			return rootView;
		}

		private void loadDataForDay(){

			// To load events data from database:

			ArrayList<Event> events = getCurrentDayEvents();

			Log.d(TAG,"events length = " + events.size());

			for (Event event : events){
				Log.d(TAG,"event start time: " + 
						event.getStartTime() + 
						" end time: " + event.getEndTime());
				for (int i = 0; i < values.getTIME_VALUES().length; i++){
					String start_time = event.getStartTime();

					if ((start_time.contains(values.getTIME_VALUES()[i])))
						createViewForEvent(event);
				}
			}
		}

		/**
		 * @return Event List
		 */
		protected static ArrayList<Event> getCurrentDayEvents() {
			//Log.d(TAG, "Day of the Month: " + calChanging);

			ArrayList<Event> eventList = new ArrayList<Event>(); 
			int currentDay = mCalendarChanging.get(Calendar.DATE);
			int currentMonth = mCalendarChanging.get(Calendar.MONTH) + 1;
			int currentYear = mCalendarChanging.get(Calendar.YEAR);

			String[] dateString = { Integer.toString(CurrentDateTimeConverter.timeDateFormatter(currentDay, currentMonth, Integer.toString(currentYear))) };

			Log.v(TAG, dateString[0] );

			Cursor cursor = 
					mEventContentResolver.query(
							DBEventsContentProvider.CONTENT_URI, null, 
							"START_DATE =? AND END_TIME NOT NULL", dateString, 
							DBSQLiteHelper.COLUMN_START_TIME + " ASC");
			
			//Log.v(TAG, "loading events");
			if (cursor.moveToFirst()) {
				while(!cursor.isAfterLast()){
					String type = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
					Log.d(TAG, "loading events " + type);

					int _id = cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_ID));
					String title = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
					String start_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_TIME));
					String end_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_TIME));
					String start_date = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));
					String location = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_LOCATION));
					String group = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
					int reminder = cursor.getInt(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_REMINDER_TIME));

					Log.v(TAG, "before start_time is " +  start_time);
					Log.v(TAG, "before end_time is " + end_time);

					// remove "1" at the beginning
					start_time = start_time.substring(1);
					end_time = end_time.substring(1);

					Log.v(TAG, "start_time is " +  start_time);
					Log.v(TAG, "end_time is " + end_time);

					Event event = 
							new Event(type, 
									title, 
									start_time, 
									end_time, 
									start_date, 
									location, 
									group, 
									reminder, 
									_id);
					eventList.add(event);
					cursor.moveToNext();
				}
			}
			cursor.close();
			return eventList;
		}

		private void createViewForEvent (Event event) {
			final String start_time = event.getStartTime();
			final String end_time = event.getEndTime();
			final String title 	= event.getTitle();
			final String date = event.getDate();
			final String group = event.getGroup();
			final int reminder = event.getReminder();
			final int _id = event.getID();

			int marginTop = calculateMargin(start_time);
			int height = (int) calculateDiffInTime(start_time, end_time);
			height = (int) (1.375 * height);

			LayoutParams lprams = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					height);

			int marginLeft = 75;
			lprams.setMargins(marginLeft, 0, 0, 0);
			lprams.topMargin = marginTop;

			Button button = new Button(mContext);
			button.setId(_id);

			String type = event.getType();

			if (type.equals("PERSONAL")) {
				button.setBackgroundColor(0xa0db8e00);
			}

			if (type.equals("FACEBOOK")) {
				button.setBackgroundColor(Color.CYAN);
			}

			if (type.equals("EVENTBRITE")) {
				button.setBackgroundColor(0xfbf2a300);
			}

			if (type.equals("UW")) {
				button.setBackgroundColor(0xdee4fa00);
			}

			if (type.equals("GOOGLE")) {
				button.setBackgroundColor(Color.YELLOW);
			}

			button.setLayoutParams(lprams);
			button.setTextColor(Color.BLACK);
			button.setTextAppearance(mContext, R.style.ButtonFontStyle);
			button.setText(title);
			button.setTextSize(15);

			if (height <= 18) {
				button.setSingleLine();
			}

			button.setEllipsize(TruncateAt.END);
			dayEventRelative.addView(button);
			button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent editEventIntent = 
							new Intent(getActivity(), EditEvent.class);

					editEventIntent.putExtra("title", title);
					editEventIntent.putExtra("start_time", start_time);
					editEventIntent.putExtra("end_time", end_time);
					editEventIntent.putExtra("date", date);
					editEventIntent.putExtra("reminder", reminder);
					editEventIntent.putExtra("group", group);
					editEventIntent.putExtra("id", _id);

					Log.d(TAG, "Event start_time: " + start_time);

					editEventIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(editEventIntent);
				}
			});
		}

		private long calculateDiffInTime(String start_time, String end_time) {
			String startTimeEvent = start_time;
			String endTimeEvent = end_time;

			Log.v(TAG, "startTime = " + start_time);
			Log.v(TAG, "endTime = " + end_time);

			long diffMinutes = 0;
			SimpleDateFormat format = new SimpleDateFormat("HHmmss");
			Date d1 = null;
			Date d2 = null;
			try {
				d1 = format.parse(startTimeEvent);
				d2 = format.parse(endTimeEvent);

				long diff = d2.getTime() - d1.getTime();
				diffMinutes = diff / (60 * 1000);

				Log.v(TAG, "diffMinutes = " + diffMinutes);


			} catch (ParseException e) {
				e.printStackTrace();
			}
			return diffMinutes;
		}


		private int calculateMargin(String start_time) {
			// TODO Auto-generated method stub
			double margin = 3;
			for (int i = 0; start_time.compareToIgnoreCase(
					values.getTIME_VALUES()[i]) != 0; i++) {
				margin = margin + 1.334;
			}

			return (int) margin;
		}

	}
}
