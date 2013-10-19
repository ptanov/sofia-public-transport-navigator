package eu.tanov.android.sptn.sumc;

import java.util.Date;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;
import eu.tanov.android.sptn.LocationView;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.favorities.BusStopItem;
import eu.tanov.android.sptn.favorities.FavoritiesService;
import eu.tanov.android.sptn.map.StationsOverlay;
import eu.tanov.android.sptn.util.TimeHelper;

public class VarnaTrafficHtmlResult implements EstimatesResolver {

	// private static final String FORMAT_OUTPUT_INFO =
	// "<table><tr><td class=\"number\"><a href=\"http://m.sumc.bg%s\">%s</a></td><td class=\"estimates\"><a href=\"http://m.sumc.bg%s\">%s</a></td></tr><tr><td class=\"direction\" colspan=\"2\">%s</td></tr></table>";
    private static final String FORMAT_OUTPUT_INFO_RIGHT = "<table><tr><td class=\"number\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td><td class=\"estimates\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td><td class=\"direction\" style=\"font-size: %dpt;\" >%s</td></tr></table>";
    private static final String FORMAT_OUTPUT_INFO_BOTTOM = "<table><tr><td class=\"number\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td><td class=\"estimates\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td></tr><tr><td colspan=\"2\" class=\"direction\" style=\"font-size: %dpt;\" >%s</td></tr></table>";
    private static final String FORMAT_OUTPUT_INFO_NO_DIRECTION = "<table><tr><td class=\"number\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td><td class=\"estimates\"><a href=\"http://m.sofiatraffic.bg%s\">%s</a></td></tr></table>";

	private static final String FORMAT_OUTPUT_INFO_WHATS_NEW_VERSION_1_06 = "<table><tr><td colspan=\"2\" style=\"color: red;\" >Текстът в синьо е линк към разписанието:</td></tr><tr><td class=\"number\"><a href=\"http://m.sumc.bg%s\">%s</a></td><td class=\"estimates\"><a href=\"http://m.sumc.bg%s\">%s</a></td></tr><tr><td colspan=\"2\" class=\"direction\" style=\"font-size: 10pt;\" >%s</td></tr><tr><td colspan=\"2\" style=\"color: red;\" >^^^ Как да се показва направлението може да се контролира от ОК-МЕНЮ-Настройки</td></tr></table>";
	private static final String FORMAT_OUTPUT_INFO_WHATS_NEW_VERSION_1_09 = "<div style=\"color: red;\" >Версия 1.09: Използвайте бутона 'В избрани' за да добавите текущата спирка в списъка с избрани спирки. До него може да достигнете от основния екран, бутон 'МЕНЮ', 'Избрани спирки'.<br/>Бутонът 'Обнови' ще обнови данните за пристигане на автобусите с най-новите от сайта на СКГТ. Приятно и ползотворно ползване! :)</div>";

	private static final String INFO_SPLITTER = "<a href=\"|\">|<b>|</b>|</a>&nbsp;-&nbsp;|<br />";
	private static final int INFO_SPLIT_SIZE = 7;

	private static final String INFO_BEGIN = "<div class=\"arr_info_";
	private static final int INFO_BEGIN_LENGTH = (INFO_BEGIN + "3\">").length();
	private static final String INFO_END = "</div>";

	private static final String TAG = "HtmlResult";

	private static final String ENCODING = "utf-8";
	private static final String MIME_TYPE = "text/html";

	private static final String BODY_START = "<div class=\"arrivals\">";
	// XXX this is not good end mark, but it is quick fix!
	private static final String BODY_END = "\n</div>";

	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";

	/**
	 * size of buffer will be: body.size() + REMAINING_TIME_BUFFER_ADDITION
	 * 10 - size of one remaining time
	 * 3 - count of remaining times per number
	 * 11 - count of numbers
	 */
	private static final String PREFERENCE_KEY_ESTIMATES_DIRECTION_SIZE = "directionSize";
	private static final String PREFERENCE_DEFAULT_VALUE_ESTIMATES_DIRECTION_SIZE = "2";
	private static final String PREFERENCE_KEY_ESTIMATES_DIRECTION_POSITION_IN_RIGHT = "directionPositionInRight";
	private static final boolean PREFERENCE_DEFAULT_VALUE_ESTIMATES_DIRECTION_POSITION_IN_RIGHT = true;

