package eu.tanov.android.sptn;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import eu.tanov.android.sptn.favorities.BusStopItem;
import eu.tanov.android.sptn.favorities.FavoritiesService;
import eu.tanov.android.sptn.util.LocaleHelper;

public class FavoritiesActivity extends ListActivity {
	public static final String EXTRA_CODE_NAME = "code";

	private static final String PREFERENCE_KEY_WHATS_NEW_VERSION1_09 = "whatsNewShowVersion1_09_favorities";
	private static final boolean PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_09 = true;

	private final List<BusStopItem> busStops = new ArrayList<BusStopItem>();
	private ArrayAdapter<BusStopItem> arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        LocaleHelper.selectLocale(this);

		setContentView(R.layout.favorities);
		arrayAdapter = new ArrayAdapter<BusStopItem>(this, android.R.layout.simple_list_item_1, busStops);
		refreshContent();
		setListAdapter(arrayAdapter);
		registerForContextMenu(findViewById(android.R.id.list));

		notifyForChangesInNewVersions();
	}

	private void notifyForChangesInNewVersions() {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if (settings.getBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_09, PREFERENCE_DEFAULT_VALUE_WHATS_NEW_VERSION1_09)) {
			final Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
			// show only first time:
			editor.putBoolean(PREFERENCE_KEY_WHATS_NEW_VERSION1_09, false);
			editor.commit();

			new AlertDialog.Builder(this).setTitle(R.string.versionChanges_1_09_favorities_title).setCancelable(true)
					.setMessage(R.string.versionChanges_1_09_favorities_text)
					.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.dismiss();
						}
					}).create().show();
		}

	}

	private FavoritiesService getFavoritiesService() {
		// XXX how to pass this service across whole application?
		return new FavoritiesService(this);
	}

	private void refreshContent() {
		final FavoritiesService favoritiesService = getFavoritiesService();
		busStops.clear();
		busStops.addAll(favoritiesService.getAll());

		arrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.favorities_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_favorities_up:
			getFavoritiesService().move(arrayAdapter.getItem(info.position), -1);

			refreshContent();
			return true;
		case R.id.menu_favorities_down:
			getFavoritiesService().move(arrayAdapter.getItem(info.position), +1);

			refreshContent();
			return true;
		case R.id.menu_favorities_remove:
			final BusStopItem busStop = arrayAdapter.getItem(info.position);
			getFavoritiesService().remove(busStop);

			final String message = getResources().getString(R.string.info_removedFromFavorities, busStop.getLabel(), busStop.getCode());
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();

			refreshContent();
			return true;
		case R.id.menu_favorities_rename:
			final BusStopItem selected = arrayAdapter.getItem(info.position);
			setNewLabel(selected.getCode(), selected.getLabel());

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setNewLabel(final String code, String oldLabel) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setText(oldLabel);
		alert.setView(input);
		alert.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String newLabel = input.getText().toString();
				getFavoritiesService().rename(code, newLabel);

				refreshContent();
			}
		});

		alert.show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final BusStopItem selectedBusStop = (BusStopItem) getListView().getItemAtPosition(position);
		final Intent data = getIntent();
		data.putExtra(EXTRA_CODE_NAME, selectedBusStop.getCode());

		if (getParent() == null) {
			setResult(RESULT_OK, data);
		} else {
			getParent().setResult(RESULT_OK, data);
		}
		finish();
	}
}
