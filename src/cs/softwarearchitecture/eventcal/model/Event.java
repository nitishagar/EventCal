package cs.softwarearchitecture.eventcal.model;

public class Event {
	
	private String title;
	private String start_time;
	private String end_time;
	private String location;
	private String group;
	
	private String _id;
	private String table_source;
	
	
	public Event(){

	};

	public Event(
			String title, 
			String start_time, 
			String end_time,
			String location,
			String group
			){
		this.title = title;
		this.start_time = start_time;
		this.end_time = end_time;
		this.location = location;
		this.group = group;
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
	
	public String getID(){
		return _id;
	}
	
	public void setID(String id){
		this._id = id;
	}
	
	public String getTableSource(){
		return table_source;
	}
	
	public void setTableSource(String tableSource){
		this.table_source = tableSource;
	}
}
