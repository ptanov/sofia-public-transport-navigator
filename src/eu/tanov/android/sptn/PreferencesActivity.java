package eu.tanov.android.sptn;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

import eu.tanov.android.sptn.util.LocaleHelper;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.selectLocale(this);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
        FlurryAgent.onStartSession(this, LocationView.FLIRRY_ID);
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                LocationView.PREFERENCE_KEY_STATISTICS_DISABLE,
                LocationView.PREFERENCE_DEFAULT_VALUE_STATISTICS_DISABLE)) {
            FlurryAgent.onStartSession(this, LocationView.FLIRRY_ID);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
        FlurryAgent.onEndSession(this);
    }
}
