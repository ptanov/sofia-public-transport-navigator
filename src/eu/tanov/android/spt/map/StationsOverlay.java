package eu.tanov.android.spt.map;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.app.AlertDialog;
import android.database.Cursor;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import eu.tanov.android.spt.R;
import eu.tanov.android.spt.providers.StationProvider;
import eu.tanov.android.spt.providers.StationProvider.Station;
import eu.tanov.android.spt.sumc.Browser;
import eu.tanov.android.spt.sumc.DialogBuilder;
import eu.tanov.android.spt.sumc.Parser;
import eu.tanov.android.spt.util.MapHelper;

public class StationsOverlay extends ItemizedOverlay<OverlayItem> {
	private static final String TAG = "StationsOverlay";

	private final ArrayList<OverlayItem> stations = new ArrayList<OverlayItem>();
	private final Activity context;
	private final MapView map;
	
	private final HashSet<String> addedStations = new HashSet<String>();

    private static final String[] PROJECTION = new String[] {
    	Station._ID, // 0
    	Station.CODE, // 1
    	Station.LAT, // 2
    	Station.LON, // 3
    };

	public StationsOverlay(Activity context, MapView map) {
		super(boundCenterBottom(context.getResources().getDrawable(R.drawable.icon)));
		this.context = context;
		this.map = map;
		populate();
//		placeStations();
	}
	
	/**
	 * should be called if location changes
	 * @param location new location
	 */
	public void placeStations(Location location) {
        final Cursor cursor = context.managedQuery(StationProvider.CONTENT_URI, PROJECTION,
        		null, null, sortOrder(location));

        if (cursor.moveToFirst()) {
            int codeColumn = cursor.getColumnIndex(Station.CODE); 
            int latColumn = cursor.getColumnIndex(Station.LAT);
            int lonColumn = cursor.getColumnIndex(Station.LON);

            String code; 
            double lat; 
            double lon; 
        
            do {
                // Get the field values
                code = cursor.getString(codeColumn);
                if (addedStations.contains(code)) {
                	//already added
                	continue;
                }
                addedStations.add(code);
                lat = cursor.getDouble(latColumn);
                lon = cursor.getDouble(lonColumn);

                final GeoPoint point = MapHelper.createGeoPoint(lat, lon);
                stations.add(new OverlayItem(point, code, ""));

                //FIXME remove:
                map.getController().animateTo(point);
            } while (cursor.moveToNext());
        }
        
	    populate();
	}

	private String sortOrder(Location location) {
		return String.format("(ABS(lat-%f)+ABS(lon-%f)) ASC",
				location.getLatitude(), location.getLongitude());
	}

	@Override
	protected OverlayItem createItem(int i) {
		return stations.get(i);
	}
//	public void addOverlay(OverlayItem overlay) {
//	    stations.add(overlay);
//	    populate();
//	}
	@Override
	public int size() {
		return stations.size();
	}

	@Override
	protected boolean onTap(int stationIndex) {
		final String stationCode = stations.get(stationIndex).getTitle();
		
		//TODO add Please wait dialog, new thread, etc...
		
		try {
			final String response = Browser.queryStation(stationCode);
			if (response == null) {
				showErrorMessage();
				return true;
			}
			final DialogBuilder builder = new DialogBuilder(context);
			new Parser(response, builder).parse();
			
			final AlertDialog dialog = builder.create();
			dialog.show();
		} catch (Exception e) {
			Log.e(TAG, "could not get estimations for "+stationCode, e);
			//being safe (Throwable!?) ;)
			showErrorMessage();
		}
		
		return true;
	}

	private void showErrorMessage() {
	   Toast.makeText(context, R.string.error_retrieveEstimates,
	        Toast.LENGTH_LONG).show();
	}
}
