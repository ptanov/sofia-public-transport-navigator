package eu.tanov.android.sptn.sumc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import eu.tanov.android.sptn.R;

//FIXME very very bad code, but no time...
public class Browser {

    private static final String CAPTCHA_START = "<img src=\"/captcha/";
    private static final char CAPTCHA_END = '"';
    private static final String QUERY_BUS_STOP_ID = "q";
    private static final String QUERY_O = "o";
    private static final String QUERY_GO = "go";
    private static final String QUERY_CAPTCHA_TEXT = "sc";
    private static final String QUERY_CAPTCHA_ID = "poleicngi";

    private static final String TAG = "SimpleBrowser";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1017.2 Safari/535.19";
    private static final String REFERER = "http://m.sofiatraffic.bg/vt/";

    /**
     * q=000000 in order to find only by ID (we expect that there is no label 000000)
     */
    private static final String URL = "http://m.sofiatraffic.bg/vt";
    private static final String REQUIRES_CAPTCHA = "Въведете символите от изображението";
    private static final String CAPTCHA_IMAGE = "http://m.sofiatraffic.bg/captcha/%s";

    private static final Object wait = new int[0];
    private static final String SHARED_PREFERENCES_NAME_SUMC_COOKIES = "sumc_cookies";
    private static final String PREFERENCES_COOKIE_NAME = "name";
    private static final String PREFERENCES_COOKIE_DOMAIN = "domain";
    private static final String PREFERENCES_COOKIE_PATH = "path";
    private static final String PREFERENCES_COOKIE_VALUE = "value";
    private static String result = null;

    public String queryStation(Context context, Handler uiHandler, String stationCode) {
        // XXX do not create client every time, use HTTP1.1 keep-alive!
        final DefaultHttpClient client = new DefaultHttpClient();

        loadCookiesFromPreferences(context, client);
        // Create a response handler
        String result = null;
        try {
            do {
                final HttpPost request = createRequest(context, uiHandler, client, stationCode, result);
                result = client.execute(request, new BasicResponseHandler());
                saveCookiesToPreferences(context, client);
            } while (result.contains(REQUIRES_CAPTCHA));
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for station " + stationCode, e);
        } finally {
            // XXX do not create client every time, use HTTP1.1 keep-alive!
            client.getConnectionManager().shutdown();
        }

        return result;
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

    private static HttpPost createRequest(Context context, Handler uiHandler, HttpClient client, String stationCode,
            String previous) {
        try {
            String captchaText = null;
            String captchaId = null;

            if (previous != null) {
                captchaId = getCaptchaId(previous);
                if (captchaId != null) {
                    final Bitmap captchaImage = getCaptchaImage(client, captchaId);
                    if (captchaImage != null) {
                        captchaText = getCaptchaText(context, uiHandler, captchaImage);
                    }
                }
            }
            // final String parametersResult = client.execute(parametersRequest, responseHandler);
            // final int busStopIdStart = parametersResult.indexOf(START_QUERY_BUS_STOP_ID);
            // final String queryBusStopId = getAttributeValue(parametersResult,
            // busStopIdStart + START_QUERY_BUS_STOP_ID.length());
            // final int oStart = parametersResult.indexOf(START_QUERY_O,
            // busStopIdStart + START_QUERY_BUS_STOP_ID.length());
            // final String queryO = getAttributeValue(parametersResult, oStart + START_QUERY_O.length());
            // final String queryGo = getAttributeValue(parametersResult,
            // parametersResult.indexOf(START_QUERY_GO, oStart + START_QUERY_O.length()) + START_QUERY_GO.length());
            return createRequest(stationCode, captchaText, captchaId);
        } catch (Exception e) {
            Log.e(TAG, "Could not get data for parameters for station: " + stationCode, e);
            return null;
        }
    }

    private static String getCaptchaText(final Context context, Handler uiHandler, final Bitmap captchaImage) {

        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                final Builder dialogBuilder = new AlertDialog.Builder(context);
                dialogBuilder.setTitle(R.string.captcha_dialog_title);
                final LinearLayout panel = new LinearLayout(context);
                panel.setOrientation(LinearLayout.VERTICAL);
                final TextView label = new TextView(context);
                label.setId(1);
                label.setText(R.string.captcha_dialog_label);
                panel.addView(label);

                final ImageView image = new ImageView(context);
                image.setId(3);
                image.setImageBitmap(captchaImage);
                panel.addView(image);

                final EditText input = new EditText(context);
                input.setId(2);
                input.setSingleLine();
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI
                        | InputType.TYPE_TEXT_VARIATION_PHONETIC);
                final ScrollView view = new ScrollView(context);
                panel.addView(input);
                view.addView(panel);

                dialogBuilder.setCancelable(true)
                        .setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                result = input.getText().toString();

                                synchronized (wait) {
                                    wait.notifyAll();
                                }

                                dialog.dismiss();
                            }
                        }).setView(view);

                dialogBuilder.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        result = null;
                        synchronized (wait) {
                            wait.notifyAll();
                        }
                    }
                });
                dialogBuilder.create().show();
            }
        });

        return waitForResult();
    }

    private static String waitForResult() {
        String localResult = null;
        // TODO very very bad code, but no time...
        try {
            synchronized (wait) {
                wait.wait();
            }
            localResult = result;
            result = null;
            if (localResult == null) {
                // user is requesting cancel
                throw new RuntimeException("Cancelled by user");
            }
            return localResult;
        } catch (InterruptedException e) {
            localResult = result;
            result = null;

            if (localResult == null) {
                // user is requesting cancel
                throw new RuntimeException("Cancelled by user");
            }

            return localResult;
        }
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

    private static HttpPost createRequest(String stationCode, String captchaText, String captchaId) {
        final HttpPost result = new HttpPost(URL);
        result.addHeader("User-Agent", USER_AGENT);
        result.addHeader("Referer", REFERER);
        // Issue 85:
        // result.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, true);
        try {
            final UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
                    parameters(stationCode, captchaText, captchaId));
            result.setEntity(entity);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Not supported default encoding?", e);
        }

        return result;
    }

    private static List<BasicNameValuePair> parameters(String stationCode, String captchaText, String captchaId) {
        final List<BasicNameValuePair> result = new ArrayList<BasicNameValuePair>(5);
        result.addAll(Arrays.asList(new BasicNameValuePair(QUERY_BUS_STOP_ID, "000000" + stationCode),
                new BasicNameValuePair(QUERY_O, "1"), new BasicNameValuePair(QUERY_GO, "1")));

        if (captchaText != null && captchaId != null) {
            result.add(new BasicNameValuePair(QUERY_CAPTCHA_ID, captchaId));
            result.add(new BasicNameValuePair(QUERY_CAPTCHA_TEXT, captchaText));
        }
        return result;
    }
}
