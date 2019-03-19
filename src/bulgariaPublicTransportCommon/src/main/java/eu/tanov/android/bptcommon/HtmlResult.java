package eu.tanov.android.bptcommon;

import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.text.format.DateFormat;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;
import eu.tanov.android.bptcommon.favorities.BusStopItem;
import eu.tanov.android.bptcommon.favorities.FavoritiesService;
import eu.tanov.android.bptcommon.interfaces.IStationsOverlay;
import eu.tanov.android.bptcommon.interfaces.ILocationView;

import static com.flurry.sdk.eh.d;
import static eu.tanov.android.bptcommon.R.id.dialog;

public abstract class HtmlResult implements EstimatesResolver {
    protected static final String ENCODING = "utf-8";
    protected static final String MIME_TYPE = "text/html";

    protected static final String HTML_START = "<html>";
    protected static final String HTML_END = "</html>";
    protected static final String HTML_HEADER = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><style type=\"text/css\">body {font-family:arial, verdana, sans-serif;font-size:14pt;margin: 0;}.sep {clear: both;}.busStop {font-weight: bold;}.typeVehicle {font-weight:bold;padding-left:3px;} td.number {width:30px;text-align:right;}table {width:100%;}div.arr_info_1 td.number {background-color:#ea0000;}div.arr_info_2 td.number {background-color:#0066aa;}div.arr_info_3 td.number {background-color:#feab10;}.number a:link {color:#FFFFFF;font-weight: bold;}.number a:visited {color:#FFFFFF;font-weight: bold;}.number a:hover {color:#FFFFFF;font-weight: bold;}.number a:active {color:#FFFFFF;font-weight: bold;}.direction {font-size: 2pt;text-align:right;}.estimates a {font-weight: bold;}.arr_title_1 b{color:#ea0000;border-bottom:1px solid #ea0000;}.arr_title_2 b{color:#0066aa;border-bottom:1px solid #0066aa;}.arr_title_3 b{color:#feab10;border-bottom:1px solid #feab10;}.vehNumber {padding:1px 3px 1px 3px;color:white;width:2em;text-align:center;font-weight:bold;}.content {padding-bottom:2px;margin-top:-4px;border-bottom:1px solid #ddd;}.errorText {color: #f00;}.legal{font-size: 50%;text-align:right;}.bus-delay{font-size:80%}.bus-delay-red{color:red}.bus-delay-green{color:green}</style></head>";

    protected final String stationCode;
    protected final String stationLabel;

    protected final Context context;
    protected String htmlData;
    protected String textData;
    protected Date date;

    private final IStationsOverlay overlay;
    protected final String provider;

    public HtmlResult(Context context, IStationsOverlay overlay, String provider, String stationCode,
            String stationLabel) {
        this.stationCode = stationCode;
        this.overlay = overlay;
        this.stationLabel = stationLabel;
        this.context = context;
        this.provider = provider;
    }


    @Override
    public String getResultAsString() {
        return textData;
    }
    
    @Override
    public void showResult(boolean onlyBuses) {
        if (onlyBuses) {
            return;
        }
        if (!(context instanceof ILocationView)) {
            return;
        }
        ((ILocationView)context).estimatesDialogDisplayed();
        final WebView browser = new WebView(context);
        browser.loadDataWithBaseURL(null, htmlData, MIME_TYPE, ENCODING, null);

        final Builder dialogBuilder = new AlertDialog.Builder(this.context);
        dialogBuilder.setTitle(context.getString(R.string.format_estimates_dialog_title,
                DateFormat.getTimeFormat(context).format(date), stationLabel, stationCode));

        dialogBuilder.setCancelable(true).setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ((ILocationView)context).estimatesDialogClosed();
                dialog.dismiss();
            }
        }).setView(browser);

        dialogBuilder.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface arg0) {
                ((ILocationView)context).estimatesDialogClosed();
            }
        });
        handleFavorities(dialogBuilder);
        handleRefresh(dialogBuilder, browser);

        AlertDialog result = dialogBuilder.create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(result.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        result.getWindow().setAttributes(lp);
        result.show();
    }

    private void handleRefresh(Builder dialogBuilder, final WebView browser) {
        dialogBuilder.setNeutralButton(R.string.buttonRefreshEstimates, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                overlay.showStation(provider, stationCode, false);
            }
        });

    }

    private void handleFavorities(Builder dialogBuilder) {
        final FavoritiesService favoritiesService = getFavoritiesService();
        if (!favoritiesService.isFavorite(provider, stationCode)) {
            // add to favorite
            dialogBuilder.setNegativeButton(R.string.buttonAddToFavorities, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ((ILocationView)context).estimatesDialogClosed();

                    favoritiesService.add(new BusStopItem(provider, 0, stationCode, stationLabel));
                    final String message = context.getResources().getString(R.string.info_addedToFavorities,
                            stationLabel, stationCode);
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            });
        } else {
            // add to schedule
//            dialogBuilder.setNegativeButton(R.string.buttonAddSchedule, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int id) {
//                    context.estimatesDialogClosed();
//
////                    favoritiesService.add(new BusStopItem(provider, 0, stationCode, stationLabel));
////                    final String message = context.getResources().getString(R.string.info_addedToFavorities,
////                            stationLabel, stationCode);
////                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
//                    dialog.dismiss();
//                }
//            });
        }

    }

    protected FavoritiesService getFavoritiesService() {
        // XXX how to pass this service across whole application?
        return new FavoritiesService(context, R.string.favorities_null_label);
    }

}
