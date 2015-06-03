package eu.tanov.android.bptcommon.utils;

import java.util.Collections;

import android.app.Activity;
import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import eu.tanov.android.bptcommon.favorities.FavoritiesService;

public class ActivityTracker {
    private ActivityTracker() {}

    public static void errorStation(Context context, String busStopSource, String stationCode) {
        if (FavoritiesService.PROVIDER_SOFIATRAFFIC.equals(busStopSource)) {
            errorSofia(context, stationCode);
        }
        if (FavoritiesService.PROVIDER_VARNATRAFFIC.equals(busStopSource)) {
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
    public static void sofiaCaptchaBackgroundError(Activity context, String stationCode) {
        FlurryAgent.logEvent("sofiaCaptchaBackgroundError", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "sofia",
                         "sofiaCaptchaBackgroundError"+stationCode,
                         null)
            .build()
        );
    }

    public static void queriedSofiaNoInfo(Context context, String stationCode) {
        FlurryAgent.logEvent("queriedSofiaNoInfo", Collections.singletonMap("stationCode", stationCode));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "queriedSofiaNoInfo",
                         stationCode,
                         null)
            .build()
        );
    }

    public static void busStopsUpdateCancel(Context context) {
        FlurryAgent.logEvent("busStopsUpdateCancel");
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopupdate",
                         "busStopsUpdateCancel",
                         "busStopsUpdateCancel",
                         null)
            .build()
        );
        
    }

    public static void busStopsUpdatedSuccess(Context context) {
        FlurryAgent.logEvent("busStopsUpdatedSuccess");
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopupdate",
                         "busStopsUpdatedSuccess",
                         "busStopsUpdatedSuccess",
                         null)
            .build()
        );
        
    }

    public static void busStopsUpdatedError(Context context) {
        FlurryAgent.logEvent("busStopsUpdatedError");
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopupdate",
                         "busStopsUpdatedError",
                         "busStopsUpdatedError",
                         null)
            .build()
        );
        
    }

    public static void couldNotScaleImage(Context context, String message) {
        FlurryAgent.logEvent("couldNotScaleImage", Collections.singletonMap("message", message));
        final EasyTracker easyTracker = EasyTracker.getInstance(context);
        if (easyTracker == null) {
            return;
        }
        easyTracker.send(MapBuilder.createEvent("busstopretrieving",
                         "couldNotScaleImage",
                         message,
                         null)
            .build()
        );
    }

}
