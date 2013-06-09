package cs.softwarearchitecture.eventcal;

import java.util.Calendar;
import java.util.Locale;

import com.viewpagerindicator.TitlePageIndicator;

import cs.softwarearchitecture.eventcal.R;
import cs.softwarearchitecture.eventcal.SettingsActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
	int  PAGE_NUMBER = 3;
	int CURRENT_POSITION = 1;
	
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
	Calendar calChanging;

	
	private String monthsName[] = {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" 	
	};
	
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
		pageIndicator.setViewPager(dayViewPager,CURRENT_POSITION);
		pageIndicator.setOnPageChangeListener(new OnPageChangeListener(){

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				
				if (state == ViewPager.SCROLL_STATE_IDLE) {	
					if (currentPage < 1){
						updateDate(-1);
					}
					else if (currentPage > 1){
						updateDate(1);
					}

					pageIndicator.setCurrentItem(CURRENT_POSITION, false);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				

			}

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				currentPage = position;
			}

		});
	}

	protected void updateDate(int changedDays) {
		// TODO Auto-generated method stub
		
		// Calculate current date
		calChanging.add(Calendar.DAY_OF_MONTH, changedDays);
		
		currentDay = calChanging.get(Calendar.DATE);
		currentMonth = calChanging.get(Calendar.MONTH);
		currentYear = calChanging.get(Calendar.YEAR);
		
		currentDate = currentDay + " " + monthsName[currentMonth] +
				", " + currentYear;
		
		// Calculate previous date
		calChanging.add(Calendar.DAY_OF_MONTH, -1);
		
		previousDay = calChanging.get(Calendar.DATE);
		previousMonth = calChanging.get(Calendar.MONTH);
		previousYear = calChanging.get(Calendar.YEAR);
		
		previousDate = previousDay + " " + monthsName[previousMonth] +
				", " + previousYear;
		
		// Calculate next date
		calChanging.add(Calendar.DAY_OF_MONTH, +2);
		
		nextDay = calChanging.get(Calendar.DATE);
		nextMonth = calChanging.get(Calendar.MONTH);
		nextYear = calChanging.get(Calendar.YEAR);
		
		nextDate = nextDay + " " + monthsName[nextMonth] + 
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
			return PAGE_NUMBER;
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

		public DayViewFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.dayview, container, false);
			return rootView;
		}
	}
}