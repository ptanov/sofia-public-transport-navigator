package eu.tanov.android.sptn.providers;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.providers.StationProvider.Station;

public class InitStations {
	private static final String ENCODING = "UTF8";
	private final Context context;

	private static class Handler extends DefaultHandler {
		private static final String FORMAT_SQL_INSERT = "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)";

		private static final String STATION = "station";
		
		//xml structure:
		private static final String CODE = Station.CODE;
		private static final String LABEL = Station.LABEL;
		private static final String LON = Station.LON;
		private static final String LAT = Station.LAT;

		private final String tableName;
		
		private final SQLiteStatement insertStatement;

		public Handler(SQLiteDatabase db, String tableName) {
			this.tableName = tableName;
			
			insertStatement = db.compileStatement(String.format(FORMAT_SQL_INSERT,
					this.tableName, Station.CODE, Station.LAT,
					Station.LON, Station.LABEL)
			);
		}

		@Override
		public void startElement(String uri, String name, String qName,
				Attributes atts) {
			if (STATION.equals(name)) {
				final String code = atts.getValue(CODE);
				final String lat = atts.getValue(LAT);
				final String lon = atts.getValue(LON);
				final String label = atts.getValue(LABEL);

				addStation(Integer.valueOf(code), Double
						.valueOf(lat), Double.valueOf(lon), label);
			}
		}

		private void addStation(Integer code, Double lat, Double lon, String label) {
			insertStatement.bindLong(1, code);
			insertStatement.bindDouble(2, lat);
			insertStatement.bindDouble(3, lon);
			insertStatement.bindString(4, label);
			insertStatement.executeInsert();
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
		
		db.beginTransaction();
		try {
			xr.parse(inputSource);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	private void initParser() {
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
	}

}
