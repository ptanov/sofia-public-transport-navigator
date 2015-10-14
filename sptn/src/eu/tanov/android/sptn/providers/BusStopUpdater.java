package eu.tanov.android.sptn.providers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class BusStopUpdater {
    private static final String TAG = "BusStopUpdater";

    private static final String PREFERENCE_KEY_BUSSTOP_TAGS = "busStopCoordinatesTag";
    private static final String DOWNLOAD_URL_SOFIATRAFFIC = "https://github.com/ptanov/sofia-public-transport-navigator/raw/master/sptn/res/raw/coordinates.xml";
    private static final String FILENAME_SOFIATRAFFIC = "coordinates_sofiatraffic.xml";

    private static final String DOWNLOAD_URL_VARNATRAFFIC = "https://github.com/ptanov/sofia-public-transport-navigator/raw/master/sptn/res/raw/coordinates_varnatraffic.json";
    private static final String FILENAME_VARNATRAFFIC = "coordinates_varnatraffic.json";
    
    private static final String ORIGINAL_TAGS_SOFIATRAFFIC = "\"a5e575ece60fd28a0af3403cf70e2fac3e543cdc\"";
    private static final String ORIGINAL_TAGS_VARNATRAFFIC = "\"332b4b22d19e58c5225f652f6b3a056acb21c6f5\"";

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
    private void allow(HttpsURLConnection conn) throws NoSuchAlgorithmException, KeyManagementException {
    	   final SSLContext context = SSLContext.getInstance("TLS");
           final TrustManager[] trustAllCerts = new TrustManager[]{
                   new X509TrustManager() {
                       public X509Certificate[] getAcceptedIssuers() {
                           return null;
                       }

                       public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                           return;
                       }

                       public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                           return;
                       }
                   }
           };

    	   context.init(null, trustAllCerts, null);

    	   conn.setSSLSocketFactory(context.getSocketFactory());
    	   conn.setHostnameVerifier(new HostnameVerifier() {
			
			@Override
			public boolean verify(String hostname, SSLSession session) {
				// TODO Auto-generated method stub
				return true;
			}
		});
    }

    private String getTag(String url) throws MalformedURLException, IOException, KeyManagementException, NoSuchAlgorithmException {
        final HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
//        allow(conn);
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
            final String previousTag = preferences.getString(PREFERENCE_KEY_BUSSTOP_TAGS,
                    generateAllTags(ORIGINAL_TAGS_SOFIATRAFFIC, ORIGINAL_TAGS_VARNATRAFFIC));
            final String currentSofiaTrafficTag = getTag(DOWNLOAD_URL_SOFIATRAFFIC);
            if (currentSofiaTrafficTag == null) {
                return false;
            }
            final String currentVarnaTrafficTag = getTag(DOWNLOAD_URL_VARNATRAFFIC);
            if (currentVarnaTrafficTag == null) {
                return false;
            }
            return (!previousTag.equals(generateAllTags(currentSofiaTrafficTag, currentVarnaTrafficTag)));
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

    public Context getContext() {
        return context;
    }

}
