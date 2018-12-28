package eu.tanov.android.sptn.providers;

import java.io.InputStream;
import java.util.HashMap;

import eu.tanov.android.sptn.R;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of stations. Each station has a code and coordinates
 */
public class StationProvider extends ContentProvider {
	private static final int DATABASE_VERSION = 22;
	public static final int STATIONS_LIMIT = 10;
	private static final String STATIONS_LIMIT_STRING = Integer.toString(STATIONS_LIMIT);

    private static final int STATIONS = 1;
    private static final int STATION_ID = 2;

	private static final String TAG = "StationProvider";

	public static final String AUTHORITY = "eu.tanov.android.StationProvider";
	private static final String STATIONS_TABLE_NAME = "stations";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/"+STATIONS_TABLE_NAME);

    private static final String DATABASE_NAME = "station.db";


    public static final class Station implements BaseColumns {
        // This class cannot be instantiated
        private Station() {}

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.sofiapublictransport.station";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.sofiapublictransport.station";


        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String CODE = "code";
        public static final String LABEL = "label";
        public static final String PROVIDER = "provider";

        public static final String DEFAULT_SORT_ORDER = CODE +" DESC";
    }
    
    
    private static final HashMap<String, String> DEFAULT_COLUMNS_PROJECTION;
	private static final UriMatcher URI_MATCHER;
    static {
        DEFAULT_COLUMNS_PROJECTION = new HashMap<String, String>();
        DEFAULT_COLUMNS_PROJECTION.put(Station._ID, Station._ID);
        DEFAULT_COLUMNS_PROJECTION.put(Station.CODE, Station.CODE);
        DEFAULT_COLUMNS_PROJECTION.put(Station.LAT, Station.LAT);
        DEFAULT_COLUMNS_PROJECTION.put(Station.LON, Station.LON);
        DEFAULT_COLUMNS_PROJECTION.put(Station.LABEL, Station.LABEL);
        DEFAULT_COLUMNS_PROJECTION.put(Station.PROVIDER, Station.PROVIDER);

        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "stations", STATIONS);
        URI_MATCHER.addURI(AUTHORITY, "stations/#", STATION_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
        private final InitStations initializer;
        private final Context context;

		public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.initializer = new InitStations();
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + STATIONS_TABLE_NAME + " ("
                    + Station._ID + " INTEGER PRIMARY KEY,"
                    + Station.CODE + " INTEGER,"
                    + Station.LAT + " FLOAT,"
                    + Station.LON + " FLOAT,"
                    + Station.LABEL + " VARCHAR(50),"
                    + Station.PROVIDER + " VARCHAR(50)"
                    + ");");
            
            reloadBusStops(db, initializer, context.getResources().openRawResource(R.raw.coordinates), context
                    .getResources().openRawResource(R.raw.coordinates_varnatraffic));
        }

		@Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+STATIONS_TABLE_NAME);
            onCreate(db);
        }
    }

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    public static boolean reloadBusStops(Context context, InputStream sumcStream, InputStream varnaStream) {
        return reloadBusStops(new DatabaseHelper(context).getWritableDatabase(), new InitStations(), sumcStream, varnaStream);
    }

    private static boolean reloadBusStops(SQLiteDatabase db, InitStations initializer, InputStream sumcStream,
            InputStream varnaStream) {
        db.beginTransaction();
        try {
            initializer.createStations(db, STATIONS_TABLE_NAME, sumcStream, varnaStream);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "failed to create stations", e);
            return false;
        } finally {
            db.endTransaction();
        }
        return true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(STATIONS_TABLE_NAME);
        qb.setProjectionMap(DEFAULT_COLUMNS_PROJECTION);

        // If no sort order is specified use the default
        final String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Station.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, STATIONS_LIMIT_STRING);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
        case STATIONS:
            return Station.CONTENT_TYPE;

        case STATION_ID:
            return Station.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (URI_MATCHER.match(uri) != STATIONS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (initialValues == null) {
        	throw new IllegalArgumentException("Values should not be null");
        }

        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = db.insert(STATIONS_TABLE_NAME, Station.CODE, initialValues);
        if (rowId > 0) {
            final Uri noteUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int count = db.delete(STATIONS_TABLE_NAME, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int count;
        switch (URI_MATCHER.match(uri)) {
        case STATIONS:
            count = db.update(STATIONS_TABLE_NAME, values, where, whereArgs);
            break;

        case STATION_ID:
            final String noteId = uri.getPathSegments().get(1);
            count = db.update(STATIONS_TABLE_NAME, values, Station._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

}
