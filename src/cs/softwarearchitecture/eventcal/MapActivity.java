package cs.softwarearchitecture.eventcal;

import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.cloud.backend.android.CloudBackendActivity;
import com.google.cloud.backend.android.CloudCallbackHandler;
import com.google.cloud.backend.android.CloudEntity;
 
public class MapActivity extends CloudBackendActivity implements OnMyLocationChangeListener{
 
    private GoogleMap mMap;
    protected String myLocation;
    protected static boolean mLocSent;
    private static final Geohasher geoHasher = new Geohasher();
    
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
        this.myLocation = geoHasher.encode(location);
        if (!mLocSent) {
            sendMyLocation();
        }
    }
    
    @SuppressWarnings("unchecked")
	private void sendMyLocation() {
        final CloudEntity self = new CloudEntity("Me");
        self.put("interest", "Cloud");
        self.put("location", this.myLocation);
        getCloudBackend().update(self, new CloudCallbackHandler() {
        	
			@Override
			public void onComplete(Object results) {
				mLocSent = true;
                drawMyMarker();
				
			}

			private void drawMyMarker() {
				mMap.clear();
				if (myLocation != null) {
					LatLng pos = geoHasher.decode(myLocation);
					// choose marker color
					float markerColor;
					markerColor = BitmapDescriptorFactory.HUE_AZURE;
	
					mMap.addMarker(new MarkerOptions()
					.position(pos)
					.title("Me")
					.icon(BitmapDescriptorFactory
							.defaultMarker(markerColor)));
				}
			}
        });
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
        TextView overlay = (TextView) findViewById(R.id.overlay);
        // overlay.setText("Not signed in");
    }
 
}