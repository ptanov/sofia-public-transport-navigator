package eu.tanov.android.sptn.util;

import com.google.android.gms.maps.model.LatLng;

public class MapHelper {
	/**
	 * utility class - no instance
	 */
	private MapHelper() {}

	public static LatLng createGeoPoint(double lat, double lon) {
		return new LatLng(lat, lon);
	}
	@Deprecated
	public static int toE6(double coordinate) {
		final Double result = coordinate * 1E6;
		return result.intValue();
	}
	@Deprecated
	public static double toCoordinate(int coordinateE6) {
		return coordinateE6 / 1E6;
	}
}
