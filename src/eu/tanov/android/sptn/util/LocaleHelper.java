package eu.tanov.android.sptn.util;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

public class LocaleHelper {
    
    private static final String PREFERENCE_KEY_USER_LOCALE_ENGLISH = "userLocaleEnglish";
    private static final boolean PREFERENCE_DEFAULT_VALUE_USER_LOCALE_ENGLISH = false;

    /**
     * utility class - no instance
     */
    private LocaleHelper() {
    }

    public static String getUserLocale(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        final boolean useEnglish = settings.getBoolean(PREFERENCE_KEY_USER_LOCALE_ENGLISH, PREFERENCE_DEFAULT_VALUE_USER_LOCALE_ENGLISH);
            
        return (useEnglish)?"en":"bg";
    }
    public static void selectLocale(Activity context) {
        Locale locale = new Locale(getUserLocale(context));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getBaseContext().getResources()
                .updateConfiguration(config, context.getBaseContext().getResources().getDisplayMetrics());
    }

}
