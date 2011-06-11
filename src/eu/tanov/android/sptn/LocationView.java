package eu.tanov.android.sptn;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.util.MapHelper;

public class LocationView extends MapActivity {

	private static final GeoPoint LOCATION_SOFIA = new GeoPoint(42696827, 23320916);

	private static final int REQUEST_CODE_SETTINGS = 1;
	private static final int REQUEST_CODE_FAVORITIES = 2;

	private static final String PREFERENCE_KEY_MAP_SATELLITE = "mapSatellite";
	private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_SATELLITE = false;

	private static final String PREFERENCE_KEY_MAP_STREET_VALUE = "mapStreetView";
	private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_STREET_VALUE = true;

	private static final String PREFERENCE_KEY_MAP_TRAFFIC = "mapTraffic";
	private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_TRAFFIC = false;

	private static final String PREFERENCE_KEY_MAP_COMPASS = "mapCompass";
	private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_COMPASS = false;

	private MyLocationOverlay myLocationOverlay;
	private StationsOverlay stationsOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final MapView map = (MapView) findViewById(R.id.mapview1);
		map.setBuiltInZoomControls(true);
		// locate in Sofia, before adding onLocationChanged listener
		map.getController().animateTo(LOCATION_SOFIA);
		setMapSettings();

		// add overlays
		final List<Overlay> overlays = map.getOverlays();
		stationsOverlay = new StationsOverlay(this, map);
		myLocationOverlay = new MyLocationOverlay(this, map) {
			boolean firstLocation = true;

			@Override
			public synchronized void onLocationChanged(Location location) {
				super.onLocationChanged(location);
				stationsOverlay.placeStations(location.getLatitude(), location.getLongitude(), false);

				if (firstLocation) {
					// do not move map every time, only first time
					firstLocation = false;
					map.getController().animateTo(MapHelper.createGeoPoint(location.getLatitude(), location.getLongitude()));
				}
			}
		};
		setCompassSettings();

		overlays.add(myLocationOverlay);
		overlays.add(stationsOverlay);

		map.getController().setZoom(16);
	}

	@Override
	protected void onResume() {
		if (myLocationOverlay != null) {
			myLocationOverlay.enableMyLocation();
			setCompassSettings();
		}
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (myLocationOverlay != null) {
			myLocationOverlay.disableMyLocation();
			myLocationOverlay.disableCompass();
		}
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.layout.menu, menu);
		return true;
	}

	private void setMapSettings() {
		final MapView map = (MapView) findViewById(R.id.mapview1);
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		map.setTraffic(settings.getBoolean(PREFERENCE_KEY_MAP_TRAFFIC, PREFERENCE_DEFAULT_VALUE_MAP_TRAFFIC));
		map.setSatellite(settings.getBoolean(PREFERENCE_KEY_MAP_SATELLITE, PREFERENCE_DEFAULT_VALUE_MAP_SATELLITE));
		map.setStreetView(settings.getBoolean(PREFERENCE_KEY_MAP_STREET_VALUE, PREFERENCE_DEFAULT_VALUE_MAP_STREET_VALUE));

		setCompassSettings(settings);
	}

	private void setCompassSettings() {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		setCompassSettings(settings);
	}

	private void setCompassSettings(SharedPreferences settings) {
		if (myLocationOverlay == null) {
			return;
		}

		if (settings.getBoolean(PREFERENCE_KEY_MAP_COMPASS, PREFERENCE_DEFAULT_VALUE_MAP_COMPASS)) {
			myLocationOverlay.enableCompass();
		} else {
			myLocationOverlay.disableCompass();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE_SETTINGS:
			setMapSettings();
			break;
		case REQUEST_CODE_FAVORITIES:
			if (resultCode != RESULT_OK) {
				return;
			}
			final String code = data.getStringExtra(FavoritiesActivity.EXTRA_CODE_NAME);
			if (code == null) {
				throw new IllegalStateException("No code provided");
			}

			stationsOverlay.showStation(code, true);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_favorities: {
			final Intent intent = new Intent(this, FavoritiesActivity.class);
			startActivityForResult(intent, REQUEST_CODE_FAVORITIES);
			break;
		}
		case R.id.menu_settings: {
			final Intent intent = new Intent(this, PreferencesActivity.class);
			startActivityForResult(intent, REQUEST_CODE_SETTINGS);
			break;
		}
		case R.id.menu_about:
			new AlertDialog.Builder(this).setTitle(R.string.aboutDialog_title).setCancelable(true).setMessage(R.string.aboutDialog_content)
					.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).create().show();
			break;
		case R.id.menu_help:
			new AlertDialog.Builder(this).setTitle(R.string.helpDialog_title).setCancelable(true).setMessage(R.string.helpDialog_content)
					.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).create().show();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}