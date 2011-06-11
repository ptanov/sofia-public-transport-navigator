package eu.tanov.android.sptn;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
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

public class FavoritiesActivity extends ListActivity {
	private final List<BusStopItem> busStops = new ArrayList<BusStopItem>();
	private ArrayAdapter<BusStopItem> arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.favorities);
		arrayAdapter = new ArrayAdapter<BusStopItem>(this, android.R.layout.simple_list_item_1, busStops);
		refreshContent();
		setListAdapter(arrayAdapter);
		registerForContextMenu(findViewById(android.R.id.list));
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

	private void setNewLabel(final int code, String oldLabel) {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		input.setText(oldLabel);
		alert.setView(input);
		alert.setPositiveButton(R.string.buttonOk, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				final String newLabel = input.getText().toString();
				getFavoritiesService().rename(Integer.toString(code), newLabel);

				refreshContent();
			}
		});

		alert.show();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final BusStopItem selectedBusStop = (BusStopItem) getListView().getItemAtPosition(position);
		// TODO how to pass position? maybe using intent?
		// getIntent().putExtra("code", selectedBusStop.getCode());
		// from http://stackoverflow.com/questions/2497205/how-to-return-a-result-startactivityforresult-from-a-tabhost-activity:
		if (getParent() == null) {
			setResult(RESULT_FIRST_USER + selectedBusStop.getCode());
		} else {
			getParent().setResult(RESULT_FIRST_USER + selectedBusStop.getCode());
		}
		finish();
	}
}