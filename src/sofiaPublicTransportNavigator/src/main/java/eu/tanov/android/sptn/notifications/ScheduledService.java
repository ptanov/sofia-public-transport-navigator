package eu.tanov.android.sptn.notifications;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import eu.tanov.android.bptcommon.Browser;
import eu.tanov.android.bptcommon.EstimatesResolver;
import eu.tanov.android.bptcommon.SofiaTrafficHtmlResult;
import eu.tanov.android.bptcommon.VarnaTrafficHtmlResult;
import eu.tanov.android.bptcommon.favorities.FavoritiesService;
import eu.tanov.android.sptn.R;

public class ScheduledService extends IntentService {
    private static final String TAG = "ScheduledService";

    public ScheduledService() {
        super("ScheduledService");
    }
    

    @Override
    protected void onHandleIntent(Intent intent1) {
        String extracted = getText(intent1);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("My notification")
                .setContentText(extracted);

//        <action android:name="android.intent.action.VIEW" />
        
//        <category android:name="android.intent.category.DEFAULT" />
//        <data android:mimeType="eu.tanov.android.StationProvider/stations" />

        Intent resultIntent = new Intent();
        resultIntent.setAction(Intent.ACTION_VIEW);
        resultIntent.setType("eu.tanov.android.StationProvider/stations");
        resultIntent.putExtra("code", "1914");
        resultIntent.putExtra("provider", "sofiatraffic.bg");

        PendingIntent resultPendingIntent =
            PendingIntent.getActivity(
            this,
            0,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        );

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        int mNotificationId = 001;
        NotificationManager mNotifyMgr = 
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


    private EstimatesResolver createResolver(String busStopSource, String stationCode, String stationLabel) {
        if (FavoritiesService.PROVIDER_SOFIATRAFFIC.equals(busStopSource)) {
            return new SofiaTrafficHtmlResult(this, null, null, stationCode, stationLabel, new Browser(R.string.error_retrieveEstimates_matching_noInfo));
        }
        if (FavoritiesService.PROVIDER_VARNATRAFFIC.equals(busStopSource)) {
            return new VarnaTrafficHtmlResult(this, null, stationCode, stationLabel);
        }
        throw new IllegalStateException("Unknown source: " + busStopSource);

    }


    private String getText(Intent intent1) {
        
        final String busStopSource = intent1.getStringExtra("provider");
        final String stationCode = intent1.getStringExtra("code");
        String stationLabel = intent1.getStringExtra("label");
        if (stationLabel == null) {
            stationLabel = "";
        }
        try {
            final EstimatesResolver resolver = createResolver(busStopSource, stationCode, stationLabel);
            
            resolver.query();
            return resolver.getResultAsString();
        } catch (Exception e) {
            return "Error getting info for " + stationCode;
        }
    }

}
