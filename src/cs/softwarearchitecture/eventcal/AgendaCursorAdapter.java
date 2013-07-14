/**
 * 
 */
package cs.softwarearchitecture.eventcal;

import java.text.DateFormatSymbols;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cs.softwarearchitecture.eventcal.utility.ColumnNames;

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
		
		if (mCellStates.length < cursor.getCount())
			mCellStates = cursor == null ? null : new int[cursor.getCount()];
		
		// Setting the TextViews
		TextView seperatorDate = (TextView)view.findViewById(R.id.separator);
		TextView eventTitle = (TextView)view.findViewById(R.id.event_title);
		TextView subtitleTime =  (TextView)view.findViewById(R.id.event_subtitle);
		
		eventTitle.setText(cursor.getString(
				cursor.getColumnIndex(ColumnNames.COLUMN_TITLE)));
		
		if (!(cursor.isNull(cursor.getColumnIndex(ColumnNames.COLUMN_END_TIME))) 
				|| cursor.getInt(cursor.getColumnIndex(ColumnNames.COLUMN_END_TIME)) != 0) {
			subtitleTime.setText(timeFormatted(Integer.toString(cursor.getInt(
					cursor.getColumnIndex(ColumnNames.COLUMN_START_TIME)))) + "-" 
					+ timeFormatted(Integer.toString(cursor.getInt(
							cursor.getColumnIndex(ColumnNames.COLUMN_END_TIME)))));
		} 
		else {
			subtitleTime.setText(timeFormatted(Integer.toString(cursor.getInt(
					cursor.getColumnIndex(ColumnNames.COLUMN_START_TIME)))));
		}
		
		// Image for each event type/group
		imageViewSetting(view, cursor);

		/*
         * Date Separator
         */
        seperatorImplementation(cursor, seperatorDate);

	}

	/**
	 * @param cursor
	 * @param seperatorDate
	 */
	protected void seperatorImplementation(Cursor cursor, TextView seperatorDate) {
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
            				cursor.getColumnIndex(ColumnNames.COLUMN_START_DATE));

                    cursor.moveToPosition(position);
                    
                    int nextDate = cursor.getInt(
            				cursor.getColumnIndex(ColumnNames.COLUMN_START_DATE));
                    
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
    				cursor.getColumnIndex(ColumnNames.COLUMN_START_DATE)));
        	
        	String month = new DateFormatSymbols().getMonths()[Integer.parseInt(dateRaw.substring(3,5)) - 1];
        	String day = dateRaw.substring(1, 3);
        	String year = dateRaw.substring(5, dateRaw.length());
        	seperatorDate.setText(day + "-" + month + "-" + year);
           
            seperatorDate.setVisibility(View.VISIBLE);
        } else {
            seperatorDate.setVisibility(View.GONE);
        }
	}

	/**
	 * Image Selection for each event from the res folder
	 * @param view
	 * @param cursor
	 */
	protected void imageViewSetting(View view, Cursor cursor) {
		ImageView eventImage = (ImageView)view.findViewById(R.id.event_image);


		String imageResourceType = cursor.getString(
				cursor.getColumnIndex(ColumnNames.COLUMN_TABLE));

		if (imageResourceType.equals("PERSONAL")) {
			eventImage.setImageResource(R.drawable.ic_action_personal);
		}

		if (imageResourceType.equals("FACEBOOK")) {
			eventImage.setImageResource(R.drawable.ic_action_facebook_event);
		}

		if (imageResourceType.equals("EVENTBRITE")) {
			eventImage.setImageResource(R.drawable.ic_action_eventbrite_event);
		}

		if (imageResourceType.equals("UW")) {
			eventImage.setImageResource(R.drawable.ic_action_uw_event);
		}

		if (imageResourceType.equals("GOOGLE")) {
			eventImage.setImageResource(R.drawable.ic_action_google_event);
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

	/**
	 * Formatted Time String for Share 
	 * @param time
	 * @return time (String)
	 */
	protected static String timeFormatted(String time) {
		Log.d(DefaultView.TAG, "Unformatted String: " + time);

		try{
			time = time.substring(1, 3) + ":" + time.substring(3, 5);
		}
		catch (NullPointerException e){
			Log.e(DefaultView.TAG, "Exception caught: (NullPointer) " + e.getMessage());
		}
		return time;
	}
}
