package cs.softwarearchitecture.eventcal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.OnDateSelectedListener;
import com.squareup.timessquare.CalendarPickerView.SelectionMode;

import cs.softwarearchitecture.eventcal.model.Event;
import cs.softwarearchitecture.eventcal.modify.AddEvent;

public class MonthActivity extends DefaultView {

	private CalendarPickerView calendar;

	public static int MONTH_VIEW = 1;
	ListView eventListView;
	EventListAdapter eventListAdapter;

	// Content Resolver
	private static ContentResolver mEventContentResolver;
	//private Event[] eventList;


	final static String TAG = "MonthActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month);

		mEventContentResolver = getContentResolver();

		final Calendar nextYear = Calendar.getInstance();
		nextYear.add(Calendar.YEAR, 1);

		final Calendar lastYear = Calendar.getInstance();
		lastYear.add(Calendar.YEAR, -1);


		calendar = (CalendarPickerView) findViewById(R.id.monthview);
		calendar.init(lastYear.getTime(), nextYear.getTime())
		.inMode(SelectionMode.SINGLE)
		.withSelectedDate(new Date());

		calendar.setOnDateSelectedListener(new onDateSelect());

		//eventList = getCurrentDayEvents();

		eventListAdapter = new EventListAdapter(this);
		eventListView = (ListView) findViewById(R.id.eventList);
		eventListView.setAdapter(eventListAdapter);
	}

	public class onDateSelect implements OnDateSelectedListener{

		@Override
		public void onDateSelected(Date date) {
			// TODO Auto-generated method stub
			mCalendarChanging.setTime(date);
			eventListAdapter.notifyDataSetChanged();

			Log.v(TAG, "in onDateSelected Listener");
		}
	}

	/* (non-Javadoc)
	 * @see cs.softwarearchitecture.eventcal.DefaultView#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.default_view, menu);
		actionBarViewSelector();
		getActionBar().setSelectedNavigationItem(MONTH_VIEW);
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

			/* (non-Javadoc)
			 * @see android.app.ActionBar.OnNavigationListener#onNavigationItemSelected(int, long)
			 */
			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				switch(position){
				case 0:
					targetIntent.setClass(getApplicationContext(), DefaultView.class);
					startActivity(targetIntent);
					finish();
					break;
				case 1:
					String className = "cs.softwarearchitecture.eventcal.MonthActivity$1";
					if(!className.equals(this.getClass().getName())) {
						targetIntent.setClass(getApplicationContext(), MonthActivity.class);
						startActivity(targetIntent);
						finish();
					}
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
	public boolean onOptionsItemSelected(MenuItem item){

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
			calendar.selectDate(new Date());
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
					calendar.selectDate(mCalendarChanging.getTime());
				}
			}, mCalendarChanging.get(Calendar.YEAR),
			mCalendarChanging.get(Calendar.MONTH),
			mCalendarChanging.get(Calendar.DAY_OF_MONTH));	
		}

		return null;
	}

	// List fragment adapter
	public class EventListAdapter extends ArrayAdapter<Event> {
		private final Context context;
		private ArrayList<Event> events;

		public EventListAdapter(Context context){
			super(context, R.layout.rowview);
			this.context = context;
			Log.v(TAG, "in adpter constructor");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			//ArrayList<Event> eventList = getCurrentDayEvents();
			Log.v(TAG, "in getView of list adapter");

			View rowView = inflater.inflate(R.layout.rowview, parent, false);
			TextView textView = (TextView) rowView.findViewById(R.id.eventname);


			if (events.size()>0){
				textView.setText(((Event) events.get(position)).getTitle());
			}
			return rowView;
		}

		@Override
		public int getCount(){
			events = DayViewFragment.getCurrentDayEvents();
			Log.v(TAG, "list adapter getCount: " + events.size());
			return events.size();
		}
	} 
}
