package cs.softwarearchitecture.eventcal;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ShareActionProvider;

public class EditEvent extends Activity {

	// Share event provider
	private ShareActionProvider mShareActionProvider;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		
		setContentView(R.layout.activity_edit_event);
		
		// Back button functionality enabled
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_event, menu);

		// Locate MenuItem with ShareActionProvider
	    MenuItem item = menu.findItem(R.id.menu_item_share);

	    // Fetch and store ShareActionProvider
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();

	    // Get the default share intent
	    mShareActionProvider.setShareIntent(createShareIntent());
	    
		return true;
	}
	
	private Intent createShareIntent() {
		
		Bundle extras = getIntent().getExtras();
		String title = extras.getString("title");
		String start_time = timeFormatted(extras.getString("start_time"));
		String end_time = timeFormatted(extras.getString("end_time"));
		
		String shareEventDetails = title + " @ " + start_time + " to " + end_time;
		
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareEventDetails);
		
		return shareIntent;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.delete_event:
			break;
		case android.R.id.home:
			super.onBackPressed();
			break;
		}
		return true;
	}

	
	private String timeFormatted(String time) {
		Log.d(DefaultView.TAG, "Unformatted String: " + time);
		
		time = time.substring(0, 2) + ":" + time.substring(2, 4);
		return time;
	}
}
