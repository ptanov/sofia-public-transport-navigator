package eu.tanov.android.bptcommon;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.tanov.android.bptcommon.utils.ActivityTracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;

import static android.R.attr.direction;
import static android.R.attr.name;
import static android.R.attr.type;

//FIXME very very bad code, but no time...
public class Browser {

    private static final Map<Integer, String> TYPE_TO_SCHEDULE = new HashMap<Integer, String>();
    {
        TYPE_TO_SCHEDULE.put(1, "autobus");
        TYPE_TO_SCHEDULE.put(2, "trolleybus");
        TYPE_TO_SCHEDULE.put(3, "tramway");
    }

    private static class InputData {
        private final String name;
        private final String value;
        private final String type;
        public InputData(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type == null?null:type.toLowerCase(Locale.US);
        }
    }
    protected enum VechileType {
        TRAM, BUS, TROLLEY
    }
    private static final String CAPTCHA_START = "<img src=\"/captcha/";
    private static final char CAPTCHA_END = '"';
    private static final String QUERY_BUS_STOP_ID = "stopCode";
    private static final String QUERY_O = "o";
    private static final String QUERY_GO = "go";

    private static final String TAG = "SimpleBrowser";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1017.2 Safari/535.19";
    private static final String REFERER = "http://m.sofiatraffic.bg/vt/";

    /**
     * q=000000 in order to find only by ID (we expect that there is no label 000000)
     */
    private static final String URL = "http://drone.sumc.bg/api/v1/timing";
    private static final String HAS_RESULT = "Информация към";
    private static final String NO_INFO = "no data";
    private static final String CAPTCHA_IMAGE = "http://m.sofiatraffic.bg/captcha/%s";
    private static final String FORM_INPUT = "<input";
    private static final String FORM_INPUT_NAME = "name=";
    private static final String FORM_INPUT_TYPE = "type=";
    private static final String FORM_INPUT_VALUE = "value=";

    private static final String SHARED_PREFERENCES_NAME_SUMC_COOKIES = "sumc_cookies";
    private static final String PREFERENCES_COOKIE_NAME = "name";
    private static final String PREFERENCES_COOKIE_DOMAIN = "domain";
    private static final String PREFERENCES_COOKIE_PATH = "path";
    private static final String PREFERENCES_COOKIE_VALUE = "value";
    private static final String VECHILE_TYPE_PARAMETER_NAME = "vehicleTypeId";
    private String previousResponse;
    private final int error_retrieveEstimates_matching_noInfo;
    
    public Browser(int error_retrieveEstimates_matching_noInfo) {
        this.error_retrieveEstimates_matching_noInfo = error_retrieveEstimates_matching_noInfo;
    }

