package eu.tanov.android.bptcommon;

import eu.tanov.android.bptcommon.utils.TimeHelper;



public class Parser {

	private static final String BEGIN_STATION_NAME = "class=\"busStop\">";
	private static final int BEGIN_STATION_NAME_LEN = BEGIN_STATION_NAME.length();
	private static final String END_STATION_NAME = "<";

	private static final String BEGIN_TYPE_VEHICLE = "class=\"typeVehicle ";
	private static final int BEGIN_TYPE_VEHICLE_LEN = BEGIN_TYPE_VEHICLE.length();
	private static final String END_TYPE_VEHICLE = "\">";

	private static final String BEGIN_LINE_NUMBER = "Number\">";
	private static final int BEGIN_LINE_NUMBER_LEN = BEGIN_LINE_NUMBER.length();
	private static final String END_LINE_NUMBER = "</span>";

	private static final String BEGIN_ESTIMATED_TIME = "-";
	private static final int BEGIN_ESTIMATED_TIME_LEN = BEGIN_ESTIMATED_TIME.length();
	private static final String END_ESTIMATED_TIME = "</div>";

	private final String response;
	private final ParserListener parserListener;

	private int parsedToPosition = 0;
	
	public Parser(String response, ParserListener parserListener) {
		this.response = response;
		this.parserListener = parserListener;
	}

	public void parse() {
		parseStationName();
		
		while(parseTypeVehicle());
	}

	private void parseStationName() {
		final int start = response.indexOf(BEGIN_STATION_NAME, parsedToPosition)+BEGIN_STATION_NAME_LEN;
		final int end = response.indexOf(END_STATION_NAME, start);
		parsedToPosition = end;

		parserListener.setStationName(response.substring(start, end).trim());
	}
	private boolean parseTypeVehicle() {
		int start = response.indexOf(BEGIN_TYPE_VEHICLE, parsedToPosition)+BEGIN_TYPE_VEHICLE_LEN;
		if (start == (-1+BEGIN_TYPE_VEHICLE_LEN)) {
			//no more vehicle types
			return false;
		}
		final int end = response.indexOf(END_TYPE_VEHICLE, start);
		parsedToPosition = end;

		final String typeVehicle = response.substring(start, end).trim();
		
		while(parseLineNumberAndEstimatedTime(typeVehicle));

		return true;
	}


	private boolean parseLineNumberAndEstimatedTime(String typeVehicle) {
		int start = response.indexOf(BEGIN_LINE_NUMBER, parsedToPosition)+BEGIN_LINE_NUMBER_LEN;
		if (start == (-1+BEGIN_LINE_NUMBER_LEN)) {
			//no more lines
			return false;
		}

		int nextVehicleTypeStart = response.indexOf(BEGIN_TYPE_VEHICLE, parsedToPosition);
		if (nextVehicleTypeStart!=-1 && start > nextVehicleTypeStart) {
			//new vehicle type is first
			return false;
		}

		final int end = response.indexOf(END_LINE_NUMBER, start);
		parsedToPosition = end;

		final String lineNumber = response.substring(start, end).trim();
		final String estimatedTime = parseEstimatedTime();
		
		parserListener.setEstimatedTime(typeVehicle, lineNumber, estimatedTime);
		return true;
	}
	
	private String parseEstimatedTime() {
		final int start = response.indexOf(BEGIN_ESTIMATED_TIME, parsedToPosition)+BEGIN_ESTIMATED_TIME_LEN;
		int end = response.indexOf(END_ESTIMATED_TIME, start);
		parsedToPosition = end;

		String result = response.substring(start, end).trim();
		return TimeHelper.removeTrailingSeparator(result);
	}
}
