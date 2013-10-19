package eu.tanov.android.sptn.favorities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.tanov.android.sptn.R;
import eu.tanov.android.sptn.providers.InitStations;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FavoritiesService {
    private static final String PROVIDER_CODE_SEPARATOR = ":";

	/**
	 * Should not be null because of checking for null value in rename
	 */
	private static final String EMPTY_LABEL = "";
    private static final String SHARED_PREFERENCES_NAME_FAVORITIES_POSITIONS_TO_CODE = "favorities_positions";
	private static final String SHARED_PREFERENCES_NAME_FAVORITIES_CODE_TO_LABELS = "favorities_labels";
    private static final String PREFERENCE_IS_PROVIDERS_FIXED_IN_FAVORITIES = "isProvidersFixedInFavorities";
	private final Context context;

	private static Comparator<BusStopItem> comparator = new Comparator<BusStopItem>() {
		@Override
		public int compare(BusStopItem object1, BusStopItem object2) {
			return object1.getPosition() - object2.getPosition();
		}
	};

	public FavoritiesService(Context context) {
		this.context = context;
		
		fixProviders();
	}

	private void fixProviders() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(PREFERENCE_IS_PROVIDERS_FIXED_IN_FAVORITIES, false)) {
            return;
        }
        final SharedPreferences positionsStore = getPositionsStore();
        final SharedPreferences labelsStore = getLabelsStore();
        final SharedPreferences.Editor positionsStoreEditor = positionsStore.edit();
        final SharedPreferences.Editor labelsStoreEditor = labelsStore.edit();

        for (Entry<String, ?> next : positionsStore.getAll().entrySet()) {
            positionsStoreEditor.putString(next.getKey(), mergeProviderAndCode(InitStations.PROVIDER_SOFIATRAFFIC, (String) next.getValue()));
        }
        for (Entry<String, ?> next : labelsStore.getAll().entrySet()) {
            labelsStoreEditor.putString(mergeProviderAndCode(InitStations.PROVIDER_SOFIATRAFFIC, next.getKey()),
                    (String)next.getValue());
        }

        if (positionsStoreEditor.commit() && labelsStoreEditor.commit()) {
            preferences.edit().putBoolean(PREFERENCE_IS_PROVIDERS_FIXED_IN_FAVORITIES, true).commit();
        }
    }

    private SharedPreferences getPositionsStore() {
		return context.getSharedPreferences(SHARED_PREFERENCES_NAME_FAVORITIES_POSITIONS_TO_CODE, Context.MODE_PRIVATE);
	}

	private SharedPreferences getLabelsStore() {
		return context.getSharedPreferences(SHARED_PREFERENCES_NAME_FAVORITIES_CODE_TO_LABELS, Context.MODE_PRIVATE);
	}

	public List<BusStopItem> getAll() {
		final Map<String, ?> allPositions = getPositionsStore().getAll();
		final Map<String, ?> allLabels = getLabelsStore().getAll();

		final List<BusStopItem> result = new ArrayList<BusStopItem>(allPositions.size());

		for (Entry<String, ?> entry : allPositions.entrySet()) {
			result.add(busStopFromEntry(entry, allLabels));
		}

		// not the fastest way, but easiest:
		Collections.sort(result, comparator);
		return result;
	}

	private String splitToProvider(String all) {
	       return all.split(PROVIDER_CODE_SEPARATOR, 2)[0];
	}
	private String splitToCode(String all) {
	    return all.split(PROVIDER_CODE_SEPARATOR, 2)[1];
	}
	private String mergeProviderAndCode(String provider, String code) {
	    return provider + PROVIDER_CODE_SEPARATOR + code;
	}
	private BusStopItem busStopFromEntry(Entry<String, ?> positionEntry, Map<String, ?> allLabels) {
		final String position = positionEntry.getKey();
		final Object all = positionEntry.getValue();
		if (!(all instanceof String)) {
			throw new IllegalStateException("Bus stop code should be integer, not: " + all);
		}

		Object label = allLabels.get(all.toString());
		if (label == null || EMPTY_LABEL.equals(label)) {
		    label = context.getString(R.string.favorities_null_label);
		}
		if (!(label instanceof String)) {
			throw new IllegalStateException("Label for favorite bus stop <" + all + "> should be String, but was: " + label);
		}
		return new BusStopItem(splitToProvider((String) all), Integer.valueOf(position), splitToCode((String) all), (String) label);
	}

	/**
	 * Move to end then remove
	 */
	public void remove(BusStopItem busStop) {
		final SharedPreferences positionsStore = getPositionsStore();
		final SharedPreferences.Editor positionsStoreEditor = positionsStore.edit();
		final SharedPreferences.Editor labelsStoreEditor = getLabelsStore().edit();

		final Map<String, ?> allPositions = positionsStore.getAll();
		final int oldPosition = busStop.getPosition();
		final int lastPosition = allPositions.size() - 1;
		// move to end:
		if (lastPosition != oldPosition) {
			reorder(positionsStoreEditor, allPositions, oldPosition, lastPosition);
		}
		// remove last
		positionsStoreEditor.remove(Integer.toString(lastPosition));
		labelsStoreEditor.remove(mergeProviderAndCode(busStop.getProvider(), busStop.getCode()));

		if (positionsStoreEditor.commit()) {
			labelsStoreEditor.commit();
		}
	}

	public boolean isFavorite(String provider, String code) {
		final SharedPreferences labelsStore = getLabelsStore();
		final String value = labelsStore.getString(mergeProviderAndCode(provider, code), null);
		return value != null;
	}

	public void add(BusStopItem busStop) {
		final SharedPreferences positionsStore = getPositionsStore();
		final SharedPreferences.Editor positionsStoreEditor = positionsStore.edit();
		final SharedPreferences.Editor labelsStoreEditor = getLabelsStore().edit();

		final int newPosition = positionsStore.getAll().size();
		final String all = mergeProviderAndCode(busStop.getProvider(), busStop.getCode());
		positionsStoreEditor.putString(Integer.toString(newPosition), all);
		String label = busStop.getLabel();
		if (label == null) {
		    label = EMPTY_LABEL;
		}
		labelsStoreEditor.putString(all, label);

		if (positionsStoreEditor.commit()) {
			labelsStoreEditor.commit();
		}
	}

	public void rename(String provider, String code, String newLabel) {
        final String all = mergeProviderAndCode(provider, code);

		final SharedPreferences labelsStore = getLabelsStore();
		final String oldName = labelsStore.getString(all, null);
		if (oldName == null) {
			throw new IllegalArgumentException(String.format("Station %s is not in favorities (newLabel: %s)", all, newLabel));
		}
		final SharedPreferences.Editor labelsStoreEditor = labelsStore.edit();
		labelsStoreEditor.putString(all, newLabel);

		labelsStoreEditor.commit();
	}

	/**
	 * If positionOffset is outside [0; size) - uses first valid position in boundary
	 */
	public void move(BusStopItem busStop, int positionOffset) {
		final SharedPreferences positionsStore = getPositionsStore();
		final SharedPreferences.Editor positionsStoreEditor = positionsStore.edit();

		final int oldPosition = busStop.getPosition();
		int newPosition = oldPosition + positionOffset;
		if (newPosition < 0) {
			newPosition = 0;
		}
		final Map<String, ?> allPositions = positionsStore.getAll();
		final int lastPosition = allPositions.size() - 1;
		if (newPosition > lastPosition) {
			newPosition = lastPosition;
		}

		if (newPosition == oldPosition) {
			return;
		}

		reorder(positionsStoreEditor, allPositions, oldPosition, newPosition);

		positionsStoreEditor.commit();
	}

	/**
	 * Without checking for new position boundaries
	 * 
	 * @param originalPositions
	 *            for optimization
	 * @param newPosition
	 *            should be in boundaries, not equal to old position!
	 */
	private void reorder(SharedPreferences.Editor positionsStoreEditor, Map<String, ?> originalPositions, int oldPosition, int newPosition) {
		final Object busStopToMove = originalPositions.get(Integer.toString(oldPosition));
		if (!(busStopToMove instanceof String)) {
			throw new IllegalStateException("No usable information for position(" + oldPosition + "): " + busStopToMove);
		}

		final int increment = oldPosition < newPosition ? 1 : -1;

		for (int currentPosition = oldPosition; currentPosition != newPosition; currentPosition += increment) {
			// swap currentPosition with next position
			final int nextPosition = currentPosition + increment;
			final Object object = originalPositions.get(Integer.toString(nextPosition));
			if (!(object instanceof String)) {
				throw new IllegalStateException("No usable information for moving position(" + nextPosition + "): " + object);
			}
			positionsStoreEditor.putString(Integer.toString(currentPosition), (String) object);
		}
		positionsStoreEditor.putString(Integer.toString(newPosition), (String) busStopToMove);
	}
}
