package eu.tanov.android.sptn.sumc;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.util.Log;

public class Browser {

    private static final String START_QUERY_BUS_STOP_ID = "<input type=\"text\"  value=\"\"  name=\"";
    private static final String START_QUERY_O = "<input type=\"hidden\" name=\"";
    private static final String START_QUERY_GO = "<input type=\"hidden\" value=\"1\" name=\"";

    private static final String TAG = "SimpleBrowser";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1017.2 Safari/535.19";
    private static final String REFERER = "http://m.sofiatraffic.bg/vt/";

    /**
     * q=000000 in order to find only by ID (we expect that there is no label 000000)
     */
    private static final String URL = "http://m.sofiatraffic.bg/vt";

    private ResponseHandler<String> responseHandler;

    public void setResponseHandler(ResponseHandler<String> responseHandler) {
        this.responseHandler = responseHandler;
    }

    public String queryStation(String stationCode) {
        // XXX do not create client every time, use HTTP1.1 keep-alive!
        final HttpClient client = new DefaultHttpClient();

        // Create a response handler
        if (responseHandler == null) {
            responseHandler = new BasicResponseHandler();
        }
        final HttpPost request = createRequest(client, responseHandler, stationCode);

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

    private static HttpPost createRequest(HttpClient client, ResponseHandler<String> responseHandler, String stationCode) {
        final HttpGet parametersRequest = createParametersRequest(stationCode);
        try {
            final String parametersResult = client.execute(parametersRequest, responseHandler);
            final int busStopIdStart = parametersResult.indexOf(START_QUERY_BUS_STOP_ID);
            final String queryBusStopId = getAttributeValue(parametersResult,
                    busStopIdStart + START_QUERY_BUS_STOP_ID.length());
            final int oStart = parametersResult.indexOf(START_QUERY_O,
                    busStopIdStart + START_QUERY_BUS_STOP_ID.length());
            final String queryO = getAttributeValue(parametersResult, oStart + START_QUERY_O.length());
            final String queryGo = getAttributeValue(parametersResult,
                    parametersResult.indexOf(START_QUERY_GO, oStart + START_QUERY_O.length()) + START_QUERY_GO.length());
            return createRequest(stationCode, queryBusStopId, queryO, queryGo);
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for parameters for station: " + stationCode, e);
            return null;
        }
    }

    private static String getAttributeValue(String parametersResult, int busStopIdStart) {
        return parametersResult.substring(busStopIdStart, parametersResult.indexOf("\"", busStopIdStart));
    }

    private static HttpGet createParametersRequest(String stationCode) {
        final HttpGet result = new HttpGet(URL);
        result.addHeader("User-Agent", USER_AGENT);
        result.addHeader("Referer", REFERER);
        result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);

        return result;
    }

    private static HttpPost createRequest(String stationCode, String queryBusStopId, String queryO, String queryGo) {
        final HttpPost result = new HttpPost(URL);
        result.addHeader("User-Agent", USER_AGENT);
        result.addHeader("Referer", REFERER);
        result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        try {
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(Arrays.asList(new BasicNameValuePair(
                    queryBusStopId,
                    "000000" + stationCode), new BasicNameValuePair(queryO, "1"), new BasicNameValuePair(queryGo, "1")));
            result.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not supported default encoding?", e);
        }

        return result;
    }
}
