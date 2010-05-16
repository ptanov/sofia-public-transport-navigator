package eu.tanov.android.spt;

import java.util.List;

import android.location.Location;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import eu.tanov.android.spt.map.StationsOverlay;
import eu.tanov.android.spt.util.MapHelper;

public class LocationView extends MapActivity {

	private MyLocationOverlay myLocationOverlay;
	private StationsOverlay stationsOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final MapView map = (MapView) findViewById(R.id.mapview1);
		map.setBuiltInZoomControls(true);

		//add overlays
		final List<Overlay> overlays = map.getOverlays();
		stationsOverlay = new StationsOverlay(this, map);
		myLocationOverlay = new MyLocationOverlay(this, map) {
			@Override
			public synchronized void onLocationChanged(Location location) {
				super.onLocationChanged(location);

				stationsOverlay.placeStations(location);
				//FIXME remove:
				map.getController().animateTo(
						MapHelper.createGeoPoint(location.getLatitude(),location.getLongitude()));
			}
		};
//		myLocationOverlay.runOnFirstFix(new Runnable() {
//			
//			@Override
//			public void run() {
//				System.out.println("sdfasdf sadf saf"+myLocationOverlay.getMyLocation());
//				System.out.println("sdfasdf sadf saf"+myLocationOverlay.getLastFix());
//			}
//		});
		overlays.add(stationsOverlay);
		overlays.add(myLocationOverlay);

		map.getController().setZoom(16);
	}

	@Override
	protected void onResume() {
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
		}
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
		}
		super.onPause();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}