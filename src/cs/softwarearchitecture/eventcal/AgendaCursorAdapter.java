/**
 * 
 */
package cs.softwarearchitecture.eventcal;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cs.softwarearchitecture.eventcal.database.DBSQLiteHelper;

/**
 * @author nitishagarwal
 *
 */
public class AgendaCursorAdapter extends CursorAdapter {

	private LayoutInflater mLayoutInflater;
    private Context mContext;
    
    private int[] mCellStates;

    /**
     * State of ListView item that has never been determined.
     */
    private static final int STATE_UNKNOWN = 0;

    /**
     * State of a ListView item that is sectioned. A sectioned item must
     * display the separator.
     */
    private static final int STATE_SECTIONED_CELL = 1;

    /**
     * State of a ListView item that is not sectioned and therefore does not
     * display the separator.
     */
    private static final int STATE_REGULAR_CELL = 2;
    
    public AgendaCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context); 
        mCellStates = cursor == null ? null : new int[cursor.getCount()];
    }
    
	/* (non-Javadoc)
	 * @see android.support.v4.widget.CursorAdapter#changeCursor(android.database.Cursor)
	 */
	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		mCellStates = cursor == null ? null : new int[cursor.getCount()];
	}

	/* (non-Javadoc)
	 * @see android.support.v4.widget.CursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
	 */
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		TextView seperator_date = (TextView)view.findViewById(R.id.separator);
		TextView event_title = (TextView)view.findViewById(R.id.event_title);
		
//		ImageView event_image = (ImageView)view.findViewById(R.id.list_image);
//		
		event_title.setText(cursor.getString(
				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TITLE)));
//		
//		String image_resource_type = cursor.getString(
//				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_TABLE));
//		
//		if (image_resource_type.equals("PERSONAL")) {
//			event_image.setImageResource(R.drawable.ic_action_personal);
//		}
//		
//		if (image_resource_type.equals("FACEBOOK")) {
//			event_image.setImageResource(R.drawable.ic_action_facebook_event);
//		}
//		
//		if (image_resource_type.equals("EVENTBRITE")) {
//			event_image.setImageResource(R.drawable.ic_action_eventbrite_event);
//		}
//		
//		if (image_resource_type.equals("UW")) {
//			event_image.setImageResource(R.drawable.ic_action_uw_event);
//		}
//		
//		if (image_resource_type.equals("GOOGLE")) {
//			event_image.setImageResource(R.drawable.ic_action_google_event);
//		}
//		
		/*
         * Separator
         */
        boolean needSeparator = false;

        final int position = cursor.getPosition();

        switch (mCellStates[position]) {
            case STATE_SECTIONED_CELL:
                needSeparator = true;
                break;

            case STATE_REGULAR_CELL:
                needSeparator = false;
                break;

            case STATE_UNKNOWN:
            default:
                // A separator is needed if it's the first itemview of the
                // ListView or if the group of the current cell is different
                // from the previous itemview.
                if (position == 0) {
                    needSeparator = true;
                } else {
                    cursor.moveToPosition(position - 1);

                    int prevDate = cursor.getInt(
            				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));

                    cursor.moveToPosition(position);
                    
                    int nextDate = cursor.getInt(
            				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE));
                    
                    if (nextDate != prevDate) {
                        needSeparator = true;
                    }

                }

                // Cache the result
                mCellStates[position] = needSeparator ? STATE_SECTIONED_CELL : STATE_REGULAR_CELL;
                break;
        }

        if (needSeparator) {
        	String dateRaw = Integer.toString(cursor.getInt(
    				cursor.getColumnIndex(DBSQLiteHelper.COLUMN_START_DATE)));
        	
        	seperator_date.setText(dateRaw.substring(1, 3) + "-" + dateRaw.substring(3,5) + "-"
        			+ dateRaw.substring(5, dateRaw.length()));
           
            seperator_date.setVisibility(View.VISIBLE);
        } else {
            seperator_date.setVisibility(View.GONE);
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
