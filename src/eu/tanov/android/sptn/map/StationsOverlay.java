package eu.tanov.android.sptn.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

import eu.tanov.android.sptn.LocationView;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.providers.InitStations;
import eu.tanov.android.sptn.providers.StationProvider;
import eu.tanov.android.sptn.providers.StationProvider.Station;
import eu.tanov.android.sptn.sumc.EstimatesResolver;
import eu.tanov.android.sptn.sumc.SofiaTrafficHtmlResult;
import eu.tanov.android.sptn.sumc.VarnaTrafficHtmlResult;
import eu.tanov.android.sptn.sumc.VarnaTrafficHtmlResult.DeviceData;
import eu.tanov.android.sptn.util.MapHelper;

public class StationsOverlay extends ItemizedOverlay<OverlayItem> {
    private static final String TAG = "StationsOverlay";

    private static final String BUSSTOP_PROVIDER_LABEL_SEPARATOR = ":";

    private static final String PREFERENCE_KEY_SHOW_REMAINING_TIME = "showRemainingTime";
    private static final boolean PREFERENCE_DEFAULT_VALUE_SHOW_REMAINING_TIME = true;

    private final ArrayList<OverlayItem> stations = new ArrayList<OverlayItem>();
    private final LocationView context;

    private final Map<String, Map<String, OverlayItem>> providerToOverlayMap = new HashMap<String, Map<String,OverlayItem>>();
    private final Handler uiHandler = new Handler();

    private final MapView map;

    private OverlayItem showBusesOverlayItem;


    private static final String[] PROJECTION = new String[] { Station._ID, // 0
            Station.CODE, // 1
            Station.LAT, // 2
            Station.LON, // 3
            Station.LABEL, // 4
            Station.PROVIDER // 5
    };

    public class StationsQuery extends BaseQuery {
        private final double lon;
        private final double lat;

