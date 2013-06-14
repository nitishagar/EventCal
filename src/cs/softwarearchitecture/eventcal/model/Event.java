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

	public String getTitle(){
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getStartTime(){
		return start_time;
	}

	public void setStartTime(String start_time) {
		this.start_time = start_time;
	}

	public String getEndTime() {
		return end_time;
	}

	public void setEndTime(String end_time) {
		this.end_time = end_time;
	}

	public String getDate(){
		return date;
	}
	
	public void setDate(String date){
		this.date = date;
	}
	
	public String getLocation(){
		return location;
	}
	
	public void setLocation(String location){
		this.location = location;
	}

	public String getGroup(){
		return group;
	}
	
	public void setGroup(String group){
		this.group = group;
	}
	
	public int getReminder(){
		return reminder;
	}
	
	public void setReminder(int reminder){
		this.reminder = reminder;
	}
	
	public int getID(){
		return _id;
	}
	
	public void setID(int id){
		this._id = id;
	}
}
