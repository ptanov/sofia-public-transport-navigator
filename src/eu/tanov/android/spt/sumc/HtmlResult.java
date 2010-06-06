package eu.tanov.android.spt.sumc;

import eu.tanov.android.spt.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.webkit.WebView;

public class HtmlResult implements EstimatesResolver {
	private static final String HTML_START = "<html>";
	private static final String HTML_END = "</html>";
	private final String stationCode;
	private final String stationLabel;
	private final Activity context;
	private String htmlData;

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
		htmlData = HTML_START + context.getString(R.string.html_header) + createBody(response) + HTML_END;
	}

	private String createBody(String response) {
		final int startOfBody = response.indexOf("<p class=\"type");

		final int endOfBody = response.indexOf("<div class=\"footer\"");
		//TODO add server time in dialog
		//TODO add option for remaining time, not estimate hour
			
		return response.substring(startOfBody, endOfBody) +
			context.getString(R.string.legal_sumc_html);
	}

	@Override
	public void showResult() {
		final WebView browser = new WebView(context);
		browser.loadData(htmlData, "text/html", "utf-8");

		Builder dialogBuilder = new AlertDialog.Builder(this.context);
		dialogBuilder.setTitle(stationLabel);

		dialogBuilder.setCancelable(true).setPositiveButton(R.string.buttonOk,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).setView(browser);
		dialogBuilder.create().show();
	}

}
