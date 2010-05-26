package eu.tanov.android.spt;

import java.util.List;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import eu.tanov.android.spt.map.StationsOverlay;
import eu.tanov.android.spt.util.MapHelper;

public class LocationView extends MapActivity {

	private static final GeoPoint LOCATION_SOFIA = new GeoPoint(42696827, 23320916);

	private MyLocationOverlay myLocationOverlay;
	private StationsOverlay stationsOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final MapView map = (MapView) findViewById(R.id.mapview1);
		map.setBuiltInZoomControls(true);
		//locate in Sofia, before adding onLocationChanged listener
		map.getController().animateTo(LOCATION_SOFIA);

		//add overlays
		final List<Overlay> overlays = map.getOverlays();
		stationsOverlay = new StationsOverlay(this, map);
		myLocationOverlay = new MyLocationOverlay(this, map) {
			boolean firstLocation = true;
			@Override
			public synchronized void onLocationChanged(Location location) {
				super.onLocationChanged(location);

				stationsOverlay.placeStations(location.getLatitude(), location.getLongitude());
				
				//FIXME remove:
				if (firstLocation) {
					//do not move map every time, only first time
					firstLocation = false;
					map.getController().animateTo(
							MapHelper.createGeoPoint(location.getLatitude(),location.getLongitude()));
				}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.layout.menu, menu);  
	    return true;  
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			
			break;
		case R.id.menu_about:
			new AlertDialog.Builder(this).
				setTitle(R.string.aboutDialog_title).
				setCancelable(true).
				setMessage(R.string.aboutDialog_content).
				create().show();
			break;

		default:
			break;
		}
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}