	private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_06 = "whatsNewShowVersion1_06";
	private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_06 = true;

	private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_09 = "whatsNewShowVersion1_09_estimates";
	private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_09 = true;
    private static final String HTML_HEADER = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" /><style type=\"text/css\">body {font-family:arial, verdana, sans-serif;font-size:14pt;margin: 0;}.sep {clear: both;}.busStop {font-weight: bold;}.typeVehicle {font-weight:bold;padding-left:3px;} td.number {width:30px;text-align:right;}table {width:100%;}div.arr_info_1 td.number {background-color:#ea0000;}div.arr_info_2 td.number {background-color:#0066aa;}div.arr_info_3 td.number {background-color:#feab10;}.number a:link {color:#FFFFFF;font-weight: bold;}.number a:visited {color:#FFFFFF;font-weight: bold;}.number a:hover {color:#FFFFFF;font-weight: bold;}.number a:active {color:#FFFFFF;font-weight: bold;}.direction {font-size: 2pt;text-align:right;}.estimates a {font-weight: bold;}.arr_title_1 b{color:#ea0000;border-bottom:1px solid #ea0000;}.arr_title_2 b{color:#0066aa;border-bottom:1px solid #0066aa;}.arr_title_3 b{color:#feab10;border-bottom:1px solid #feab10;}.vehNumber {padding:1px 3px 1px 3px;color:white;width:2em;text-align:center;font-weight:bold;}.content {padding-bottom:2px;margin-top:-4px;border-bottom:1px solid #ddd;}.errorText {color: #f00;}.legal{font-size: 50%;text-align:right;}</style></head>";

	private final String stationCode;
	private final String stationLabel;
	private final LocationView context;
	private String htmlData;
	private Date date;
	private final boolean showRemainingTime;

	private final String formatMinutesAndHours;
	private final String formatOnlyMinutes;
	private final Integer directionSize;
	private final boolean directionPositionInRight;
	private boolean showWhatsNewInVersion1_06;
	private boolean showWhatsNewInVersion1_09;
	private final StationsOverlay overlay;
    private final Handler uiHandler;

