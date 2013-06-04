package cs.softwarearchitecture.eventcal.dayview;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.http.ParseException;

import cs.softwarearchitecture.eventcal.R;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class DayviewFragment extends Fragment {
	
	private ImageView prevDay;
	private ImageView nextDay;
	
	private TextView currentDate;
	
	private int day;
	private int month; 
	private int year;
	
	private String startDate;
	private Calendar cal;
	private Calendar calendar;
	private Context mContext;
	
	int margin = 0;
	
	RelativeLayout dayRelative;
	Animation animFlipInForeward;
	Animation animFlipOutForeward;
	Animation animFlipInBackward;
	Animation animFlipOutBackward;
	
	private String[] timevalues = {
			"00:00 AM", "00:30 AM", "01:00 AM",
			"01:30 AM", "02:00 AM", "02:30 AM", "03:00 AM", "03:30 AM",
			"04:00 AM", "04:30 AM", "05:00 AM", "05:30 AM", "06:00 AM",
			"06:30 AM", "07:00 AM", "07:30 AM", "08:00 AM", "08:30 AM",
			"09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM",
			"11:30 AM", "12:00 PM", "12:30 PM", "13:00 PM", "13:30 PM",
			"14:00 PM", "14:30 PM", "15:00 PM", "15:30 PM", "16:00 PM",
			"16:30 PM", "17:00 PM", "17:30 PM", "18:00 PM", "18:30 PM",
			"19:00 PM", "19:30 PM", "20:00 PM", "20:30 PM", "21:00 PM",
			"21:30 PM", "22:00 PM", "22:30 PM", "23:00 PM", "23:30 PM"	
			};
	
	private String months[] = {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" 	
			};
	
	private String monthsNumbers[] = { 
			"01", "02", "03", "04", "05", "06",
			"07", "08", "09", "10", "11", "12" 
			};

	private final DateFormat dateformatter = new DateFormat();
	
	String eventDate;
	String title;
	
	public DayviewFragment(){
		
	}
	
	public DayviewFragment(String startDate){
		this.startDate = startDate;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup containerView = (ViewGroup) inflater.inflate(
				R.layout.dayview, container, false);
	
		dayRelative = (RelativeLayout) 
				containerView.findViewById(R.id.dayRelative);
		
		prevDay = (ImageView) containerView.findViewById(R.id.prevDay);
		nextDay = (ImageView) containerView.findViewById(R.id.nextDay);
		currentDate = 
				(TextView) containerView.findViewById(R.id.currentDate);
		
		calendar = Calendar.getInstance(Locale.getDefault());
		cal = Calendar.getInstance(Locale.getDefault());
	
		mContext = getActivity().getApplicationContext();
		
		month =calendar.get(Calendar.MONTH);
		year = calendar.get(Calendar.YEAR);
		day = calendar.get(Calendar.DAY_OF_MONTH);
		
		if(startDate == null){
			startDate = (month + 1) + "/" + day + "/" + year;
		}
	
		Date date = new Date(startDate);
		startDate = (String) dateformatter.format("yyyy/MM/dd", date);
		eventDate = (String) dateformatter.format("MM/dd/yyyy", date);
		
		date = new Date(startDate);
		cal.setTime(date);
		
		String currentMonth = months[cal.get(Calendar.MONTH)];
		String currentDay = cal.get(Calendar.DATE) + " ";
		String currentYear = " " + cal.get(Calendar.YEAR); 
		
		currentDate.setText(currentDay + currentMonth + "," + currentYear);
	
		try {
			loadDataForDay();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		prevDay.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0){
				
				try {
					onLTRFling();
				} catch (ParseException e){
					e.printStackTrace();
				}
			}
		});
		
		nextDay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				try {
					onRTLFling();
				} catch (ParseException e){
					e.printStackTrace();
				}
				
			}
		});
		return containerView;
	}

	private void loadDataForDay() {
		// TODO Auto-generated method stub
		
	}
} 
