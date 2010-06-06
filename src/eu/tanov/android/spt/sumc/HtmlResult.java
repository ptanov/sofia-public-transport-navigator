package eu.tanov.android.spt.sumc;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.webkit.WebView;
import eu.tanov.android.spt.R;

public class HtmlResult implements EstimatesResolver {
	private static final String ENCODING = "utf-8";
	private static final String MIME_TYPE = "text/html";

	private static final String BODY_START = "<p class=\"type";
	private static final String BODY_END = "<div class=\"footer\"";
	
	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";
	
	private final String stationCode;
	private final String stationLabel;
	private final Activity context;
	private String htmlData;
	private Date date;

	public HtmlResult(Activity context, String stationCode, String stationLabel) {
		this.stationCode = stationCode;
		this.stationLabel = stationLabel;
		this.context = context;
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
		//TODO add option for remaining time, not estimate hour
			
		return response.substring(startOfBody, endOfBody) +
			context.getString(R.string.legal_sumc_html);
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
