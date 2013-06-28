package cs.softwarearchitecture.eventcal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.Facebook.DialogListener;

public class SettingsActivity extends PreferenceActivity {

	// Permissions array
	public static final String[] PERMS = { "user_events" };

	// Handler member variable
	private Handler mHandler = new Handler();

	@SuppressWarnings("deprecation")
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

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
		bindPreferenceSummaryToValue(findPreference("facebook_login"));
		bindPreferenceSummaryToValue(findPreference("eventbrite_login"));
		bindPreferenceSummaryToValue(findPreference("google_login"));

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
			String checkBoxKey = preference.getKey();
			Boolean valueOfCheckBox = (Boolean)value;
			if (checkBoxKey.equals("facebook_login")) {
				if (valueOfCheckBox){
					Log.d(DefaultView.TAG, "Facebook Login requested!");
					LoginDialogListener loginComplete = new LoginDialogListener();
					DefaultView.mFacebook.authorize(this, PERMS, loginComplete);
					Log.d(DefaultView.TAG, "Facebook authorize called!");
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
			} else if (checkBoxKey.equals("eventbrite_login")) {
				Log.d(DefaultView.TAG, "Eventbrite Login clicked!");
			} else if (checkBoxKey.equals("google_login")) {
				Log.d(DefaultView.TAG, "Google Login clicked!");
			}
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
		if (preference instanceof CheckBoxPreference){
			preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToCheckBoxListener);
			sBindPreferenceSummaryToCheckBoxListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getBoolean(preference.getKey(),
									false));
		}
		else {
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
			DefaultView.mEditor = DefaultView.mPreference.edit();
			DefaultView.mEditor.putString("access_token", DefaultView.mFacebook.getAccessToken());
			DefaultView.mEditor.putLong("access_expires", DefaultView.mFacebook.getAccessExpires());
			DefaultView.mEditor.commit();

		}

		/* (non-Javadoc)
		 * @see com.facebook.android.Facebook.DialogListener#onFacebookError(com.facebook.android.FacebookError)
		 */
		@Override
		public void onFacebookError(FacebookError e) {
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


}
