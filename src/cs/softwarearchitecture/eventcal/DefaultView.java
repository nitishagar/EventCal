package cs.softwarearchitecture.eventcal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
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
import android.widget.SpinnerAdapter;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.model.Event;
import cs.softwarearchitecture.eventcal.modify.AddEvent;
import cs.softwarearchitecture.eventcal.modify.EditEvent;
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


	//Calendar calendar; 
	public static Calendar calChanging;

	private static Values values;
	
	// Content Resolver
	private static ContentResolver mEventContentResolver;

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
		
		mEventContentResolver = getContentResolver();

		values = new Values();
		calChanging = Calendar.getInstance(Locale.getDefault());

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
	
	@Override
	protected void onResume(){
		super.onResume();
		mCalendarPagerAdapter.notifyDataSetChanged();
	}

	protected void updateDate(int changedDays) {

		// Calculate current date
		calChanging.add(Calendar.DAY_OF_MONTH, changedDays);

		currentDay = calChanging.get(Calendar.DATE);
		currentMonth = calChanging.get(Calendar.MONTH);
		currentYear = calChanging.get(Calendar.YEAR);

		mCurrentDate = currentDay + " " 
				+ values.getMONTH_VALUES()[currentMonth] +
				", " + currentYear;
		
		Log.v(TAG, "current Date is " + mCurrentDate);

		// Calculate previous date
		calChanging.add(Calendar.DAY_OF_MONTH, -1);

		previousDay = calChanging.get(Calendar.DATE);
		previousMonth = calChanging.get(Calendar.MONTH);
		previousYear = calChanging.get(Calendar.YEAR);

		mPreviousDate = previousDay + " " 
				+ values.getMONTH_VALUES()[previousMonth] +
				", " + previousYear;

		// Calculate next date
		calChanging.add(Calendar.DAY_OF_MONTH, +2);

		nextDay = calChanging.get(Calendar.DATE);
		nextMonth = calChanging.get(Calendar.MONTH);
		nextYear = calChanging.get(Calendar.YEAR);

		mNextDate = nextDay + " " 
				+ values.getMONTH_VALUES()[nextMonth] + 
				", " + nextYear;

		// Reset calChanging to current day
		calChanging.add(Calendar.DAY_OF_MONTH, -1);
		mCalendarPagerAdapter.notifyDataSetChanged();
		pageIndicator.setCurrentItem(values.getCURRENT_PAGE(), false);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_view, menu);
		
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
		case R.id.action_search:
			Intent searchIntent = new Intent(this, SearchActivity.class);
			searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(searchIntent);
			break;
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
			calChanging = Calendar.getInstance(Locale.getDefault());
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
					calChanging.set(year, monthOfYear, dayOfMonth);
					updateDate(0);
				}
			}, calChanging.get(Calendar.YEAR),
			calChanging.get(Calendar.MONTH),
			calChanging.get(Calendar.DAY_OF_MONTH));	
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

		private ArrayList<Event> getCurrentDayEvents() {
			//Log.d(TAG, "Day of the Month: " + calChanging);
			
			ArrayList<Event> eventList = new ArrayList<Event>(); 
			int currentDay = calChanging.get(Calendar.DATE);
			int currentMonth = calChanging.get(Calendar.MONTH);
			int currentYear = calChanging.get(Calendar.YEAR);

			String[] dateString = { Integer.toString(CurrentDateTimeConverter.timeDateFormatter(currentDay, currentMonth, Integer.toString(currentYear))) };
					
			Log.v(TAG, dateString[0] );
			
			Cursor cursor = 
					mEventContentResolver.query(
							DBEventsContentProvider.CONTENT_URI, null, 
							"START_DATE =?", dateString, null);
			
			//Log.v(TAG, "loading events");
			if (cursor.moveToFirst()) {
					Log.v(TAG, "loading events");
				while(!cursor.isAfterLast()){
					//Log.v(TAG, "loading events");
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
							new Event(title, start_time, end_time, start_date, location, group, reminder, _id);
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
			button.setBackgroundResource(R.drawable.appointment_new);
			button.setLayoutParams(lprams);
			button.setTextColor(Color.BLACK);
			button.setTextAppearance(mContext, R.style.ButtonFontStyle);
			button.setText(title);
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