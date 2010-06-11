package eu.tanov.android.sptn.sumc;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.util.Log;

public class Browser {
	private static final String TAG = "SimpleBrowser";

	private static final String PARAMETER_STATION_CODE = "stopCode";
	private static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/533.8 (KHTML, like Gecko) Chrome/5.0.396.0 Safari/533.8";
	private static final String URL = "http://m.sumc.bg/vt";

	private ResponseHandler<String> responseHandler;
	
	public void setResponseHandler(ResponseHandler<String> responseHandler) {
		this.responseHandler = responseHandler;
	}
	public String queryStation(String stationCode) {
		//XXX do not create client every time, use HTTP1.1 keep-alive!
        final HttpClient client = new DefaultHttpClient();

        final HttpPost request = createRequest(stationCode);
        
        // Create a response handler
        if (responseHandler == null) {
        	responseHandler = new BasicResponseHandler();
        }
        String result = null;
		try {
			result = client.execute(request, responseHandler);
		} catch (Exception e) {
			Log.e(TAG, "Could not get data for station "+stationCode, e);
		}

		//XXX do not create client every time, use HTTP1.1 keep-alive!
        client.getConnectionManager().shutdown();
        
        return result;
	}

	private static HttpPost createRequest(String stationCode) {
		final HttpPost result = new HttpPost(URL);
        result.getParams().setParameter(CoreProtocolPNames.USER_AGENT, USER_AGENT);
        result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        
        try {
        	final List<BasicNameValuePair> parameters = Collections.singletonList(new BasicNameValuePair(PARAMETER_STATION_CODE, stationCode));
			result.setEntity(new UrlEncodedFormEntity(parameters));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Default encoding is unsupported", e);
		}
		return result;
	}
}
