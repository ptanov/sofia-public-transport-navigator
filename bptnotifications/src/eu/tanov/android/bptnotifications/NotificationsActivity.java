package eu.tanov.android.bptnotifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class NotificationsActivity extends ActionBarActivity {
    public static final String PROVIDER_SOFIATRAFFIC = "sofiatraffic.bg";
    public static final String PROVIDER_VARNATRAFFIC = "varnatraffic.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        alarm();
    }

    private void alarm() {
        final AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        final Intent i = new Intent();
        i.setAction("eu.tanov.android.sptn.ACTION_FETCH");
//        i.putExtra("provider", "varnatraffic.com");
//        i.putExtra("code", "351");
        i.putExtra("provider", PROVIDER_SOFIATRAFFIC);
        i.putExtra("code", "1914");
//        i.putExtra("label", "");

        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15 * 1000, 24 * 60 * 60 * 1000, pi);
        
        final Intent i2 = new Intent();
        i2.setAction("eu.tanov.android.sptn.ACTION_FETCH");
//        i.putExtra("provider", "varnatraffic.com");
//        i.putExtra("code", "351");
        i2.putExtra("provider", PROVIDER_SOFIATRAFFIC);
        i2.putExtra("code", "1287");
//        i.putExtra("label", "");

        PendingIntent pi2 = PendingIntent.getService(this, 0, i2, PendingIntent.FLAG_UPDATE_CURRENT);
        mgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 15 *60  * 1000, 24 * 60 * 60 * 1000, pi2);
        
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
