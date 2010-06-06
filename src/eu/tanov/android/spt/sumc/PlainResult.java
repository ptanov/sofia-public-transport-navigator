package eu.tanov.android.spt.sumc;

import android.app.Activity;

public class PlainResult implements EstimatesResolver {
	private final String stationCode;
	private final String stationLabel;
	private final Activity context;

	private PlainDialogBuilder builder;

	public PlainResult(Activity context, String stationCode, String stationLabel) {
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
			throw new IllegalStateException("could not get estimations (null) for " + stationCode
					+ ". " + stationLabel);
		}
		builder = new PlainDialogBuilder(context);
		new Parser(response, builder).parse();
	}

	@Override
	public void showResult() {
		builder.create().show();
	}

}
