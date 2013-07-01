/**
 * 
 */
package cs.softwarearchitecture.eventcal;

import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author nitishagarwal
 *
 */
public class AgendaCursorAdapter extends CursorAdapter {

	private LayoutInflater mLayoutInflater;
    private Context mContext;
    
    public AgendaCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
    }
    
	/* (non-Javadoc)
	 * @see android.support.v4.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView event_title = (TextView)view.findViewById(R.id.event_title);
		ImageView event_image = (ImageView)view.findViewById(R.id.list_image);
		event_title.setText(cursor.getString(
				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE)));
		
		String image_resource_type = cursor.getString(
				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
		
		if (image_resource_type.equals("PERSONAL")) {
			// TODO create a personal image
			event_image.setImageResource(R.drawable.ic_action_personal);
		}
		
		if (image_resource_type.equals("FACEBOOK")) {
			event_image.setImageResource(R.drawable.ic_action_facebook_event);
		}
		
		if (image_resource_type.equals("EVENTBRITE")) {
			event_image.setImageResource(R.drawable.ic_action_eventbrite_event);
		}
		
		if (image_resource_type.equals("UW")) {
			event_image.setImageResource(R.drawable.ic_action_uw_event);
		}
		
		if (image_resource_type.equals("GOOGLE")) {
			event_image.setImageResource(R.drawable.ic_action_google_event);
		}
	}

	/* (non-Javadoc)
	 * @see android.support.v4.widget.CursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
	 */
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.agenda_item, viewGroup, false);
		bindView(view, context, cursor);
		return view;
	}

}
