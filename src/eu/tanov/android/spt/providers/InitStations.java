package eu.tanov.android.spt.providers;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import eu.tanov.android.spt.R;
import eu.tanov.android.spt.providers.StationProvider.Station;

public class InitStations {
	private static final String ENCODING = "UTF8";
	private final Context context;

	private static class Handler extends DefaultHandler {
		private static final String STATION = "station";
		
		private static final String CODE = "code";
		private static final String LABEL = "label";
		private static final String LON = "lon";
		private static final String LAT = "lat";

		private final SQLiteDatabase db;
		private final String tableName;

		public Handler(SQLiteDatabase db, String tableName) {
			this.db = db;
			this.tableName = tableName;
		}

		@Override
		public void startElement(String uri, String name, String qName,
				Attributes atts) {
			//TODO create in compiled statement and in transaction

			if (STATION.equals(name)) {
				final String code = atts.getValue(CODE);
				final String lat = atts.getValue(LAT);
				final String lon = atts.getValue(LON);
				final String label = atts.getValue(LABEL);

				addStation(db, tableName, Integer.valueOf(code), Double
						.valueOf(lat), Double.valueOf(lon), label);
			}
		}

		private static void addStation(SQLiteDatabase db, String tableName,
				Integer code, Double lat, Double lon, String label) {
			final ContentValues values = new ContentValues();
			values.put(Station.CODE, code);
			values.put(Station.LAT, lat);
			values.put(Station.LON, lon);
			values.put(Station.LABEL, label);
			db.insert(tableName, Station.CODE, values);
		}
	}

	public InitStations(Context context) {
		this.context = context;
	}

	public void createStations(SQLiteDatabase db, String tableName) throws IOException, SAXException {
		initParser();

		final XMLReader xr = XMLReaderFactory.createXMLReader();
		final Handler handler = new Handler(db, tableName);

		xr.setContentHandler(handler);
		xr.setErrorHandler(handler);

		final InputStream openRawResource = context.getResources()
				.openRawResource(R.raw.coordinates);

		final InputSource inputSource = new InputSource(openRawResource);
		inputSource.setEncoding(ENCODING);
		xr.parse(inputSource);
	}

	private void initParser() {
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
	}

}
