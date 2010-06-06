package eu.tanov.android.spt.sumc;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.webkit.WebView;
import eu.tanov.android.spt.R;
import eu.tanov.android.spt.util.TimeHelper;

public class HtmlResult implements EstimatesResolver {
	private static final String ENCODING = "utf-8";
	private static final String MIME_TYPE = "text/html";

	private static final String BODY_START = "<p class=\"type";
	private static final String BODY_END = "<div class=\"footer\"";
	
	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";

	private static final char REMAINING_TIME_START = '-';
	private static final int REMAINING_TIME_START_LENGTH = String.valueOf(REMAINING_TIME_START).length();

    private static final char REMAINING_TIME_END = '<';

    /**
     * size of buffer will be: body.size() + REMAINING_TIME_BUFFER_ADDITION
     * 10 - size of one remaining time
     * 3 - count of remaining times per number
     * 11 - count of numbers 
     */
    private static final int REMAINING_TIME_BUFFER_ADDITION = 10 * 3 * 11;
	
	private final String stationCode;
	private final String stationLabel;
	private final Activity context;
	private String htmlData;
	private Date date;
	private final boolean showRemainingTime;

	private final String formatMinutesAndHours;
	private final String formatOnlyMinutes;

	public HtmlResult(Activity context, String stationCode, String stationLabel, boolean showRemainingTime) {
		this.stationCode = stationCode;
		this.stationLabel = stationLabel;
		this.context = context;
		this.showRemainingTime = showRemainingTime;
		
	//	if (showRemainingTime) {
		this.formatOnlyMinutes = this.context.getString(R.string.remainingTime_format_onlyMinutes);
		this.formatMinutesAndHours = this.context.getString(R.string.remainingTime_format_minutesAndHours);
	//	} else {
	//		this.formatOnlyMinutes = null;
	//		this.formatMinutesAndHours = null;
	//	}		
	}

	@Override
	public void query() {
		final Browser browser = new Browser();
		final ResponseHandlerWithDate responseHandler = new ResponseHandlerWithDate();
		browser.setResponseHandler(responseHandler);
		final String response = browser.queryStation(stationCode);
		if (response == null) {
			throw new IllegalStateException(
					"could not get estimations (null) for " + stationCode
							+ ". " + stationLabel);
		}
		date = responseHandler.getDate();
		htmlData = HTML_START + context.getString(R.string.html_header) + createBody(response) + HTML_END;
	}

	private String createBody(String response) {
		final int startOfBody = response.indexOf(BODY_START);

		final int endOfBody = response.indexOf(BODY_END);

		String body = response.substring(startOfBody, endOfBody);
		if (showRemainingTime) {
			body = convertToRemainingTime(body);
		}
		return body + context.getString(R.string.legal_sumc_html);
	}

	//XXX bad code, improve:
	private String convertToRemainingTime(String body) {
		final StringBuilder result = new StringBuilder(body.length() + REMAINING_TIME_BUFFER_ADDITION);
		
		int end = 0;
		int start = body.indexOf(REMAINING_TIME_START, end);
		while (start != -1) {
			start += REMAINING_TIME_START_LENGTH;
			//just copy not remaining time data
			result.append(body, end, start);
			
			end = body.indexOf(REMAINING_TIME_END, start);
			if (end != -1) {
				try {
					String timeData = body.substring(start, end).trim();
					timeData = TimeHelper.removeTrailingSeparator(timeData);
					result.append(TimeHelper.toRemainingTime(date, timeData,
						formatOnlyMinutes, formatMinutesAndHours)
					);
				} catch (Exception e) {
					//this is not remaining time - just copy:
					result.append(body, start, end);
				}
			}

			start = body.indexOf(REMAINING_TIME_START, end);
		}
		//append rest of body
		result.append(body, end, body.length());
		
		return result.toString();
	}

	@Override
	public void showResult() {
		final WebView browser = new WebView(context);
		browser.loadData(htmlData, MIME_TYPE, ENCODING);

		Builder dialogBuilder = new AlertDialog.Builder(this.context);
		dialogBuilder.setTitle(
				context.getString(R.string.format_estimates_dialog_title, 
						DateFormat.getTimeFormat(context).format(date),
						stationLabel
				)
		);

		dialogBuilder.setCancelable(true).setPositiveButton(R.string.buttonOk,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).setView(browser);
		dialogBuilder.create().show();
	}

}
