/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.tanov.android.spt.providers;

import java.util.HashMap;

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
	private static final int DATABASE_VERSION = 3;
	private static final String DEFAULT_STATIONS_LIMIT = "3";

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

        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, "stations", STATIONS);
        URI_MATCHER.addURI(AUTHORITY, "stations/#", STATION_ID);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + STATIONS_TABLE_NAME + " ("
                    + Station._ID + " INTEGER PRIMARY KEY,"
                    + Station.CODE + " INTEGER,"
                    + Station.LAT + " FLOAT,"
                    + Station.LON + " FLOAT"
                    + ");");
            
            populateData(db);
        }

		private static void addStation(SQLiteDatabase db, int code, double lat, double lon) {
        	final ContentValues values = new ContentValues();
        	values.put(Station.CODE, code);
        	values.put(Station.LAT, lat);
        	values.put(Station.LON, lon);
        	db.insert(STATIONS_TABLE_NAME, Station.CODE, values);
		}

        private static void populateData(SQLiteDatabase db) {
        	addStation(db, 1903, 42.6904559680764, 23.332074880599976);
        	addStation(db, 1902, 42.689943376606394, 23.330883979797363);
        	addStation(db,  927, 42.688334600520754, 23.329392671585083);
        	addStation(db, 1700, 42.692190861662795, 23.33517551422119);
        	addStation(db,  299, 42.688279396680365, 23.328673839569092);
        	addStation(db,  300, 42.688003376742486, 23.32924246788025);
        	addStation(db,  926, 42.68828728294628, 23.327772617340088);
        	addStation(db, 1914, 42.686568053294145, 23.330165147781372);
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
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy, DEFAULT_STATIONS_LIMIT);

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
