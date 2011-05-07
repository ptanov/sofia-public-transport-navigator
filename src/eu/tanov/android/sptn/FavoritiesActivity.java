package eu.tanov.android.sptn;

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

public class FavoritiesActivity extends ListActivity {
	final BusStopItem[] listItems = { new BusStopItem(1, 11, "aa"),
			new BusStopItem(2, 22, "bb"), };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.favorities);
		setListAdapter(new ArrayAdapter<BusStopItem>(this,
				android.R.layout.simple_list_item_1, listItems));
		registerForContextMenu(findViewById(android.R.id.list));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.favorities_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.menu_favorities_up:
			return true;
		case R.id.menu_favorities_down:
			return true;
		case R.id.menu_favorities_remove:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final BusStopItem selectedBusStop = (BusStopItem) getListView().getItemAtPosition(position);
		//TODO how to pass position? maybe using intent?
//		getIntent().putExtra("code", selectedBusStop.getCode());
		//from http://stackoverflow.com/questions/2497205/how-to-return-a-result-startactivityforresult-from-a-tabhost-activity:
		if (getParent() == null) {
			setResult(RESULT_FIRST_USER+selectedBusStop.getCode());
		} else {
		    getParent().setResult(RESULT_FIRST_USER+selectedBusStop.getCode());
		}
		finish();
	}
}
