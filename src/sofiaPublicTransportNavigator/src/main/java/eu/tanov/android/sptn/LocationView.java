package eu.tanov.android.sptn;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

import eu.tanov.android.bptcommon.favorities.FavoritiesService;
import eu.tanov.android.bptcommon.interfaces.ILocationView;
import eu.tanov.android.bptcommon.utils.ActivityTracker;
import eu.tanov.android.sptn.map.BusesOverlay;
import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.providers.BusStopUpdater;
import eu.tanov.android.sptn.providers.InitStations;
import eu.tanov.android.sptn.util.LocaleHelper;
import eu.tanov.android.sptn.util.MapHelper;

public class LocationView extends MapActivity implements ILocationView {
    public static final String FLIRRY_ID = "7S33XB55J3RXDK4HBJNP";

    private final class UpdateBusStopsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final BusStopUpdater updater;

        private UpdateBusStopsAsyncTask(BusStopUpdater updater) {
            this.updater = updater;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            return updater.update();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                ActivityTracker.busStopsUpdatedSuccess(updater.getContext());

                Toast.makeText(LocationView.this, R.string.update_busstop_toast_update_success, Toast.LENGTH_LONG)
                        .show();
            } else {
                ActivityTracker.busStopsUpdatedError(updater.getContext());

                Toast.makeText(LocationView.this, R.string.update_busstop_toast_update_failure, Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private final class CheckForUpdateAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final BusStopUpdater updater = new BusStopUpdater(LocationView.this);

        @Override
        protected Boolean doInBackground(Void... params) {
            return updater.isUpdateAvailable();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                return;
            }
            if (isWifiConnected()) {
                // do not ask user when there is WIFI
                new UpdateBusStopsAsyncTask(updater).execute();
            } else {
                new AlertDialog.Builder(LocationView.this)
                        .setTitle(R.string.update_busstop_dialog_dataconnection_title).setCancelable(true)
                        .setMessage(R.string.update_busstop_dialog_dataconnection_text)
                        .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                new UpdateBusStopsAsyncTask(updater).execute();
                            }
                        }).setNegativeButton(R.string.buttonCancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityTracker.busStopsUpdateCancel(LocationView.this);
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        }
    }

    private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_10 = "whatsNewShowVersion1_10_startupScreenFavorities";
    private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_10 = false;

    private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_17 = "whatsNewShowVersion1_17_tixbg";
    private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_17 = false;

    private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_20 = "whatsNewShowVersion1_20_searchByBusStopId";
    private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_20 = false;

    private static final int REQUEST_CODE_SETTINGS = 1;
    private static final int REQUEST_CODE_FAVORITIES = 2;

    private static final String PREFERENCE_KEY_MAP_SATELLITE = "mapSatellite";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_SATELLITE = false;

    // issue #49: actually not used, mapSatellite has more priority
    // private static final String PREFERENCE_KEY_MAP_STREET_VALUE = "mapStreetView";
    // private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_STREET_VALUE = true;

    private static final String PREFERENCE_KEY_MAP_TRAFFIC = "mapTrafficEnabled";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_TRAFFIC = true;

    private static final String PREFERENCE_KEY_MAP_COMPASS = "mapCompass";
    private static final boolean PREFERENCE_DEFAULT_VALUE_MAP_COMPASS = false;

    private static final String PREFERENCE_KEY_STARTUP_SCREEN_FAVORITIES = "commonStartupScreenFavorities";
    private static final boolean PREFERENCE_DEFAULT_VALUE_STARTUP_SCREEN_FAVORITIES = false;
    private static final String PREFERENCE_KEY_DEFAULT_PROVIDER = "defaultProvider";

    public static final String PREFERENCE_KEY_STATISTICS_DISABLE = "disableStatistics";
    public static final boolean PREFERENCE_DEFAULT_VALUE_STATISTICS_DISABLE = false;

    private static final String PREFERENCE_KEY_LAST_LOCATION_LATITUDE_E6 = "lastLocationLatitudeE6";
    private static final String PREFERENCE_KEY_LAST_LOCATION_LONGITUDE_E6 = "lastLocationLongitudeE6";
    private static final String PREFERENCE_KEY_LAST_LOCATION_ZOOM = "lastLocationZoom";
    private static final int LOCATION_SOFIA_LATITUDE_E6 = 42696827;
    private static final int LOCATION_SOFIA_LONGITUDE_E6 = 23320916;
    private static final int ZOOM_DEFAULT = 16;

    private static final int DIALOG_ID_ABOUT = 1;
    private static final int DIALOG_ID_PROGRESS_PLACE_STATIONS = 2;
    private static final int DIALOG_ID_PROGRESS_QUERY_STATION = 3;
    private static final String PACKAGE_TIX_BG = "bg.tix";
    private static final String MARKET_TIX_BG = "market://details?id=" + PACKAGE_TIX_BG;

    private MyLocationOverlay myLocationOverlay;
    private StationsOverlay stationsOverlay;
    private BusesOverlay busesOverlay;
    private boolean progressPlaceStationsDisplayed = false;
    private boolean progressQueryStationsDisplayed = false;
    private boolean estimatesDialogVisible = false;

    private String userLocale;

    private MapView map;
    private Timer timer;
    private boolean fetchingCancelled = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.selectLocale(this);
        userLocale = LocaleHelper.getUserLocale(this);
        setContentView(R.layout.main);
        map = (MapView) findViewById(R.id.mapview1);
        map.setBuiltInZoomControls(true);

        // add overlays
        final List<Overlay> overlays = map.getOverlays();
        stationsOverlay = new StationsOverlay(this, map);

        // locate to last location, before adding onLocationChanged listener
        initializeMapLocation();
        setMapSettings();

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

        busesOverlay = new BusesOverlay(this, map);

        overlays.add(myLocationOverlay);
        overlays.add(stationsOverlay);
        overlays.add(busesOverlay);

        notifyForChangesInNewVersions();

        selectStartupScreen();
        
        new CheckForUpdateAsyncTask().execute();
    }

    private boolean isWifiConnected() {
        final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }

    private void saveMapLastLocation() {
        if (map == null) {
            return;
        }
        final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        final GeoPoint mapCenter = map.getMapCenter();
        editor.putInt(PREFERENCE_KEY_LAST_LOCATION_LATITUDE_E6, mapCenter.getLatitudeE6());
        editor.putInt(PREFERENCE_KEY_LAST_LOCATION_LONGITUDE_E6, mapCenter.getLongitudeE6());
        editor.putInt(PREFERENCE_KEY_LAST_LOCATION_ZOOM, map.getZoomLevel());
        editor.commit();
    }

    private void initializeMapLocation() {

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        // locate in Sofia if not set already
        final int lat = settings.getInt(PREFERENCE_KEY_LAST_LOCATION_LATITUDE_E6, LOCATION_SOFIA_LATITUDE_E6);
        final int lon = settings.getInt(PREFERENCE_KEY_LAST_LOCATION_LONGITUDE_E6, LOCATION_SOFIA_LONGITUDE_E6);
        final int zoom = settings.getInt(PREFERENCE_KEY_LAST_LOCATION_ZOOM, ZOOM_DEFAULT);

        final GeoPoint location = new GeoPoint(lat, lon);
        map.getController().animateTo(location);
        map.getController().setZoom(zoom);

        stationsOverlay.placeStations(MapHelper.toCoordinate(location.getLatitudeE6()),
                MapHelper.toCoordinate(location.getLongitudeE6()), false);
    }

    private void notifyForChangesInNewVersions() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_10, PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_10)) {
            final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            // show only first time:
            editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_10, false);
            editor.commit();

            new AlertDialog.Builder(this).setTitle(R.string.versionChanges_1_10_startupScreen_title)
                    .setCancelable(true).setMessage(R.string.versionChanges_1_10_startupScreen_text)
                    .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }

        if (settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_17, PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_17)) {
            final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            // show only first time:
            editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_17, false);
            editor.commit();

            new AlertDialog.Builder(this).setTitle(R.string.versionChanges_1_17_startupScreen_title)
                    .setCancelable(true).setMessage(R.string.versionChanges_1_17_startupScreen_text)
                    .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }

        if (settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_20, PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_20)) {
            final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            // show only first time:
            editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_20, false);
            editor.commit();
            final TextView message = new TextView(this);
            message.setPadding(5, 5, 5, 5);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            message.setText(Html.fromHtml(getResources().getString(R.string.versionChanges_1_20_startupScreen_text)));

            new AlertDialog.Builder(this).setTitle(R.string.versionChanges_1_20_startupScreen_title)
                    .setCancelable(true).setView(message)
                    .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }

    }

    private void selectStartupScreen() {
        if (showStation(getIntent(), true)) {
            return;
        }
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean startupScreenFavorities = settings.getBoolean(PREFERENCE_KEY_STARTUP_SCREEN_FAVORITIES,
                PREFERENCE_DEFAULT_VALUE_STARTUP_SCREEN_FAVORITIES);
        if (startupScreenFavorities) {
            navigateToFavorities();
        }
    }

    private void disableLocationUpdates() {
        if (myLocationOverlay != null) {
            myLocationOverlay.disableMyLocation();
            myLocationOverlay.disableCompass();
        }
        stopTimer();
    }
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void enableLocationUpdates() {
        if (myLocationOverlay != null && !estimatesDialogVisible) {
            myLocationOverlay.enableMyLocation();
            setCompassSettings();
            //TODO very bad code, but there is no time:
            stopTimer();
            if (stationsOverlay != null && stationsOverlay.getShowBusesOverlayItem() != null) {
                timer = new Timer();
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (stationsOverlay == null || stationsOverlay.getShowBusesOverlayItem() == null) {
                                    stopTimer();
                                    return;
                                }
                                stationsOverlay.showBuses();
                            }
                        });
                    }
                }, 1000, 1000);
            }
        }
    }

    public void estimatesDialogDisplayed() {
        estimatesDialogVisible = true;
        disableLocationUpdates();
    }

    public void estimatesDialogClosed() {
        estimatesDialogVisible = false;
        enableLocationUpdates();
    }

    @Override
    protected void onResume() {
        enableLocationUpdates();
        super.onResume();
    }

    @Override
    protected void onPause() {
        disableLocationUpdates();
        saveMapLastLocation();
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

    private void enableDisableStatistics() {
        final boolean disable = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                PREFERENCE_KEY_STATISTICS_DISABLE, PREFERENCE_DEFAULT_VALUE_STATISTICS_DISABLE);
        GoogleAnalytics.getInstance(getApplicationContext()).setAppOptOut(disable);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_SETTINGS:
            if (!LocaleHelper.getUserLocale(this).equals(userLocale)) {
                restartActivity();
                return;
            }
            enableDisableStatistics();
            setMapSettings();
            break;
        case REQUEST_CODE_FAVORITIES:
            if (resultCode != RESULT_OK) {
                return;
            }
            showStation(data, false);
            break;

        default:
            break;
        }
    }

    private boolean showStation(Intent data, boolean optionalParameters) {
        final String code = data.getStringExtra(FavoritiesActivity.EXTRA_CODE_NAME);
        final String provider = data.getStringExtra(FavoritiesActivity.EXTRA_PROVIDER_NAME);
        if (code == null || provider == null) {
            if (optionalParameters) {
                return false;
            }
            throw new IllegalStateException("No code provided");
        }
        map.getController().setZoom(ZOOM_DEFAULT);

        stationsOverlay.showStation(provider, code, true);
        return true;
    }

    private void restartActivity() {
        Toast.makeText(this, R.string.settings_changeLocale_restart, Toast.LENGTH_LONG).show();

        final Intent intent = getIntent();
        // overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();

        // overridePendingTransition(0, 0);
        startActivity(intent);
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
        case R.id.menu_tixbg:
            navigateToTixBg();
            break;
        case R.id.menu_searchByBusStopId:
            askForBusStopId();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void askForBusStopId() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER /* | InputType.TYPE_NUMBER_VARIATION_NORMAL */);

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(input);

        final RadioGroup group = new RadioGroup(this);
        linearLayout.addView(group);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String selectedProvider = preferences.getString(PREFERENCE_KEY_DEFAULT_PROVIDER,
                FavoritiesService.PROVIDER_SOFIATRAFFIC);

        final List<RadioButton> radios = new LinkedList<RadioButton>();
        for (String provider : InitStations.PROVIDERS) {
            final RadioButton radio = new RadioButton(this);
            radio.setText(provider);
            radio.setId(radios.size());
            if (selectedProvider.equals(provider)) {
                radio.setChecked(true);
            }
            radios.add(radio);
            group.addView(radio);
        }

        final ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout);

        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.searchByBusStopId_dialogTitle)
                .setMessage(R.string.searchByBusStopId_dialogContent).setView(scrollView)
                .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        final String defaultProvider = saveDefaultProvider(preferences, radios);

                        String value = input.getText().toString();
                        while (value.startsWith("0")) {
                            value = value.substring(1);
                        }
                        if (value.length() == 0 || value.replaceAll("\\d+", "").length() > 0) {
                            Toast.makeText(LocationView.this, R.string.searchByBusStopId_dialog_badBusStopID,
                                    Toast.LENGTH_LONG).show();
                            askForBusStopId();
                        } else {
                            stationsOverlay.showStation(defaultProvider, value, true);
                        }
                    }
                }).create();
        input.requestFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    protected String saveDefaultProvider(SharedPreferences preferences, List<RadioButton> radios) {
        for (RadioButton radioButton : radios) {
            if (radioButton.isChecked()) {
                final Editor editor = preferences.edit();
                editor.putString(PREFERENCE_KEY_DEFAULT_PROVIDER, radioButton.getText().toString());
                editor.commit();
                return radioButton.getText().toString();
            }
        }
        throw new IllegalStateException("No provider selected");
    }

    private void navigateToTixBg() {
        try {
            final Intent toLaunch = getPackageManager().getLaunchIntentForPackage(PACKAGE_TIX_BG);
            if (toLaunch == null) {
                installTixBg();
                return;
            }
            startActivity(toLaunch);
            Toast.makeText(this, R.string.tixbg_info_return, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            installTixBg();
        }

    }

    private void installTixBg() {
        Toast.makeText(this, R.string.tixbg_install, Toast.LENGTH_LONG).show();

        final Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_TIX_BG));
        startActivity(goToMarket);
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
            progressQueryStations.setCancelable(true);
            progressQueryStations.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    fetchingCancelled = true;
                }
            });
            fetchingCancelled = false;
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

    @Override
    protected void onStart() {
        super.onStart();
        enableDisableStatistics();
        EasyTracker.getInstance(this).activityStart(this);
        
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                PREFERENCE_KEY_STATISTICS_DISABLE, PREFERENCE_DEFAULT_VALUE_STATISTICS_DISABLE)) {
            FlurryAgent.onStartSession(this, FLIRRY_ID);
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
        FlurryAgent.onEndSession(this);
    }
    
    public BusesOverlay getBusesOverlay() {
        return busesOverlay;
    }

    public boolean isFetchingCancelled() {
        return fetchingCancelled;
    }
}
