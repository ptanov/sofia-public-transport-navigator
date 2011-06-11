package eu.tanov.android.sptn.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.providers.StationProvider;
import eu.tanov.android.sptn.providers.StationProvider.Station;
import eu.tanov.android.sptn.sumc.EstimatesResolver;
import eu.tanov.android.sptn.sumc.HtmlResult;
import eu.tanov.android.sptn.util.MapHelper;

public class StationsOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String TAG = "StationsOverlay";

	private static final String PREFERENCE_KEY_SHOW_REMAINING_TIME = "showRemainingTime";
	private static final boolean PREFERENCE_DEFAULT_VALUE_SHOW_REMAINING_TIME = true;

	private final ArrayList<OverlayItem> stations = new ArrayList<OverlayItem>();
	private final Activity context;

	private final Map<String, OverlayItem> codeToOverlayItem = new HashMap<String, OverlayItem>();
	private ProgressDialog pd;
	private final Handler uiHandler = new Handler();

	private final MapView map;

	private static final String[] PROJECTION = new String[] { Station._ID, // 0
			Station.CODE, // 1
			Station.LAT, // 2
			Station.LON, // 3
			Station.LABEL, // 4
	};

	public class StationsQuery extends BaseQuery {
		private final double lon;
		private final double lat;

		public StationsQuery(double lat, double lon) {
			this.lat = lat;
			this.lon = lon;
		}

		private String sortOrder(double lat, double lon) {
			return String.format("(ABS(lat-(%f))+ABS(lon-(%f))) ASC", lat, lon);
		}

		@Override
		protected Cursor createCursor() {
			return context.managedQuery(StationProvider.CONTENT_URI, PROJECTION, null, null, sortOrder(lat, lon));
		}

	}

	public class StationQuery extends BaseQuery {
		private final String code;

		public StationQuery(String code) {
			this.code = code;
		}

		@Override
		protected Cursor createCursor() {
			return context.managedQuery(StationProvider.CONTENT_URI, PROJECTION, "code=" + code, null, null);
		}
	}

	public abstract class BaseQuery implements Runnable {

		@Override
		public void run() {
			try {
				final Cursor cursor = createCursor();

				if (!cursor.moveToFirst()) {
					// no results
					return;
				}

				// iterate over result
				int codeColumn = cursor.getColumnIndex(Station.CODE);
				int labelColumn = cursor.getColumnIndex(Station.LABEL);
				int latColumn = cursor.getColumnIndex(Station.LAT);
				int lonColumn = cursor.getColumnIndex(Station.LON);

				String code;
				String label;
				double lat;
				double lon;
				final ArrayList<OverlayItem> newStations = new ArrayList<OverlayItem>(StationProvider.STATIONS_LIMIT);

				do {
					// Get the field values
					code = cursor.getString(codeColumn);
					if (codeToOverlayItem.containsKey(code)) {
						// already added
						continue;
					}
					// not a best way to save index of node, but it works if stations are not removed
					label = cursor.getString(labelColumn);
					lat = cursor.getDouble(latColumn);
					lon = cursor.getDouble(lonColumn);

					final GeoPoint point = MapHelper.createGeoPoint(lat, lon);
					final OverlayItem overlayItem = new OverlayItem(point, code, label);
					codeToOverlayItem.put(code, overlayItem);
					newStations.add(overlayItem);
				} while (cursor.moveToNext());

				// in UI thread
				populateInUiThread(newStations);
			} finally {
				hideProgressDialog();
			}
		}

		protected abstract Cursor createCursor();
	}

	public class EstimatesQuery extends Thread {

		private final OverlayItem overlayItem;

		public EstimatesQuery(OverlayItem overlayItem) {
			if (overlayItem == null) {
				throw new IllegalArgumentException("overlay item is null");
			}
			this.overlayItem = overlayItem;
		}

		@Override
		public void run() {
			final String stationCode = overlayItem.getTitle();
			final String stationLabel = overlayItem.getSnippet();

			try {
				final EstimatesResolver resolver = new HtmlResult(context, stationCode, stationLabel, showRemainingTime());
				// long operation
				resolver.query();

				// in UI thread
				showEstimates(resolver);
			} catch (Exception e) {
				Log.e(TAG, "could not get estimations for " + stationCode + ". " + stationLabel, e);
				// being safe (Throwable!?) ;)
				showErrorMessage(stationLabel, stationCode);
			} finally {
				hideProgressDialog();
			}

			return;
		}

	}

	public StationsOverlay(Activity context, MapView map) {
		super(boundCenterBottom(context.getResources().getDrawable(R.drawable.station)));
		this.context = context;
		this.map = map;
		populateFixed();
	}

	/**
	 * from http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
	 * http://developmentality.wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-
	 * workarounds/
	 */
	private void populateFixed() {
		setLastFocusedIndex(-1);
		populate();
		// redraw items
		map.invalidate();
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		final boolean result = super.onTap(p, mapView);
		if (!result) {
			// only if not on overlay item
			placeStations(MapHelper.toCoordinate(p.getLatitudeE6()), MapHelper.toCoordinate(p.getLongitudeE6()), true);
		}
		return result;
	}

	/**
	 * should be called if location changes
	 * 
	 * @param location
	 *            new location
	 */
	public void placeStations(double newLat, double newLon, boolean showDialog) {
		final StationsQuery query = new StationsQuery(newLat, newLon);
		if (showDialog) {
			createProgressDialog(R.string.progressDialog_message_stations);
		}
		new Thread(query).start();
	}

	public void placeStation(String code) {
		if (codeToOverlayItem.containsKey(code)) {
			return;
		}
		new StationQuery(code).run();
	}

	public void showStation(String code, boolean animateTo) {
		if (!codeToOverlayItem.containsKey(code)) {
			placeStation(code);
		}
		final OverlayItem station = codeToOverlayItem.get(code);
		if (station == null) {
			throw new IllegalStateException("Unknown code: " + code);
		}
		if (animateTo) {
			map.getController().animateTo(station.getPoint());
		}
		showStation(station);
	}

	protected void showStation(OverlayItem station) {
		final EstimatesQuery query = new EstimatesQuery(station);
		createProgressDialog(R.string.progressDialog_message_estimating);
		query.start();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return stations.get(i);
	}

	@Override
	public int size() {
		return stations.size();
	}

	@Override
	protected boolean onTap(int stationIndex) {
		showStation(stations.get(stationIndex));

		return true;
	}

	/**
	 * or hour of arriving
	 * 
	 * @return
	 */
	private boolean showRemainingTime() {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		return settings.getBoolean(PREFERENCE_KEY_SHOW_REMAINING_TIME, PREFERENCE_DEFAULT_VALUE_SHOW_REMAINING_TIME);
	}

	/**
	 * runs in ui thread
	 * 
	 * @param stationCode
	 */
	private void showErrorMessage(final String stationLabel, final String stationCode) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				final String message = context.getResources()
						.getString(R.string.error_retrieveEstimates_generic, stationLabel, stationCode);
				Toast.makeText(context, message, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void populateInUiThread(final ArrayList<OverlayItem> newStations) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				stations.addAll(newStations);
				populateFixed();
			}
		});
	}

	private void showEstimates(final EstimatesResolver resolver) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				resolver.showResult();
			}
		});
	}

	private void createProgressDialog(int message) {
		pd = ProgressDialog.show(context, context.getResources().getString(R.string.progressDialog_title), context.getResources()
				.getString(message), true, false);
	}

	private void hideProgressDialog() {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				if (pd != null) {
					pd.dismiss();
					pd = null;
				}
			}
		});
	}

}
