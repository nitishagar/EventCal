/**
 * 
 */
package cs.softwarearchitecture.eventcal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.util.Log;

/**
 * @author nitishagarwal
 *
 */
public class CurrentDateTimeConverter {

	private Date mDateTime;
	private int mDate;
	private int mTime;
	
	/**
	 * Constructor takes in input date time in String (UTC time as input)
	 * @param inputDateTime
	 */
	@SuppressLint("SimpleDateFormat")
	@SuppressWarnings("deprecation")
	public CurrentDateTimeConverter(String inputDateTime) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		boolean conversionSuccessOrNot = false;
		try {
			// Converting the current string to date format
			mDateTime = sdf.parse(inputDateTime); 
			conversionSuccessOrNot = true;
		} catch (ParseException e) {
			Log.e(DefaultView.TAG, "ERROR parsing time, Exception caught: " + e.getMessage());
		} 
		
		if(conversionSuccessOrNot){
			// Getting current zone info
			Calendar calendar = Calendar.getInstance();
			Log.d(DefaultView.TAG, "current: "+calendar.getTime());
			TimeZone currentZone = calendar.getTimeZone();

			// Converting to current timezone
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf2.setTimeZone(currentZone);

			String currentDate = sdf2.format(mDateTime);
			Log.d(DefaultView.TAG, "Current date: " + currentDate);

			try {
				mDateTime = sdf2.parse(currentDate);
			} catch (ParseException e) {
				Log.e(DefaultView.TAG, "ERROR parsing time, Exception caught: " + e.getMessage());
			}

			// Formatting the int value for both
			mDate = timeDateFormatter(mDateTime.getDate(), mDateTime.getMonth() + 1, 
					Integer.toString(mDateTime.getYear() + 1900));

			mTime = timeDateFormatter(mDateTime.getHours(), mDateTime.getMinutes(), "00");
			Log.d(DefaultView.TAG, "Date: " + Integer.toString(mDate) + " Time: " + Integer.toString(mTime));
		}
	}
	
	/**
	 * Takes in the current date in day, month, year format or time in hr, min, sec format
	 * and returns the formatted time for database
	 * @param firstVal
	 * @param secondVal
	 * @param thirdVal
	 * @return formatted time or date 
	 */
	/**
	 * @param firstVal
	 * @param secondVal
	 * @param thirdVal
	 * @return
	 */
	public static int timeDateFormatter(int firstVal, int secondVal, String thirdVal) {
		int formattedValue = 0;
		String firstString = "";
		String secondString = "";
		
		if (firstVal < 10)
			firstString = "0" + Integer.toString(firstVal);
		else
			firstString = Integer.toString(firstVal);
		
		if (secondVal < 10)
			secondString = "0" + Integer.toString(secondVal);
		else
			secondString = Integer.toString(secondVal);
	
		formattedValue = Integer.parseInt("1" + firstString + secondString 
				+ thirdVal);
		
		return formattedValue;
	}
	
	/**
	 * Getter for Date
	 * @return Date
	 */
	public int getDate(){
		return mDate;
	}
	
	/**
	 * Getter for Time
	 * @return Time
	 */
	public int getTime(){
		return mTime;
	}

}
