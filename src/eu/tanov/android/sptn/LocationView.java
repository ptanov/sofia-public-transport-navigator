package eu.tanov.android.sptn;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.util.MapHelper;

public class LocationView extends MapActivity {
    private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_10 = "whatsNewShowVersion1_10_startupScreenFavorities";
    private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_10 = true;

    private static final GeoPoint LOCATION_SOFIA = new GeoPoint(42696827, 23320916);

    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final int REQUEST_CODE_FAVORITIES = 2;

    private static final String PREFERENCE_KEY_MAP_SATELLITE = "mapSatellite";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_SATELLITE = false;

    // issue #49: actually not used, mapSatellite has more priority
    // private static final String PREFERENCE_KEY_MAP_STREET_VALUE = "mapStreetView";
    // private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_STREET_VALUE = true;

    private static final String PREFERENCE_KEY_MAP_TRAFFIC = "mapTraffic";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_TRAFFIC = false;

    private static final String PREFERENCE_KEY_MAP_COMPASS = "mapCompass";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_COMPASS = false;

    private static final String PREFERENCE_KEY_STARTUP_SCREEN_FAVORITIES = "commonStartupScreenFavorities";
    private static final boolean PREFERENCE_DEFAULT_VALUE_STARTUP_SCREEN_FAVORITIES = false;
    private static final int DIALOG_ID_ABOUT = 1;
    private static final int DIALOG_ID_PROGRESS_PLACE_STATIONS = 2;
    private static final int DIALOG_ID_PROGRESS_QUERY_STATION = 3;

    private MyLocationOverlay myLocationOverlay;
    private StationsOverlay stationsOverlay;
    private boolean progressPlaceStationsDisplayed = false;
    private boolean progressQueryStationsDisplayed = false;

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
                    map.getController().animateTo(
                            MapHelper.createGeoPoint(location.getLatitude(), location.getLongitude()));
                }
            }
        };
        setCompassSettings();

        overlays.add(myLocationOverlay);
        overlays.add(stationsOverlay);

        map.getController().setZoom(16);
        notifyForChangesInNewVersions();

        selectStartupScreen();
    }

    private void notifyForChangesInNewVersions() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_10, PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_10)) {
            final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            // show only first time:
            editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_10, false);
            editor.commit();

            new AlertDialog.Builder(this).setTitle(R.string.versionChanges_1_10_startupScreen_title)
                    .setCancelable(true)
                    .setMessage(R.string.versionChanges_1_10_startupScreen_text)
                    .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }

    }

    private void selectStartupScreen() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean startupScreenFavorities = settings.getBoolean(PREFERENCE_KEY_STARTUP_SCREEN_FAVORITIES,
                PREFERENCE_DEFAULT_VALUE_STARTUP_SCREEN_FAVORITIES);
        if (startupScreenFavorities) {
            navigateToFavorities();
        }
    }

    public void disableLocationUpdates() {
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay.disableCompass();
        }
    }

    public void enableLocationUpdates() {
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
            setCompassSettings();
        }
    }

    @Override
    protected void onResume() {
        enableLocationUpdates();
        super.onResume();
    }

    @Override
    protected void onPause() {
        disableLocationUpdates();
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
        // as suggested in
        // http://stackoverflow.com/questions/7478952/mapview-rendering-with-tiles-missing-with-an-x-in-the-center#7510957
        // :
        // map.setStreetView(settings.getBoolean(PREFERENCE_KEY_MAP_STREET_VALUE,
        // PREFERENCE_DEFAULT_VALUE_MAP_STREET_VALUE));

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
            navigateToFavorities();
            break;
        }
        case R.id.menu_settings: {
            final Intent intent = new Intent(this, PreferencesActivity.class);
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
            break;
        }
        case R.id.menu_about:
            showDialog(DIALOG_ID_ABOUT);

            break;
        case R.id.menu_help:
            new AlertDialog.Builder(this).setTitle(R.string.helpDialog_title).setCancelable(true)
                    .setMessage(R.string.helpDialog_content)
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
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_ID_ABOUT:
            return createAboutDialog();

        case DIALOG_ID_PROGRESS_PLACE_STATIONS: {
            final ProgressDialog progressPlaceStations = new ProgressDialog(this);
            progressPlaceStations.setTitle(R.string.progressDialog_title);
            progressPlaceStations.setMessage(getResources().getString(R.string.progressDialog_message_stations));
            progressPlaceStations.setIndeterminate(true);
            progressPlaceStations.setCancelable(false);

            return progressPlaceStations;
        }

        case DIALOG_ID_PROGRESS_QUERY_STATION: {
            final ProgressDialog progressQueryStations = new ProgressDialog(this);
            progressQueryStations.setTitle(R.string.progressDialog_title);
            progressQueryStations.setMessage(getResources().getString(R.string.progressDialog_message_estimating));
            progressQueryStations.setIndeterminate(true);
            progressQueryStations.setCancelable(false);

            return progressQueryStations;
        }
        default:
            return null;
        }
    }

    private Dialog createAboutDialog() {
        final View view = this.getLayoutInflater().inflate(R.layout.about_dialog, null);
        final TextView androidMarketLink = (TextView) view.findViewById(R.id.androidMarketLink);
        androidMarketLink.setMovementMethod(LinkMovementMethod.getInstance());
        androidMarketLink.setText(Html.fromHtml(getResources().getString(R.string.aboutDialog_market)));
        return new AlertDialog.Builder(this).setTitle(R.string.aboutDialog_title).setCancelable(true).setView(view)
                .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                }).create();
    }

    private void navigateToFavorities() {
        final Intent intent = new Intent(this, FavoritiesActivity.class);
        startActivityForResult(intent, REQUEST_CODE_FAVORITIES);
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    public void hideProgressPlaceStations() {
        if (!progressPlaceStationsDisplayed) {
            return;
        }
        progressPlaceStationsDisplayed = false;
        removeDialog(DIALOG_ID_PROGRESS_PLACE_STATIONS);
    }

    public void showProgressPlaceStations() {
        if (progressPlaceStationsDisplayed) {
            return;
        }
        progressPlaceStationsDisplayed = true;
        showDialog(DIALOG_ID_PROGRESS_PLACE_STATIONS);
    }

    public void hideProgressQueryStation() {
        if (!progressQueryStationsDisplayed) {
            return;
        }
        progressQueryStationsDisplayed = false;

        removeDialog(DIALOG_ID_PROGRESS_QUERY_STATION);
    }

    public void showProgressQueryStation() {
        if (progressQueryStationsDisplayed) {
            return;
        }
        progressQueryStationsDisplayed = true;

        showDialog(DIALOG_ID_PROGRESS_QUERY_STATION);
    }

}
