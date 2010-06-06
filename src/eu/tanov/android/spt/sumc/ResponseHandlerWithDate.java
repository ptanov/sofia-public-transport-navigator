package eu.tanov.android.spt.sumc;

import java.io.IOException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.BasicResponseHandler;

public class ResponseHandlerWithDate extends BasicResponseHandler {
	private Date date;

	@Override
	public String handleResponse(HttpResponse response)
			throws HttpResponseException, IOException {

		
		final Header dateHeader = response.getFirstHeader("Date");
		if (dateHeader != null) {
			try {
				date = new Date(dateHeader.getValue());
			} catch (Exception e) {
				//current phone time
				date = new Date();
			}
		} else {
			//current phone time
			date = new Date();
		}

		return super.handleResponse(response);
	}
	
	/**
	 * @return always not null
	 */
	public Date getDate() {
		return date;
	}
}
