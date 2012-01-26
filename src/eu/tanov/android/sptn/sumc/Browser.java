package eu.tanov.android.sptn.sumc;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.util.Log;

public class Browser {

    private static final String TAG = "SimpleBrowser";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1017.2 Safari/535.19";
    private static final String REFERER = "http://m.sofiatraffic.bg/vt/";

    /**
     * q=000000 in order to find only by ID (we expect that there is no label 000000)
     */
    private static final String FORMAT_URL = "http://m.sofiatraffic.bg/vt?q=000000%s&o=1&go=1";

    private ResponseHandler<String> responseHandler;

    public void setResponseHandler(ResponseHandler<String> responseHandler) {
        this.responseHandler = responseHandler;
    }

    public String queryStation(String stationCode) {
        // XXX do not create client every time, use HTTP1.1 keep-alive!
        final HttpClient client = new DefaultHttpClient();

        final HttpGet request = createRequest(stationCode);

        // Create a response handler
        if (responseHandler == null) {
            responseHandler = new BasicResponseHandler();
        }
        String result = null;
        try {
            result = client.execute(request, responseHandler);
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for station " + stationCode, e);
        }

        // XXX do not create client every time, use HTTP1.1 keep-alive!
        client.getConnectionManager().shutdown();

        return result;
    }

    private static HttpGet createRequest(String stationCode) {
        final HttpGet result = new HttpGet(String.format(FORMAT_URL, stationCode));
        result.addHeader("User-Agent", USER_AGENT);
        result.addHeader("Referer", REFERER);
        result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        return result;
    }
}
