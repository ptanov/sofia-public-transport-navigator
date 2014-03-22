package eu.tanov.android.sptn.util;

import java.util.Collections;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import eu.tanov.android.sptn.providers.InitStations;

public class ActivityTracker {
    private ActivityTracker() {}

    public static void errorStation(Context context, String busStopSource, String stationCode) {
        if (InitStations.PROVIDER_SOFIATRAFFIC.equals(busStopSource)) {
            errorSofia(context, stationCode);
        }
        if (InitStations.PROVIDER_VARNATRAFFIC.equals(busStopSource)) {
            errorVarna(context, stationCode);
        }
    }
    private static void errorVarna(Context context, String stationCode) {
        FlurryAgent.logEvent("errorVarna", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "errorVarna",
                         stationCode,
                         null)
            .build()
        );
    }
    private static void errorSofia(Context context, String stationCode) {
        FlurryAgent.logEvent("errorSofia", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "errorSofia",
                         stationCode,
                         null)
            .build()
        );
    }

    public static void queriedSofia(Context context, String stationCode) {
        FlurryAgent.logEvent("queriedSofia", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "queriedSofia",
                         stationCode,
                         null)
            .build()
        );

    }

    public static void queriedVarna(Context context, String stationCode) {
        FlurryAgent.logEvent("queriedVarna", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "queriedVarna",
                         stationCode,
                         null)
            .build()
        );
    }

    public static void sofiaCaptchaSuccess(Context context) {
        FlurryAgent.logEvent("sofiaCaptchaSuccess");
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "sofia",
                         "sofiaCaptchaSuccess",
                         null)
            .build()
        );

    }

    public static void sofiaCaptchaCancel(Context context) {
        FlurryAgent.logEvent("sofiaCaptchaCancel");
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "sofia",
                         "sofiaCaptchaCancel",
                         null)
            .build()
        );
    }

}
