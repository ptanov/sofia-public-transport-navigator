package eu.tanov.android.bptcommon;

public interface ParserListener {

	public void setStationName(String stationName);

	public void setEstimatedTime(String vehicleType, String lineNumber, String estimatedTime);
}
