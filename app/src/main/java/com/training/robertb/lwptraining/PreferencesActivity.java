package com.training.robertb.lwptraining;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import static android.preference.Preference.*;

/**
 * Created by RobertB on 2/22/2015.
 */
public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        // add a validator to the "numberofCircles" preference so that it only
        // accepts numbers
        Preference circlePreference = getPreferenceScreen().findPreference("numberOfCircles");

        // add the validator
        circlePreference.setOnPreferenceChangeListener(numberCheckListener);
    }


    /**
     * Checks that a preference is a valid numerical value
     */

    OnPreferenceChangeListener numberCheckListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            // check that the string is an integer
            if (newValue != null && newValue.toString().length() > 0
                    && newValue.toString().matches("\\d*")) {
                return true;
            }
            // If now create a message to the user
            Toast.makeText(PreferencesActivity.this, "Invalid Input", Toast.LENGTH_SHORT).show();
            return false;
        }
    };
}