    public String queryStation(Context context, Handler uiHandler, String stationCode) {
        // XXX do not create client every time, use HTTP1.1 keep-alive!
        final DefaultHttpClient client = new DefaultHttpClient();

        loadCookiesFromPreferences(context, client);
        // Create a response handler
        String result = null;
        try {
            final HttpPost request = createRequest(context, uiHandler, client, stationCode);
            result = client.execute(request, new BasicResponseHandler());
            saveCookiesToPreferences(context, client);

            if (result.contains(NO_INFO)) {
                result = context.getResources().getString(error_retrieveEstimates_matching_noInfo);
            } else {
                result = convertToOldFormat(stationCode, result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for station " + stationCode, e);
        } finally {
            // XXX do not create client every time, use HTTP1.1 keep-alive!
            client.getConnectionManager().shutdown();
        }

        return result;
    }
    public static String getHTML(String urlToRead) throws IOException {
        // based on https://stackoverflow.com/questions/1485708/how-do-i-do-a-http-get-in-java
        StringBuilder result = new StringBuilder();
        java.net.URL url = new java.net.URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }


    private String convertToOldFormat(String stationCode, String json) throws JSONException {
        String url = toSchedulesUrl1(stationCode);
        final JSONArray responses = new JSONArray(json);
        final StringBuilder result = new StringBuilder();
        result.append("<html><body><div class=\"arrivals\">");
        result.append("<table>");
        for (int i=0; i < responses.length(); i++) {
            final JSONObject response = responses.getJSONObject(i);




                    result.append("<tr><td>" +

                            "<div class=\"arr_info_"+response.getInt("type")+"\">"+
                            "<a href=\""+url+"\">"+"<b>"+response.getString("lineName")+"</b>"+"</a>&nbsp;-&nbsp;"+response.getString("timing")+"<br />"+
"</div>"
+
                            "</td></tr>");

        }
        result.append("</table>");
        result.append("\n</div></body></html>");
        return result.toString();
    }

    private String toSchedulesUrl(String stationCode, int type, String name, String direction) {
        return "/"+TYPE_TO_SCHEDULE.get(type)+"/"+name+"#sign/"+direction+"/"+toSumcCode(stationCode);
    }
    private String toSchedulesUrl1(String stationCode) {
        try {
    stationCode =toSumcCode(stationCode);
            String json = getHTML("https://schedules.sofiatraffic.bg/server/data/stop_sign_find/" + stationCode);
            final JSONObject responses = new JSONObject(json);
            return "/stop/"+ responses.getString("stop_id")+"/"+responses.getString("url_name")+"#"+stationCode;
        } catch (Exception e) {
            return "";
        }
    }


    private void saveCookiesToPreferences(Context context, DefaultHttpClient client) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME_SUMC_COOKIES,
                Context.MODE_PRIVATE);
        final Editor edit = sharedPreferences.edit();
        edit.clear();

        int i = 0;
        for (Cookie cookie : client.getCookieStore().getCookies()) {
            edit.putString(PREFERENCES_COOKIE_NAME + i, cookie.getName());
            edit.putString(PREFERENCES_COOKIE_VALUE + i, cookie.getValue());
            edit.putString(PREFERENCES_COOKIE_DOMAIN + i, cookie.getDomain());
            edit.putString(PREFERENCES_COOKIE_PATH + i, cookie.getPath());
            i++;
        }
        edit.commit();
    }

    private void loadCookiesFromPreferences(Context context, DefaultHttpClient client) {
        final CookieStore cookieStore = client.getCookieStore();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME_SUMC_COOKIES,
                Context.MODE_PRIVATE);

