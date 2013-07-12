package cs.softwarearchitecture.eventcal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{

	// Permissions array
	public static final String[] PERMS = { "user_events", "offline_access" };

	// Handler member variable
	private Handler mHandler = new Handler();

	SharedPreferences mSettings;
	
	// Pref. storage
	protected static SharedPreferences mSettingPreference;
	protected static Editor mSettingEditor;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);
		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mSettings.registerOnSharedPreferenceChangeListener(this);
		
		mSettingPreference = getSharedPreferences("setting-pref", 0);

		// Add 'notifications' preferences, and a corresponding header.
		PreferenceCategory preferenceHeader = new PreferenceCategory(this);
		preferenceHeader.setTitle(R.string.pref_header_notifications);
		getPreferenceScreen().addPreference(preferenceHeader);
		addPreferencesFromResource(R.xml.pref_notification);

		// Add 'data and sync' preferences, and a corresponding header.
		preferenceHeader = new PreferenceCategory(this);
		preferenceHeader.setTitle(R.string.pref_header_data_sync);
		getPreferenceScreen().addPreference(preferenceHeader);
		addPreferencesFromResource(R.xml.pref_data_sync);
		
		// Bind the summaries of List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.

		bindPreferenceSummaryToValue(findPreference("notifications_new_event_ringtone"));
		bindPreferenceSummaryToValue(findPreference("sync_frequency"));

	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
				.setSummary(index >= 0 ? listPreference.getEntries()[index]
						: null);

			} else if (preference instanceof RingtonePreference) {
				// For ringtone preferences, look up the correct display value
				// using RingtoneManager.
				if (TextUtils.isEmpty(stringValue)) {
					// Empty values correspond to 'silent' (no ringtone).
					preference.setSummary(R.string.pref_ringtone_silent);
					
				} else {
					Ringtone ringtone = RingtoneManager.getRingtone(
							preference.getContext(), Uri.parse(stringValue));

					if (ringtone == null) {
						// Clear the summary if there was a lookup error.
						preference.setSummary(null);
					} else {
						// Set the summary to reflect the new ringtone display
						// name.
						String name = ringtone
								.getTitle(preference.getContext());
						preference.setSummary(name);
						
						mSettingEditor = mSettingPreference.edit();
						mSettingEditor.putString("notification_tone", stringValue);
						mSettingEditor.commit();
					}
				}

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			return true;
		}
	};

	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToCheckBoxListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
		.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
								""));
	}

	/**
	 * @author nitishagarwal
	 *
	 */
	private class LoginDialogListener implements DialogListener {

		/* (non-Javadoc)
		 * @see com.facebook.android.Facebook.DialogListener#onComplete(android.os.Bundle)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void onComplete(Bundle values) {
			Log.d(DefaultView.TAG, "Facebook Login successful!");
			//			mText.setText("Facebook Login successful. Press Menu...");
			SharedPreferences preference = getSharedPreferences("facebook-session", 0);
			DefaultView.mEditor = preference.edit();
			DefaultView.mEditor.putString("access_token", DefaultView.mFacebook.getAccessToken());
			DefaultView.mEditor.putLong("access_expires", DefaultView.mFacebook.getAccessExpires());
			DefaultView.mEditor.commit();

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.Facebook.DialogListener#onFacebookError(com.facebook.android.FacebookError)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void onFacebookError(FacebookError e) {
			CheckBoxPreference loginCheck = (CheckBoxPreference) findPreference("facebook_login");
    		loginCheck.setChecked(false);
			Log.e(DefaultView.TAG, "Facebook Login error! Message: " + e.getMessage());
		}

		/* (non-Javadoc)
		 * @see com.facebook.android.Facebook.DialogListener#onError(com.facebook.android.DialogError)
		 */
		@Override
		public void onError(DialogError e) {
			Log.e(DefaultView.TAG, "Facebook onError called. Message: " + e.getMessage());

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.Facebook.DialogListener#onCancel()
		 */
		@Override
		public void onCancel() {
			Log.e(DefaultView.TAG, "onCancel called check message");
		}

	}

	/**
	 * @author nitishagarwal
	 *
	 */
	@SuppressWarnings("deprecation")
	private class LogoutRequestListener implements RequestListener {

		/* (non-Javadoc)
		 * @see com.facebook.android.AsyncFacebookRunner.RequestListener#onComplete(java.lang.String, java.lang.Object)
		 */
		@SuppressWarnings("deprecation")
		@Override
		public void onComplete(String response, Object state) {
			//			mHandler.post(new Runnable() {
			//
			//				@Override
			//				public void run() {
			//					//					mText.setText("Logged out!");
			//					Log.d(TAG, "Logged out!");
			//				}
			//			});

			//			mPreference.clear(this); // clear token and expire value
			//			com.facebook.android.Util.clearCookies(this);

			//            mFacebook.setAccessToken(null);
			//            mFacebook.setAccessExpires(0);

			//            Editor editor = mPreference.edit();
			//            editor.remove("access_token");
			//            editor.remove("access_expires");
			//            editor.clear();
			//            editor.commit();
			Log.d(DefaultView.TAG, "Logged out!");
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

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		DefaultView.mFacebook.authorizeCallback(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String value) {
		if (sharedPreferences.getBoolean("facebook_login", false)) {
			if(!(DefaultView.mFacebook.isSessionValid())){
				int FORCE_DIALOG_AUTH =-1;
				Log.d(DefaultView.TAG, "Facebook Login requested!");
				LoginDialogListener loginComplete = new LoginDialogListener();
				DefaultView.mFacebook.authorize(this, PERMS, loginComplete);
				Log.d(DefaultView.TAG, "Facebook authorize called!");
			}
		}
		else {
			if(DefaultView.mFacebook.isSessionValid()){
				Log.d(DefaultView.TAG, "Logging out...");
				try{
					DefaultView.mAsyncRunnner.logout(this, new LogoutRequestListener());
				}
				catch (Exception e){
					Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
				}
			}
		} 
		
		if (sharedPreferences.getBoolean("eventbrite_login", false)) {
			if(mSettingPreference.getString("user_id", null) == null) {
				Log.d(DefaultView.TAG, "Eventbrite Login clicked!");
				
				// Creating a login Id dialog
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.eventbrite_user_id);
				
				// Get the layout inflater
			    LayoutInflater inflater = this.getLayoutInflater();

			    // Inflate and set the layout for the dialog
			    // Pass null as the parent view because its going in the dialog layout
			    final View dialogView = inflater.inflate(R.layout.user_id_dialog, null);
			    
			    builder.setView(dialogView)
			    // Add the buttons
			    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		// User clicked OK button
			    		EditText userID = (EditText) dialogView.findViewById(R.id.userid);

			    		try{
			    			Log.d(DefaultView.TAG, "USER ID: " + userID.getText().toString());
				    		// Also check the value is correct or not
			    			
			    			
			    			// Store it in shared preferences
			    			mSettingEditor = mSettingPreference.edit();
			    			mSettingEditor.putString("user_id", userID.getText().toString());
			    			mSettingEditor.commit();
			    		}
			    		catch (Exception e){
			    			Log.e(DefaultView.TAG, "Exception caught: " + e.getMessage());
			    		}
			    	}
			    })
			    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			    	public void onClick(DialogInterface dialog, int id) {
			    		CheckBoxPreference loginCheck = (CheckBoxPreference) findPreference("eventbrite_login");
			    		loginCheck.setChecked(false);
			    		
			    		// close the dialog box
			    		dialog.cancel();
			    	}
			    });
				
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				
				// Show the Dialog
				dialog.show();
			}
		} 
		else{
			Log.d(DefaultView.TAG, "Deleting user ID for Eventbrite!");
			mSettingEditor = mSettingPreference.edit();
			mSettingEditor.remove("user_id");
			mSettingEditor.commit();
		}
		
		if (sharedPreferences.getBoolean("google_login", false)) {
			Log.d(DefaultView.TAG, "Google Login clicked!");	
		}
		
		if (sharedPreferences.getBoolean("uw_login", false)) {
			Log.d(DefaultView.TAG, "UW Login clicked!");	
		}
		
	}


}
