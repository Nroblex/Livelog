package se.selborn.livelog;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by anders.selborn on 2014-02-28.
 */
public class AppSettings extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.activity_appsettings);

        //EditTextPreference txPref = (EditTextPreference) findViewById(R.id.)


    }
}