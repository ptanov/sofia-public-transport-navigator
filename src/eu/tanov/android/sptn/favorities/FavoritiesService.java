package eu.tanov.android.sptn.favorities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import eu.tanov.android.sptn.R;

import android.content.Context;
import android.content.SharedPreferences;

public class FavoritiesService {

	private static final String SHARED_PREFERENCES_NAME_FAVORITIES_POSITIONS_TO_CODE = "favorities_positions";
	private static final String SHARED_PREFERENCES_NAME_FAVORITIES_CODE_TO_LABELS = "favorities_labels";
	private final Context context;

	private static Comparator<BusStopItem> comparator = new Comparator<BusStopItem>() {
		@Override
		public int compare(BusStopItem object1, BusStopItem object2) {
			return object1.getPosition() - object2.getPosition();
		}
	};

	public FavoritiesService(Context context) {
		this.context = context;
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

	private BusStopItem busStopFromEntry(Entry<String, ?> positionEntry, Map<String, ?> allLabels) {
		final String position = positionEntry.getKey();
		final Object code = positionEntry.getValue();
		if (!(code instanceof String)) {
			throw new IllegalStateException("Bus stop code should be integer, not: " + code);
		}

		Object label = allLabels.get(code.toString());
		if (label == null) {
		    label = context.getString(R.string.favorities_null_label);
		}
		if (!(label instanceof String)) {
			throw new IllegalStateException("Label for favorite bus stop <" + code + "> should be String, but was: " + label);
		}
		return new BusStopItem(Integer.valueOf(position), (String) code, (String) label);
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
		labelsStoreEditor.remove(busStop.getCode());

		if (positionsStoreEditor.commit()) {
			labelsStoreEditor.commit();
		}
	}

	public boolean isFavorite(String code) {
		final SharedPreferences labelsStore = getLabelsStore();
		final String value = labelsStore.getString(code, null);
		return value != null;
	}

	public void add(BusStopItem busStop) {
		final SharedPreferences positionsStore = getPositionsStore();
		final SharedPreferences.Editor positionsStoreEditor = positionsStore.edit();
		final SharedPreferences.Editor labelsStoreEditor = getLabelsStore().edit();

		final int newPosition = positionsStore.getAll().size();
		positionsStoreEditor.putString(Integer.toString(newPosition), busStop.getCode());
		labelsStoreEditor.putString(busStop.getCode(), busStop.getLabel());

		if (positionsStoreEditor.commit()) {
			labelsStoreEditor.commit();
		}
	}

	public void rename(String code, String newLabel) {
		final SharedPreferences labelsStore = getLabelsStore();
		final String oldName = labelsStore.getString(code, null);
		if (oldName == null) {
			throw new IllegalArgumentException(String.format("Station %s is not in favorities (newLabel: %s)", code, newLabel));
		}
		final SharedPreferences.Editor labelsStoreEditor = labelsStore.edit();
		labelsStoreEditor.putString(code, newLabel);

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
