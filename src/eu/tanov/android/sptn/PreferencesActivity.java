package eu.tanov.android.sptn;

import eu.tanov.android.sptn.util.LocaleHelper;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity {
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        LocaleHelper.selectLocale(this);

	        addPreferencesFromResource(R.xml.preferences);
	    }
}
