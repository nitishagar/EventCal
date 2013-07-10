package cs.softwarearchitecture.eventcal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
import com.google.cloud.backend.android.CloudQuery;
import com.google.cloud.backend.android.CloudQuery.Order;
import com.google.cloud.backend.android.CloudQuery.Scope;
 
public class MapActivity extends CloudBackendActivity implements OnMyLocationChangeListener{
 
    private GoogleMap mMap;
    protected String myLocation;
    protected String mSelfID;
    protected static boolean mLocSent;
    private static final Geohasher mGeoHasher = new Geohasher();
    
    private User mSelf;
    private List<User> mUsers = new ArrayList<User>();
    
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    @Override
    public void onMyLocationChange(Location location) {
        this.myLocation = mGeoHasher.encode(location);
        if (!mLocSent) {
            sendMyLocation();
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	saveState();
    }

	/**
	 * 
	 */
	private void saveState() {
		// save current location
//    	Log.d("MAP", "Saving ID: " + mSelf.asEntity().getId());
    	SharedPreferences.Editor editor = getSharedPreferences("Map_Session", Context.MODE_PRIVATE).edit();
    	if (mSelfID != null)
    		editor.putString("User_ID", mSelfID);
    	editor.commit();
	}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void sendMyLocation() {

        // update user state async task
        SelfConfig userConfig = new SelfConfig();
        userConfig.execute();

    }
    
    class SelfConfig extends AsyncTask<Void, Void, CloudEntity> {

    	@Override
		protected CloudEntity doInBackground(Void... cEntity) {
			try {
				
				if(mSelf == null && mSelfID == null) {
		        	Log.d("MAP", "Account name: " + MapActivity.super.getAccountName());
		        	mSelf = new User(MapActivity.super.getAccountName(), myLocation);
		        }
				else {
					Log.d("MAP", "Account name: " + MapActivity.super.getAccountName() + "Already Present");
					mSelf = new User(getCloudBackend().get(MapActivity.super.getAccountName(), mSelfID));
				}
		        
				Log.d("MAP", "Entity Update Called!");
				return getCloudBackend().update(mSelf.asEntity());
	        } catch (Exception e) {
	        	Log.e("MAP", "Exception caught: " + e.getMessage());
	            return null;
	        }
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(CloudEntity resultEntity) {
			Log.d("MAP", "Location Sent!");
       		mLocSent = true;
       		mSelfID = resultEntity.getId();
       		Log.d("MAP", "Self ID: " + mSelfID);
       		saveState();
		}

	}

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(this);
    }
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
    }
 
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        
        // Get data from shared pref.
        SharedPreferences prefs = getSharedPreferences("Map_Session", Context.MODE_PRIVATE);
        Log.d("MAP", "Retrieving SelfID: " + prefs.getString("User_ID", " "));
        mSelfID = prefs.getString("User_ID", null);
        // TextView overlay = (TextView) findViewById(R.id.overlay);
        // overlay.setText("Not signed in");
    }
    
    @Override
    protected void onPostCreate() {
    	super.onPostCreate();
    	Log.d("MAP", "Query User being called!");
        queryUsers();
    }

	private void queryUsers() {
		
		// create a response handler that will receive the query result or an error
	    CloudCallbackHandler<List<CloudEntity>> cloudHandler = new CloudCallbackHandler<List<CloudEntity>>() {
	      @Override
	      public void onComplete(List<CloudEntity> results) {
	    	  Log.d("MAP", "Mapping Users!");
//	    	  mUsers = User.fromEntities(results);
//	    	  drawMarkers();
	      }

	      @Override
	      public void onError(IOException exception) {
	    	  Log.d("MAP", "Exception!");
	    	  handleEndpointException(exception);
	      }
	    };

		// Remove previous query
		getCloudBackend().clearAllSubscription();
	    // execute the query with the handler
		CloudQuery cloudQuery = new CloudQuery("EventCalUser");
		cloudQuery.setSort(CloudEntity.PROP_UPDATED_AT, Order.DESC);
		cloudQuery.setLimit(50);
		cloudQuery.setScope(Scope.FUTURE_AND_PAST);

	    try {
	    	RetreiveUserList userList = new RetreiveUserList();
	    	userList.execute(cloudQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
 
	class RetreiveUserList extends AsyncTask<CloudQuery, Void, List<CloudEntity>> {

	    private Exception exception;

		@Override
		protected List<CloudEntity> doInBackground(CloudQuery... cq) {
			try {
				Log.d("MAP", "Execute reached");
				return getCloudBackend().list(cq[0]);
	        } catch (Exception e) {
	            this.exception = e;
	            return null;
	        }
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<CloudEntity> result) {
			mUsers = User.fromEntities(result);
			notifyMarkerUpdate();
		}

	}
	
	protected void notifyMarkerUpdate() {
		Log.d("MAP", "notify update called");
		drawMarkers();		
	}
	protected void handleEndpointException(IOException exception) {
		Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show();
		
//		Log.e(DefaultView.TAG, "Exception caught: " + exception.getMessage());
	}

	private void drawMarkers() {
		mMap.clear();
		Log.d("MAP", "Drawing Markers!");
		for (User appUser : mUsers) {
			if (appUser.getGeohash() != null) {
				LatLng pos = mGeoHasher.decode(appUser.getGeohash());
				// choose marker color
				float markerColor;
				if (appUser.getName() != null) {
					String currentName = appUser.getName();
					String userOriginalName = super.getAccountName();
				}
				if (appUser.getName() != null && appUser.getName().equals(super.getAccountName())) {
					markerColor = BitmapDescriptorFactory.HUE_AZURE;
				} else {
					markerColor = BitmapDescriptorFactory.HUE_RED;
				}
				mMap.addMarker(new MarkerOptions()
					.position(pos)
					.title(appUser.getName())
					.snippet("" + appUser.getUpdatedAt().getTime())
					.icon(BitmapDescriptorFactory
							.defaultMarker(markerColor)));
			}
		}
	}
}
