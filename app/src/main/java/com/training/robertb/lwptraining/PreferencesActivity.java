package com.training.robertb.lwptraining;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;

import org.androidannotations.annotations.EActivity;

/**
 * Created by RobertB on 2/22/2015.
 */
@EActivity
public class PreferencesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActionBar() != null) {
            getActionBar().setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.apptheme_color));
            getActionBar().setTitle(
                    Html.fromHtml("<font color=\"#FFFFFF\">" +
                            getString(R.string.action_settings) + "</font>"));
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment()).commit();
    }
}