        public StationsQuery(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        private String sortOrder(double lat, double lon) {
            return String.format(Locale.US, "(ABS(lat-(%f))+ABS(lon-(%f))) ASC", lat, lon);
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
                final int codeColumn = cursor.getColumnIndex(Station.CODE);
                final int labelColumn = cursor.getColumnIndex(Station.LABEL);
                final int latColumn = cursor.getColumnIndex(Station.LAT);
                final int lonColumn = cursor.getColumnIndex(Station.LON);
                final int providerColumn = cursor.getColumnIndex(Station.PROVIDER);

                String code;
                String label;
                double lat;
                double lon;
                String provider;
                final ArrayList<OverlayItem> newStations = new ArrayList<OverlayItem>(StationProvider.STATIONS_LIMIT);

                do {
                    // Get the field values
                    code = cursor.getString(codeColumn);
                    provider = cursor.getString(providerColumn);
                    if (getOverlayItem(provider, code) != null) {
                        // already added
                        continue;
                    }
                    // not a best way to save index of node, but it works if stations are not removed
                    label = cursor.getString(labelColumn);
                    lat = cursor.getDouble(latColumn);
                    lon = cursor.getDouble(lonColumn);

                    final GeoPoint point = MapHelper.createGeoPoint(lat, lon);
                    final OverlayItem overlayItem = new OverlayItem(point, code, provider + BUSSTOP_PROVIDER_LABEL_SEPARATOR + label);
                    addOverlayItem(provider, code, overlayItem);
                    newStations.add(overlayItem);
                } while (cursor.moveToNext());

                // in UI thread
                populateInUiThread(newStations);
            } catch (Exception e) {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        context.hideProgressPlaceStations();
                    }
                });
                throw new RuntimeException("populate stations", e);
            }
        }

        protected abstract Cursor createCursor();
    }

    public class EstimatesQuery extends Thread {

        private final OverlayItem overlayItem;
        private final boolean showOnlyBuses;

        public EstimatesQuery(OverlayItem overlayItem, boolean showOnlyBuses) {
            if (overlayItem == null) {
                throw new IllegalArgumentException("overlay item is null");
            }
            this.overlayItem = overlayItem;
            this.showOnlyBuses = showOnlyBuses;
        }

        private EstimatesResolver createResolver(String busStopSource, String stationCode, String stationLabel) {
            if (InitStations.PROVIDER_SOFIATRAFFIC.equals(busStopSource)) {
                return new SofiaTrafficHtmlResult(context, uiHandler, StationsOverlay.this,
                        stationCode, stationLabel,
                        showRemainingTime());
            }
            if (InitStations.PROVIDER_VARNATRAFFIC.equals(busStopSource)) {
                return new VarnaTrafficHtmlResult(context, StationsOverlay.this, stationCode, stationLabel);
            }

            throw new IllegalStateException("Unknown source: " + busStopSource);
        }
        @Override
        public void run() {
            try {
                final String stationCode = overlayItem.getTitle();
                final String[] snippets = overlayItem.getSnippet().split(BUSSTOP_PROVIDER_LABEL_SEPARATOR, 2);
                final String stationLabel = snippets[1];
                final String busStopSource = snippets[0];
                try {
                    final EstimatesResolver resolver = createResolver(busStopSource, stationCode, stationLabel);
                    if (resolver.hasBusSupport()) {
                        StationsOverlay.this.showBusesOverlayItem = this.overlayItem;
                    }
                    // long operation
                    resolver.query();

                    // in UI thread
                    showEstimates(resolver, showOnlyBuses);
                } catch (Exception e) {
                    Log.e(TAG, "could not get estimations for " + stationCode + ". " + stationLabel, e);
                    // being safe (Throwable!?) ;)
                    showErrorMessage(stationLabel, stationCode);

                    uiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            context.hideProgressQueryStation();
                        }
                    });
                }

            } finally {
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        context.hideProgressQueryStation();
                    }
                });
            }
        }

    }

    public StationsOverlay(LocationView context, MapView map) {
        super(boundCenterBottom(context.getResources().getDrawable(R.drawable.station)));
        this.context = context;
        this.map = map;
        populateFixed();
    }

    /**
     * from http://groups.google.com/group/android-developers/browse_thread/thread/38b11314e34714c3
     * http://developmentality
     * .wordpress.com/2009/10/19/android-itemizedoverlay-arrayindexoutofboundsexception-nullpointerexception-
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
        if(showDialog) {
            context.showProgressPlaceStations();
        }
        new Thread(query).start();
    }

    public void placeStation(String provider, String code) {
        if (getOverlayItem(provider, code) != null) {
            return;
        }
        new StationQuery(code).run();
    }
    public void showStation(String provider, String code, boolean animateTo) {
        showStation(provider, code, animateTo, false);
    }
    private OverlayItem getOverlayItem(String provider, String code) {
        final Map<String, OverlayItem> map = providerToOverlayMap.get(provider);
        if (map == null) {
            return null;
        }
        return map.get(code);
    }
    private void addOverlayItem(String provider, String code, OverlayItem item) {
        Map<String, OverlayItem> map = providerToOverlayMap.get(provider);
        if (map == null) {
            map = new HashMap<String, OverlayItem>();
            providerToOverlayMap.put(provider, map);
        }
        map.put(code, item);
    }
    private void showStation(String provider, String code, boolean animateTo, boolean throwIfNotFound) {
        if (getOverlayItem(provider, code) == null) {
            placeStation(provider, code);
        }
        OverlayItem station = getOverlayItem(provider, code);
        if (station == null) {
            if (throwIfNotFound) {
                // for future improving, now bus stops entered in SEARCH by busstop id can be unknown to our DB
                throw new IllegalStateException("Unknown code: " + code);
            } else {
                station = new OverlayItem(new GeoPoint(0, 0), code, provider + BUSSTOP_PROVIDER_LABEL_SEPARATOR + code);
            }
        } else {
            if (animateTo) {
                map.getController().animateTo(station.getPoint());
            }
        }
        showStation(station, false);
    }

    protected void showStation(OverlayItem station, boolean showOnlyBuses) {
        if (!showOnlyBuses) {
            context.getBusesOverlay().showBusses(Collections.<DeviceData>emptyList());
        }
        showBusesOverlayItem = null;
        final EstimatesQuery query = new EstimatesQuery(station, showOnlyBuses);
        if (!showOnlyBuses) {
            context.showProgressQueryStation();
        }
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
        showStation(stations.get(stationIndex), false);

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
                context.hideProgressPlaceStations();
            }
        });
    }

    private void showEstimates(final EstimatesResolver resolver, final boolean showOnlyBuses) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (context.isFetchingCancelled()) {
                    return;
                }
                resolver.showResult(showOnlyBuses);
                context.hideProgressQueryStation();
            }
        });
    }

    public OverlayItem getShowBusesOverlayItem() {
        return showBusesOverlayItem;
    }
    
    public void showBuses() {
        if (showBusesOverlayItem != null) {
            showStation(showBusesOverlayItem, true);
        }
    }
}
