package eu.tanov.android.sptn.sumc;

import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.tanov.android.sptn.LocationView;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.favorities.BusStopItem;
import eu.tanov.android.sptn.favorities.FavoritiesService;
import eu.tanov.android.sptn.map.StationsOverlay;

public class VarnaTrafficHtmlResult implements EstimatesResolver {
    private static final String TAG = "VarnaTrafficHtmlResult";

    private static final String ENCODING = "utf-8";
    private static final String MIME_TYPE = "text/html";

    private static final String STATION_URL = "http://varnatraffic.com/Ajax/FindStationDevices?stationId=";

    private static final String HTML_START = "<html>";
    private static final String HTML_END = "</html>";
    private static final String HTML_HEADER = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><style type=\"text/css\">body {font-family:arial, verdana, sans-serif;font-size:14pt;margin: 0;}.sep {clear: both;}.busStop {font-weight: bold;}.typeVehicle {font-weight:bold;padding-left:3px;} td.number {width:30px;text-align:right;}table {width:100%;}div.arr_info_1 td.number {background-color:#ea0000;}div.arr_info_2 td.number {background-color:#0066aa;}div.arr_info_3 td.number {background-color:#feab10;}.number a:link {color:#FFFFFF;font-weight: bold;}.number a:visited {color:#FFFFFF;font-weight: bold;}.number a:hover {color:#FFFFFF;font-weight: bold;}.number a:active {color:#FFFFFF;font-weight: bold;}.direction {font-size: 2pt;text-align:right;}.estimates a {font-weight: bold;}.arr_title_1 b{color:#ea0000;border-bottom:1px solid #ea0000;}.arr_title_2 b{color:#0066aa;border-bottom:1px solid #0066aa;}.arr_title_3 b{color:#feab10;border-bottom:1px solid #feab10;}.vehNumber {padding:1px 3px 1px 3px;color:white;width:2em;text-align:center;font-weight:bold;}.content {padding-bottom:2px;margin-top:-4px;border-bottom:1px solid #ddd;}.errorText {color: #f00;}.legal{font-size: 50%;text-align:right;}</style></head>";

    private final String stationCode;
    private final String stationLabel;
    private final LocationView context;
    private String htmlData;
    private Date date;

    private final StationsOverlay overlay;

    public VarnaTrafficHtmlResult(LocationView context, Handler uiHandler, StationsOverlay overlay, String stationCode,
            String stationLabel, boolean showRemainingTime) {
        //hardcoded TODO
        this.stationCode = "113";

//        this.stationCode = stationCode;
        this.overlay = overlay;
        this.stationLabel = stationLabel;
        this.context = context;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class DeviceData {
        private int device;
        private int line;
        private String arriveTime;
        private String arriveIn;

        public int getDevice() {
            return device;
        }

        public void setDevice(int device) {
            this.device = device;
        }

        public int getLine() {
            return line;
        }

        public void setLine(int line) {
            this.line = line;
        }

        public String getArriveTime() {
            return arriveTime;
        }

        public void setArriveTime(String arriveTime) {
            this.arriveTime = arriveTime;
        }

        public String getArriveIn() {
            return arriveIn;
        }

        public void setArriveIn(String arriveIn) {
            this.arriveIn = arriveIn;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @SuppressWarnings("unused")
    private static class Response {
        private DeviceData[] liveData;

        public DeviceData[] getLiveData() {
            return liveData;
        }

        public void setLiveData(DeviceData[] liveData) {
            this.liveData = liveData;
        }

    }

    @Override
    public void query() {
        final Response all;
        try {
            
            Log.i(TAG, "fetching: " + STATION_URL + stationCode);

            all = new ObjectMapper().readValue(new java.net.URL(
                    STATION_URL + stationCode).openConnection()
                    .getInputStream(), Response.class);
        } catch (Exception e) {
            throw new IllegalStateException("could not get estimations (null) for " + stationCode + ". " + stationLabel, e);
        }
        date = new Date();

        htmlData = HTML_START + HTML_HEADER + createBody(all) + HTML_END;
    }

    private String createBody(Response all) {
        final StringBuilder result = new StringBuilder();;
        for (DeviceData next : all.getLiveData()) {
            result.append(next.line+ ":"+next.arriveIn+"<br />");
        }
        return result.toString();
    }


    @Override
    public void showResult() {
        context.estimatesDialogDisplayed();
        final WebView browser = new WebView(context);
        browser.loadDataWithBaseURL(null, htmlData, MIME_TYPE, ENCODING, null);

        final Builder dialogBuilder = new AlertDialog.Builder(this.context);
        dialogBuilder.setTitle(context.getString(R.string.format_estimates_dialog_title,
                DateFormat.getTimeFormat(context).format(date), stationLabel, stationCode));

        dialogBuilder.setCancelable(true).setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                context.estimatesDialogClosed();
                dialog.dismiss();
            }
        }).setView(browser);

        dialogBuilder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                context.estimatesDialogClosed();
            }
        });
        handleFavorities(dialogBuilder);
        handleRefresh(dialogBuilder, browser);
        dialogBuilder.create().show();
    }

    private void handleRefresh(Builder dialogBuilder, final WebView browser) {
        dialogBuilder.setNeutralButton(R.string.buttonRefreshEstimates, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                overlay.showStation(stationCode, false);
            }
        });

    }

    private void handleFavorities(Builder dialogBuilder) {
        final FavoritiesService favoritiesService = getFavoritiesService();
        if (!favoritiesService.isFavorite(stationCode)) {
            // add to favorite
            dialogBuilder.setNegativeButton(R.string.buttonAddToFavorities, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    context.estimatesDialogClosed();

                    favoritiesService.add(new BusStopItem(0, stationCode, stationLabel));
                    final String message = context.getResources().getString(R.string.info_addedToFavorities,
                            stationLabel, stationCode);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        }

    }

    private FavoritiesService getFavoritiesService() {
        // XXX how to pass this service across whole application?
        return new FavoritiesService(context);
    }

}
