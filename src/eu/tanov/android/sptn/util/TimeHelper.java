package eu.tanov.android.sptn.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;

import android.util.Log;

public class TimeHelper {
	private static final int MILLIS_TO_MINUTES = 1000 * 60;
	private static final int MILLIS_TO_HOURS = MILLIS_TO_MINUTES * 60;
    // 1 hour = 3 600 000 milliseconds
	private static final int HOURS_TO_MILLIS = 60 * 60 * 1000;
	
	private static final String SEPARATOR_ESTIMATED_TIME = ",";
	private static final String TAG = "TimeHelper";
	private static final int SPACE_PER_REMAINING_TIME = 10;
    private static final long NEXT_DAY_TRASHOLD = 20 * HOURS_TO_MILLIS;

	/**
	 * utility class - no instance
	 */
	private TimeHelper() {
	}

	public static String toRemainingTimes(Date now, String timeData,
			String formatOnlyMinutes, String formatMinutesAndHours) {
		final String[] times = timeData.split(SEPARATOR_ESTIMATED_TIME);
		final TreeMap<Long, String> sortedTimes = new TreeMap<Long, String>();
        for (String time : times) {
            try {
                toRemainingTime(sortedTimes, now, time, formatOnlyMinutes, formatMinutesAndHours);
            } catch (Exception e) {
                Log.e(TAG, "could not convert " + time, e);
                addInFirstEmptyPlace(sortedTimes, 0, time);
            }
        }
        
        final StringBuilder result = new StringBuilder(times.length * SPACE_PER_REMAINING_TIME);
        for (String time : sortedTimes.values()) {
            result.append(time);
            result.append(SEPARATOR_ESTIMATED_TIME);
        }

		if (result.length()>0) {
			//remove last comma
			result.deleteCharAt(result.length() - SEPARATOR_ESTIMATED_TIME.length());
		}
		
		return result.toString();
	}

	private static void toRemainingTime(TreeMap<Long, String> sortedTimes, Date now, String time,
			String formatOnlyMinutes, String formatMinutesAndHours) {
		final String[] hoursMinutes = time.split(":");
		if (hoursMinutes.length != 2) {
			throw new IllegalArgumentException(
					"could not split hours-minutes: " + time);
		}
		
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hoursMinutes[0]));
		calendar.set(Calendar.MINUTE, Integer.parseInt(hoursMinutes[1]));

		final long remainingTimeInMillis = getRemainingTimeInMillis(now, calendar);
		if (remainingTimeInMillis < 0) {
			throw new IllegalArgumentException(String.format(
					"negative remaining time: time = %s, now = %s, diff = %s",
					time, now, remainingTimeInMillis));
		}
		addInFirstEmptyPlace(sortedTimes, calendar.getTimeInMillis(), remainingTimeToHumanReadableForm(remainingTimeInMillis, formatOnlyMinutes, formatMinutesAndHours));
	}

	private static void addInFirstEmptyPlace(TreeMap<Long, String> sortedTimes, long timeInMillis,
            String remainingTimeToHumanReadableForm) {
	    while(sortedTimes.containsKey(timeInMillis)) {
	        timeInMillis++;
	    }
	    sortedTimes.put(timeInMillis, remainingTimeToHumanReadableForm);
    }

    private static long getRemainingTimeInMillis(Date now, Calendar calendar) {
        final long arrivingTime = calendar.getTimeInMillis();

        final long remainingTimeInMillis = arrivingTime - now.getTime();
        if (remainingTimeInMillis < 0 && (-remainingTimeInMillis) > NEXT_DAY_TRASHOLD) {
            calendar.add(Calendar.DATE, 1);
            return getRemainingTimeInMillis(now, calendar);
        }
        return remainingTimeInMillis;
	}
	/**
	 * @param remaining
	 * @return something like ~1ч.22м.
	 */
	private static String remainingTimeToHumanReadableForm(long remaining,
			String formatOnlyMinutes, String formatMinutesAndHours) {
		int minutes = (int) ((remaining % MILLIS_TO_HOURS) / MILLIS_TO_MINUTES);
		int hours = (int) (remaining / MILLIS_TO_HOURS);
		
		if (hours > 0) {
			return String.format(formatMinutesAndHours, hours, minutes);
		} else {
			return String.format(formatOnlyMinutes, minutes);
		}
	}

	public static String removeTrailingSeparator(String timeData) {
		if (!timeData.endsWith(SEPARATOR_ESTIMATED_TIME)) {
			return timeData;
		}
		// sometimes estimated time has one more comma: 21:00,21:20,
		return timeData.substring(0, timeData.length() - 1);
	}

}
