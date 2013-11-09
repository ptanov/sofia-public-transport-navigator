package eu.tanov.android.sptn.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class BusStopUpdater {
    private static final String TAG = "BusStopUpdater";

    private static final String PREFERENCE_KEY_BUSSTOP_TAGS = "busStopCoordinatesTag";
    private static final String DOWNLOAD_URL_SOFIATRAFFIC = "https://sofia-public-transport-navigator.googlecode.com/hg/res/raw/coordinates.xml";
    private static final String FILENAME_SOFIATRAFFIC = "coordinates_sofiatraffic.xml";

    private static final String DOWNLOAD_URL_VARNATRAFFIC = "https://sofia-public-transport-navigator.googlecode.com/hg/res/raw/coordinates_varnatraffic.json";
    private static final String FILENAME_VARNATRAFFIC = "coordinates_varnatraffic.json";

    private final Context context;
    private static boolean updatingInProgress = false;

    public BusStopUpdater(Context context) {
        this.context = context;
    }

    public boolean update() {
        if (updatingInProgress) {
            Log.e(TAG, "update already in progress");
            return false;
        }
        updatingInProgress = true;
        final FileDownloader sofiaTrafficDownloader = new FileDownloader(context, DOWNLOAD_URL_SOFIATRAFFIC,
                FILENAME_SOFIATRAFFIC);
        sofiaTrafficDownloader.run();
        if (sofiaTrafficDownloader.getTag() == null) {
            Log.e(TAG, "could not get tag of sofiaTrafficDownloader");
            return false;
        }
        final FileDownloader varnaTrafficDownloader = new FileDownloader(context, DOWNLOAD_URL_VARNATRAFFIC,
                FILENAME_VARNATRAFFIC);
        varnaTrafficDownloader.run();
        if (varnaTrafficDownloader.getTag() == null) {
            Log.e(TAG, "could not get tag of varnaTrafficDownloader");
            return false;
        }
        boolean reloadBusStops;
        try {
            reloadBusStops = StationProvider.reloadBusStops(context, openFile(FILENAME_SOFIATRAFFIC),
                    openFile(FILENAME_VARNATRAFFIC));
        } catch (FileNotFoundException e) {
            reloadBusStops = false;
            Log.e(TAG, "file not found while updating bus stops", e);
        }
        getFile(FILENAME_SOFIATRAFFIC).delete();
        getFile(FILENAME_VARNATRAFFIC).delete();

        if (reloadBusStops) {
            setTags(sofiaTrafficDownloader.getTag(), varnaTrafficDownloader.getTag());
        }
        updatingInProgress = false;
        Log.i(TAG, "bus stops updated: " + reloadBusStops);
        return reloadBusStops;
    }

    private String generateAllTags(String sofiaTrafficTag, String varnaTrafficTag) {
        return sofiaTrafficTag + ":" + varnaTrafficTag;
    }

    private void setTags(String sofiaTrafficTag, String varnaTrafficTag) {
        final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(PREFERENCE_KEY_BUSSTOP_TAGS, generateAllTags(sofiaTrafficTag, varnaTrafficTag));
        editor.commit();
    }

    private String getTag(String url) throws MalformedURLException, IOException {
        final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setUseCaches(false);
        conn.setRequestMethod("HEAD");
        final String result = conn.getHeaderField(FileDownloader.HEADER_ETAG);
        conn.disconnect();
        return result;
    }

    public boolean isUpdateAvailable() {
        if (updatingInProgress) {
            return false;
        }
        try {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            final String previousTag = preferences.getString(PREFERENCE_KEY_BUSSTOP_TAGS, null);
            if (previousTag == null) {
                // init first time
                setTags(getTag(DOWNLOAD_URL_SOFIATRAFFIC), getTag(DOWNLOAD_URL_VARNATRAFFIC));
                return false;
            }
            return (!previousTag.equals(generateAllTags(getTag(DOWNLOAD_URL_SOFIATRAFFIC),
                    getTag(DOWNLOAD_URL_VARNATRAFFIC))));
        } catch (Exception e) {
            Log.e(TAG, "could not check for update", e);
            return false;
        }
    }

    private File getFile(String filename) {
        return new File(context.getFilesDir(), filename);
    }

    private InputStream openFile(String filename) throws FileNotFoundException {
        return new FileInputStream(getFile(filename));
    }


}
