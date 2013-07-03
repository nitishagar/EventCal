package cs.softwarearchitecture.eventcal;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class MonthActivity extends DefaultView {

	public static int MONTH_VIEW = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month);
	}

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

}
