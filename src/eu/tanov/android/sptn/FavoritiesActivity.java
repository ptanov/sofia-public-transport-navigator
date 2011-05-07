package eu.tanov.android.sptn;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
			getFavoritiesService().remove(arrayAdapter.getItem(info.position));

			refreshContent();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