	public VarnaTrafficHtmlResult(LocationView context, Handler uiHandler, StationsOverlay overlay, String stationCode, String stationLabel, boolean showRemainingTime) {
		this.stationCode = stationCode;
		this.overlay = overlay;
		this.stationLabel = stationLabel;
		this.context = context;
		this.uiHandler = uiHandler;
		this.showRemainingTime = showRemainingTime;

		this.formatOnlyMinutes = this.context.getString(R.string.remainingTime_format_onlyMinutes);
		this.formatMinutesAndHours = this.context.getString(R.string.remainingTime_format_minutesAndHours);

		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		directionSize = Integer.valueOf(settings.getString(PREFERENCE_KEY_ESTIMATES_DIRECTION_SIZE,
				PREFERENCE_DEFAULT_VALUE_ESTIMATES_DIRECTION_SIZE));
		directionPositionInRight = settings.getBoolean(PREFERENCE_KEY_ESTIMATES_DIRECTION_POSITION_IN_RIGHT,
				PREFERENCE_DEFAULT_VALUE_ESTIMATES_DIRECTION_POSITION_IN_RIGHT);
		showWhatsNewInVersion1_06 = settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_06,
				PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_06);
		showWhatsNewInVersion1_09 = settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_09,
				PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_09);
	}

	@Override
	public void query() {
		final Browser browser = new Browser();
		final String response = browser.queryStation(context, uiHandler, stationCode);
		if (response == null) {
			throw new IllegalStateException("could not get estimations (null) for " + stationCode + ". " + stationLabel);
		}

		// servers of sumc does not have ntp synchronization and their time is wrong
		// date = responseHandler.getDate();
		date = new Date();
		htmlData = HTML_START + HTML_HEADER + createBody(response) + HTML_END;
	}

	private String createBody(String response) {
		final int startOfBody = response.indexOf(BODY_START);
		final int endOfBody = response.indexOf(BODY_END, startOfBody);

		if (startOfBody == -1 || endOfBody == -1) {
			// error
			if (response.contains(context.getResources().getString(R.string.error_retrieveEstimates_matching_noInfo))) {
				return context.getResources().getString(R.string.error_retrieveEstimates_noInfo, stationLabel, stationCode);
			} else if (response.contains(context.getResources().getString(R.string.error_retrieveEstimates_matching_noBusStop))) {
				return context.getResources().getString(R.string.error_retrieveEstimates_noBusStop, stationLabel, stationCode);
			}
			throw new IllegalStateException("Unknown error with " + response);
		}
		final String body = response.substring(startOfBody, endOfBody + BODY_END.length());
		final String fixedBody = fixBody(body);

		return fixedBody + context.getString(R.string.legal_sumc_html);
	}

	// XXX bad code, improve:
	private String fixBody(final String body) {
		final StringBuilder result = new StringBuilder(body.length() * 2);

		int end = 0;
		int start = body.indexOf(INFO_BEGIN, end);
		while (start != -1) {
			start += INFO_BEGIN_LENGTH;
			// just copy not remaining time data
			result.append(body, end, start);

			end = body.indexOf(INFO_END, start);
			if (end != -1) {
				try {
					final String[] split = body.substring(start, end).split(INFO_SPLITTER, INFO_SPLIT_SIZE);
					if (split.length != INFO_SPLIT_SIZE) {
						throw new IllegalStateException("different split size: " + split.length);
					}

					result.append(formatSplitted(split));
				} catch (Exception e) {
					Log.e(TAG, "error while converting: " + body.substring(start, end), e);
					// this is not remaining time - just copy:
					result.append(body, start, end);
				}
			}

			start = body.indexOf(INFO_BEGIN, end);
		}
		// append rest of body
		result.append(body, end, body.length());

		return result.toString();
	}

	/**
	 * XXX bad code - many IFs... reduce them...
	 */
	private String formatSplitted(String[] split) {
		if (showRemainingTime) {
			split[5] = TimeHelper.toRemainingTimes(date, split[5].trim(), formatOnlyMinutes, formatMinutesAndHours);
		}

		if (showWhatsNewInVersion1_06) {
			final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			// show only first time:
			editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_06, false);
			editor.commit();
			showWhatsNewInVersion1_06 = false;
			return String.format(FORMAT_OUTPUT_INFO_WHATS_NEW_VERSION_1_06, split[1], split[3], split[1], split[5], split[6]);
		}

		if (showWhatsNewInVersion1_09) {
			final Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			// show only first time:
			editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_09, false);
			editor.commit();
			showWhatsNewInVersion1_09 = false;
			return String.format(FORMAT_OUTPUT_INFO_WHATS_NEW_VERSION_1_09, split[1], split[3], split[1], split[5], split[6]);
		}
		if (directionSize < 1) {
			return String.format(FORMAT_OUTPUT_INFO_NO_DIRECTION, split[1], split[3], split[1], split[5]);
		}

		if (directionPositionInRight) {
			return String.format(FORMAT_OUTPUT_INFO_RIGHT, split[1], split[3], split[1], split[5], directionSize, split[6]);
		} else {
			return String.format(FORMAT_OUTPUT_INFO_BOTTOM, split[1], split[3], split[1], split[5], directionSize, split[6]);
		}
	}

	@Override
	public void showResult() {
		context.estimatesDialogDisplayed();
		final WebView browser = new WebView(context);
        browser.loadDataWithBaseURL(null, htmlData, MIME_TYPE, ENCODING, null);

		final Builder dialogBuilder = new AlertDialog.Builder(this.context);
		dialogBuilder.setTitle(context.getString(R.string.format_estimates_dialog_title, DateFormat.getTimeFormat(context).format(date),
				stationLabel, stationCode));

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
					final String message = context.getResources().getString(R.string.info_addedToFavorities, stationLabel, stationCode);
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