        int i = 0;
        while (sharedPreferences.contains(PREFERENCES_COOKIE_NAME + i)) {
            final String name = sharedPreferences.getString(PREFERENCES_COOKIE_NAME + i, null);
            final String value = sharedPreferences.getString(PREFERENCES_COOKIE_VALUE + i, null);
            final BasicClientCookie result = new BasicClientCookie(name, value);

            result.setDomain(sharedPreferences.getString(PREFERENCES_COOKIE_DOMAIN + i, null));
            result.setPath(sharedPreferences.getString(PREFERENCES_COOKIE_PATH + i, null));
            cookieStore.addCookie(result);
            i++;
        }
    }

    private HttpPost createRequest(Context context, Handler uiHandler, HttpClient client, String stationCode) {
        try {
            return createRequest(stationCode);
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for parameters for station: " + stationCode, e);
            return null;
        }
    }

    protected String getCaptchaText(Activity context, Handler uiHandler, Bitmap captchaImage, String stationCode) {
        ActivityTracker.sofiaCaptchaBackgroundError(context, stationCode);
        throw new IllegalStateException("captcha in background");
    }


    private static Bitmap getCaptchaImage(HttpClient client, String captchaId) throws ClientProtocolException,
            IOException {
        final HttpGet request = new HttpGet(String.format(CAPTCHA_IMAGE, captchaId));
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Referer", REFERER);
        request.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);

        final HttpResponse response = client.execute(request);

        final HttpEntity entity = response.getEntity();
        final InputStream in = entity.getContent();

        int next;
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((next = in.read()) != -1) {
            bos.write(next);
        }
        bos.flush();
        byte[] result = bos.toByteArray();

        bos.close();

        entity.consumeContent();
        return BitmapFactory.decodeByteArray(result, 0, result.length);
    }

    private static String getCaptchaId(String previous) {
        final int captchaStart = previous.indexOf(CAPTCHA_START);
        if (captchaStart == -1) {
            return null;
        }
        final int captchaEnd = previous.indexOf(CAPTCHA_END, captchaStart + CAPTCHA_START.length());
        if (captchaEnd == -1) {
            return null;
        }

        return previous.substring(captchaStart + CAPTCHA_START.length(), captchaEnd);
    }

    private static HttpPost createRequest(String stationCode) {
        stationCode = toSumcCode(stationCode);

        final HttpPost result = new HttpPost(URL);
        result.addHeader("User-Agent", USER_AGENT);
//        result.addHeader("Referer", REFERER);
        // Issue 85:
        // result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
        try {

    // poor's man solution to create json :) (but... no time)
            final StringEntity params =new StringEntity("{\"stopCode\":\"" +
                    stationCode +
                    "\"}");
            result.addHeader("content-type", "application/json");
            result.setEntity(params);

        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not supported default encoding?", e);
        }

        return result;
    }
    private static BasicNameValuePair createParameter(Map<String, String> parametersMapping, String key, String value) {
        String mappedKey = parametersMapping.get(key);
        if (mappedKey == null) {
            mappedKey = key;
        }
        return new BasicNameValuePair(mappedKey, value);
    }
    private static List<BasicNameValuePair> parameters(String stationCode, String captchaText, String captchaId, VechileType type, String previous) {
        final List<BasicNameValuePair> result = new ArrayList<BasicNameValuePair>(6);
        if (previous != null) {
            final Map<String, String> parametersMapping = getParametersMapping(previous, stationCode, captchaText, captchaId);

            parametersMapping.put("submit", "Провери");
            for (Entry<String, String> next : parametersMapping.entrySet()) {
                result.add(new BasicNameValuePair(next.getKey(), next.getValue()));
            }
            if (type != null) {
                result.add(createParameter(parametersMapping, VECHILE_TYPE_PARAMETER_NAME, Integer.toString(type.ordinal())));
            }

        }
        return result;
    }

    private static String getForm(String previous) {
        final int formStart = previous.indexOf("<form");
        if (formStart == -1) {
            return null;
        }
        final int formEnd = previous.indexOf("</form", formStart);
        if (formEnd == -1) {
            return null;
        }
        return previous.substring(formStart, formEnd);
    }
    private static String getParameterValue(String form, int index, int length) {
        final int start = index + length;
        if ((index == -1) || (start+1 >= form.length())) {
            return "";
        }
        final char startChar = form.charAt(start);
        int end = form.indexOf(startChar, start+1);
        if (end == -1) {
            return "";
        }
        return form.substring(start+1, end);
    }

    private static InputData getInput(String form, int index) {
        if (index == -1) {
            return null;
        }
        final int end = form.indexOf(">", index);
        if (end == -1) {
            return null;
        }
        final int nameIndex = form.indexOf(FORM_INPUT_NAME, index);
        if (nameIndex == -1 || nameIndex >= end) {
            return null;
        }
        final int typeIndex = form.indexOf(FORM_INPUT_TYPE, index);
        final int valueIndex = form.indexOf(FORM_INPUT_VALUE, index);
        
        return new InputData(getParameterValue(form, nameIndex, FORM_INPUT_NAME.length()),
                getParameterValue(form, valueIndex>=end?-1:valueIndex, FORM_INPUT_VALUE.length()),
                getParameterValue(form, typeIndex>=end?-1:typeIndex, FORM_INPUT_TYPE.length()));
    }
    private static boolean isCaptchaText(Map<String, String> result, InputData input, boolean isCodeFound) {
        if ("text".equals(input.type) && isCodeFound) {
            return true;
        }
        return false;
    }
    private static boolean isStationCode(Map<String, String> result, InputData input, boolean isCodeFound) {
        if ("text".equals(input.type) && !isCodeFound) {
            return true;
        }
        return false;
    }
    /**
     * Very bad code (this method and all called methods), but no time... 
     */
    private static Map<String, String> getParametersMapping(String previous, String stationCode, String captchaText, String captchaId) {
        boolean isCodeFound = false;
        final Map<String, String> result = new HashMap<String, String>();
        final String form = getForm(previous);
        if (form != null) {
            int indexOf = 0;
            while(indexOf != -1) {
                indexOf = form.indexOf(FORM_INPUT, indexOf + 1);
                final InputData input = getInput(form, indexOf);
                if (input == null) {
                    continue;
                }
                if ("hidden".equals(input.type)) {
                    result.put(input.name, input.value);
                } else if (isCaptchaText(result, input, isCodeFound)) {
                    result.put(input.name, captchaText);
                } else if (isStationCode(result, input, isCodeFound)) {
                    isCodeFound = true;
                    result.put(input.name, stationCode);
                }
            }
        }
        return result;
    }

    private static String toSumcCode(String stationCode) {
        stationCode = "000000" + stationCode;
        return stationCode.substring(stationCode.length() - 4);
    }
}
