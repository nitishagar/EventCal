package cs.softwarearchitecture.eventcal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import cs.softwarearchitecture.eventcal.contentprovider.DBEventsContentProvider;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import cs.softwarearchitecture.eventcal.model.Event;
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

	private String currentDate;
	private String previousDate;
	private String nextDate;


	//Calendar calendar; 
	static Calendar calChanging;

	private static Values values;
	
	// Content Resolver
	private static ContentResolver mEventContentResolver;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager dayViewPager;

	// TAG for logCat
	public static String TAG = "EVENT CALENDAR";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_default_view);
		
		mEventContentResolver = getContentResolver();

		values = new Values();
		//calendar = Calendar.getInstance(Locale.getDefault());
		calChanging = Calendar.getInstance(Locale.getDefault());
		updateDate(0);



		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mCalendarPagerAdapter = new CalendarPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		dayViewPager = (ViewPager) findViewById(R.id.dayViewPager);
		dayViewPager.setAdapter(mCalendarPagerAdapter);

		final TitlePageIndicator pageIndicator = 
				(TitlePageIndicator) findViewById(R.id.pageIndicator);
		pageIndicator.setViewPager(dayViewPager,values.getCURRENT_PAGE());
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
					
					pageIndicator.setCurrentItem(values.getCURRENT_PAGE(), false);
					mCalendarPagerAdapter.notifyDataSetChanged();
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

		currentDate = currentDay + " " 
				+ values.getMONTH_VALUES()[currentMonth] +
				", " + currentYear;

		// Calculate previous date
		calChanging.add(Calendar.DAY_OF_MONTH, -1);

		previousDay = calChanging.get(Calendar.DATE);
		previousMonth = calChanging.get(Calendar.MONTH);
		previousYear = calChanging.get(Calendar.YEAR);

		previousDate = previousDay + " " 
				+ values.getMONTH_VALUES()[previousMonth] +
				", " + previousYear;

		// Calculate next date
		calChanging.add(Calendar.DAY_OF_MONTH, +2);

		nextDay = calChanging.get(Calendar.DATE);
		nextMonth = calChanging.get(Calendar.MONTH);
		nextYear = calChanging.get(Calendar.YEAR);

		nextDate = nextDay + " " 
				+ values.getMONTH_VALUES()[nextMonth] + 
				", " + nextYear;

		// Reset calChanging to current day
		calChanging.add(Calendar.DAY_OF_MONTH, -1);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_view, menu);
		return true;
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
			Intent gotoIntent = new Intent(this, GotoActivity.class);
			startActivity(gotoIntent);
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
		}
		return true;
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
				return previousDate.toUpperCase(l);
			case 1:
				return currentDate.toUpperCase(l); 
			case 2:
				return nextDate.toUpperCase(l); 
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

			String[] dateString = { Integer.toString(timeDateFormatter(currentDay, currentMonth, Integer.toString(currentYear))) };
					
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
					String _id = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_ID));
                    String title = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE));
					String start_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_TIME));
					String end_time = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_END_TIME));
					String location = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_LOCATION));
					String group = cursor.getString(cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
					
					Log.v(TAG, "before start_time is " +  start_time);
					Log.v(TAG, "before end_time is " + end_time);
				
					// remove "1" at the beginning
					start_time = start_time.substring(1);
					end_time = end_time.substring(1);
					
					Log.v(TAG, "start_time is " +  start_time);
					Log.v(TAG, "end_time is " + end_time);
					
					Event event = 
							new Event(title, start_time, end_time, location, group, _id);
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
			final int _id = Integer.parseInt(event.getID());
			
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
		
		private int timeDateFormatter(int firstVal, int secondVal, String thirdVal) {
			int formattedValue = 0;
			String firstString = null;
			String secondString = null;
			
			if (firstVal < 10)
				firstString = "0" + Integer.toString(firstVal);
			else
				firstString = Integer.toString(firstVal);
			
			if (secondVal < 10)
				secondString = "0" + Integer.toString(secondVal);
			else
				secondString = Integer.toString(secondVal);
		
			formattedValue = Integer.parseInt("1" + firstString + secondString 
					+ thirdVal);
			
			return formattedValue;
		}

	}
}