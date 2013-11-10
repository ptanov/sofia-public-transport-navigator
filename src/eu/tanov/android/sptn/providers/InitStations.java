package eu.tanov.android.sptn.providers;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.tanov.android.sptn.providers.StationProvider.Station;

public class InitStations {
    private static final String ENCODING = "UTF8";

    public static final String PROVIDER_SOFIATRAFFIC = "sofiatraffic.bg";
    public static final String PROVIDER_VARNATRAFFIC = "varnatraffic.com";

    public static final String[] PROVIDERS = new String[] { PROVIDER_SOFIATRAFFIC, PROVIDER_VARNATRAFFIC };

	//TODO move to external file
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionVarnaTraffic {
        private double lat;
        private double lon;

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public double getLon() {
            return lon;
        }

        public void setLon(double lon) {
            this.lon = lon;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class BusStopVarnaTraffic {
        private int id;
        private String text;
        private PositionVarnaTraffic position;

        public int getId() {
            return id;
        }

        @SuppressWarnings("unused")
        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        @SuppressWarnings("unused")
        public void setText(String text) {
            this.text = text;
        }

        public PositionVarnaTraffic getPosition() {
            return position;
        }

        @SuppressWarnings("unused")
        public void setPosition(PositionVarnaTraffic position) {
            this.position = position;
        }

    }
	private static class Handler extends DefaultHandler {

        private static final String FORMAT_SQL_INSERT = "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, '%s')";

		//FIXME rename to "busStop"
		private static final String ELEMENT_NAME_BUS_STATION = "station";
		
		//xml structure:
		private static final String ATTRIBUTE_NAME_CODE = Station.CODE;
		private static final String ATTRIBUTE_NAME_LABEL = Station.LABEL;
		private static final String ATTRIBUTE_NAME_LON = Station.LON;
		private static final String ATTRIBUTE_NAME_LAT = Station.LAT;

		private final String tableName;
		
		private final SQLiteStatement insertStatement;

		public Handler(SQLiteDatabase db, String tableName) {
			this.tableName = tableName;
			
			insertStatement = db.compileStatement(String.format(FORMAT_SQL_INSERT,
					this.tableName, Station.CODE, Station.LAT,
					Station.LON, Station.LABEL, Station.PROVIDER, PROVIDER_SOFIATRAFFIC)
			);
		}

		@Override
		public void startElement(String uri, String name, String qName,
				Attributes atts) {
			if (ELEMENT_NAME_BUS_STATION.equals(name)) {
				final String code = atts.getValue(ATTRIBUTE_NAME_CODE);
				final String lat = atts.getValue(ATTRIBUTE_NAME_LAT);
				final String lon = atts.getValue(ATTRIBUTE_NAME_LON);
				final String label = atts.getValue(ATTRIBUTE_NAME_LABEL);

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

    public void createStations(SQLiteDatabase db, String tableName, InputStream sumcStream, InputStream varnaStream)
            throws IOException, SAXException {
        db.delete(tableName, null, null);
        createSUMCStations(db, tableName, sumcStream);
        createVarnaStations(db, tableName, varnaStream);
    }

	private void createVarnaStations(SQLiteDatabase db, String tableName, InputStream openRawResource) throws JsonParseException, JsonMappingException, IOException {
	    final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        final BusStopVarnaTraffic[] all = OBJECT_MAPPER.readValue(openRawResource, BusStopVarnaTraffic[].class);
        final String FORMAT_SQL_INSERT = "INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, '%s')";
        
        final SQLiteStatement insertStatement = db.compileStatement(String.format(FORMAT_SQL_INSERT,
                tableName, Station.CODE, Station.LAT,
                Station.LON, Station.LABEL, Station.PROVIDER, PROVIDER_VARNATRAFFIC)
        );
        
        for (BusStopVarnaTraffic busStopVarnaTraffic : all) {
            insertStatement.bindLong(1, busStopVarnaTraffic.getId());
            insertStatement.bindDouble(2, busStopVarnaTraffic.getPosition().getLat());
            insertStatement.bindDouble(3, busStopVarnaTraffic.getPosition().getLon());
            insertStatement.bindString(4, busStopVarnaTraffic.getText());
            insertStatement.executeInsert();
        }
	}

    private void createSUMCStations(SQLiteDatabase db, String tableName, InputStream openRawResource) throws IOException, SAXException {
        initParser();

        final XMLReader xr = XMLReaderFactory.createXMLReader();
        final Handler handler = new Handler(db, tableName);

        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);

        final InputSource inputSource = new InputSource(openRawResource);
        inputSource.setEncoding(ENCODING);
        
        xr.parse(inputSource);
    }

    private void initParser() {
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");
	}

}
