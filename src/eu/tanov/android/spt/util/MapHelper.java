package eu.tanov.android.spt.util;

import com.google.android.maps.GeoPoint;

public class MapHelper {
	public static GeoPoint createGeoPoint(double lat, double lon) {
		return new GeoPoint(toE6(lat), toE6(lon));
	}
	public static int toE6(double coordinate) {
		final Double result = coordinate * 1E6;
		return result.intValue();
	}
}
