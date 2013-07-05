package cs.softwarearchitecture.eventcal.model;

public class Event {
	
	private String title;
	private String start_time;
	private String end_time;
	private String date;
	private String location;
	private String group;
	private int reminder;
	private int _id;
	
	
	public Event(){
		
	};

	/**
	 * @param title
	 * @param start_time
	 * @param end_time
	 * @param date
	 * @param location
	 * @param group
	 * @param reminder
	 * @param _id
	 */
	public Event(
			String title, 
			String start_time, 
			String end_time,
			String date,
			String location,
			String group,
			int reminder,
			int _id
			){
		this.title = title;
		this.start_time = start_time;
		this.end_time = end_time;
		this.date = date;
		this.location = location;
		this.group = group;
		this.reminder = reminder;
		this._id = _id;
	}

	/**
	 * @return title
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return Start time
	 */
	public String getStartTime(){
		return start_time;
	}

	/**
	 * @param start_time
	 */
	public void setStartTime(String start_time) {
		this.start_time = start_time;
	}

	/**
	 * @return End Time
	 */
	public String getEndTime() {
		return end_time;
	}

	/**
	 * @param end_time
	 */
	public void setEndTime(String end_time) {
		this.end_time = end_time;
	}

	/**
	 * @return Date
	 */
	public String getDate(){
		return date;
	}
	
	/**
	 * @param date
	 */
	public void setDate(String date){
		this.date = date;
	}
	
	/**
	 * @return
	 */
	public String getLocation(){
		return location;
	}
	
	/**
	 * @param location
	 */
	public void setLocation(String location){
		this.location = location;
	}

	/**
	 * @return
	 */
	public String getGroup(){
		return group;
	}
	
	/**
	 * @param group
	 */
	public void setGroup(String group){
		this.group = group;
	}
	
	/**
	 * @return Reminder
	 */
	public int getReminder(){
		return reminder;
	}
	
	/**
	 * @param reminder
	 */
	public void setReminder(int reminder){
		this.reminder = reminder;
	}
	
	/**
	 * @return ID of event
	 */
	public int getID(){
		return _id;
	}
	
	/**
	 * @param id
	 */
	public void setID(int id){
		this._id = id;
	}
}
