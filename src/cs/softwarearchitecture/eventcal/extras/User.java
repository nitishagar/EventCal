package cs.softwarearchitecture.eventcal.extras;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.cloud.backend.android.CloudEntity;

public class User {

	private CloudEntity cloudEntity;

	public static final String KEY_NAME = "name";
	public static final String KEY_GEOHASH = "location";

	public User(String name, String geohash) {
		this.cloudEntity = new CloudEntity("EventCalUser");
		this.setName(name);
		this.setGeohash(geohash);
	}

	public User(CloudEntity e) {
		this.cloudEntity = e;
	}

	public CloudEntity asEntity() {
		return this.cloudEntity;
	}

	public static List<User> fromEntities(List<CloudEntity> entities) {
		List<User> users = new ArrayList<User>();
		for (CloudEntity cloudEntity : entities) {
			users.add(new User(cloudEntity));
		}
		return users;
	}

	public String getName() {
		return (String) cloudEntity.get(KEY_NAME);
	}

	public void setName(String name) {
		cloudEntity.put(KEY_NAME, name);
	}

	public String getGeohash() {
		return (String) cloudEntity.get(KEY_GEOHASH);
	}

	public void setGeohash(String geohash) {
		cloudEntity.put(KEY_GEOHASH, geohash);
	}

	public Date getUpdatedAt() {
		return cloudEntity.getUpdatedAt();
	}

}