/**
 * 
 */
package cs.softwarearchitecture.eventcal;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import cs.softwarearchitecture.eventcal.modify.EditEvent;

/**
 * @author nitishagarwal
 *
 */
public class EventNotificationReceiver extends BroadcastReceiver {

	/* (non-Javadoc)
	 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
        if (extras != null) {
            String title = extras.getString("title");
            String start_time = extras.getString("start_time");
			String end_time = extras.getString("end_time");
			String date = extras.getString("date");
			String group = extras.getString("group");
			int reminder = extras.getInt("reminder");
			int _id = extras.getInt("id");
            
            // Event details tab intent
            Intent editEventIntent = 
					new Intent(context, EditEvent.class);
			
			editEventIntent.putExtra("title", title);
			editEventIntent.putExtra("start_time", start_time);
			editEventIntent.putExtra("end_time", end_time);
			editEventIntent.putExtra("date", date);
			editEventIntent.putExtra("reminder", reminder);
			editEventIntent.putExtra("group", group);
			editEventIntent.putExtra("id", _id);
			
            // show the notification now
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification mNotification = new Notification(R.drawable.ic_launcher, context.getString(R.string.app_name), System.currentTimeMillis());
            PendingIntent pi = PendingIntent.getActivity(context, 0, editEventIntent, 0); // open MainActivity if the user selects this notification
            mNotification.setLatestEventInfo(context, context.getString(R.string.app_name), title, pi);
            mNotification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_SOUND;
            mNotificationManager.notify(1, mNotification);
        }
	}

}
