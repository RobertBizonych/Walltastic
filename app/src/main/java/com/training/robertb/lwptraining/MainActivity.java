package com.training.robertb.lwptraining;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity {
    private static final long SLIDESHOW_IMAGE_DURATION = 3000;
    @ViewById
    protected ViewPager slideShow;
    @ViewById
    protected Button setWallpaper;
    @ViewById
    protected AdView adView;
    private SlideShowAdapter adapter;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private String[] imagesUri;
    private Handler slideShowHandler = new Handler();
    private Runnable runSlideShow = new Runnable() {
        public void run() {
            int position = slideShow.getCurrentItem();
            if (adapter != null) {
                if (position == adapter.getCount()) {
                    slideShow.setCurrentItem(0, false);
                } else {
                    // Second parameter of false turns ViewPager scroll animation off
                    slideShow.setCurrentItem(position + 1, true);
                    slideShowHandler.postDelayed(this, SLIDESHOW_IMAGE_DURATION);
                }
            }
        }
    };

    @AfterViews
    final void init() {
        getSupportActionBar().setBackgroundDrawable(
                getResources().getDrawable(R.drawable.apptheme_color));
        getSupportActionBar().setTitle(
                Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>"));
        slideShow.setPageTransformer(true, new ZoomOutPageTransformer());
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        ArrayList<String> savedImages =
                new ArrayList<>(preferences.getStringSet("images", Collections.EMPTY_SET));
        if (adapter == null) {
            int currentOSVersion = Build.VERSION.SDK_INT;
            if (currentOSVersion >= Build.VERSION_CODES.JELLY_BEAN) {
                //noinspection Resolving API
                slideShow.setBackground(getResources().getDrawable(R.drawable.cat_icon));
            } else {
                slideShow.setBackgroundResource(R.drawable.cat_icon);
            }
        }

        if (!savedImages.isEmpty()) {
            imagesUri = new String[savedImages.size()];
            imagesUri = savedImages.toArray(imagesUri);
            playSlideShow();
        }

        setWallpaper.setEnabled(adapter != null);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private void playSlideShow() {
        slideShow.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        adapter = new SlideShowAdapter(this, imagesUri);
        slideShow.setAdapter(adapter);
    }

    @Click
    public void selectImages() {
        GridActivity_.intent(this).startForResult(INTENT_REQUEST_GET_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == INTENT_REQUEST_GET_IMAGES) {
            ArrayList<String> receivedData = data.getStringArrayListExtra("images");
            imagesUri = new String[receivedData.size()];
            imagesUri = receivedData.toArray(imagesUri);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putStringSet("images", transform(imagesUri)).apply();

            playSlideShow();
        }
        setWallpaper.setEnabled(adapter != null);
    }

    private Set<String> transform(String[] imagesUri) {
        Set<String> images = new HashSet<>();
        for (String imageURL : imagesUri) {
            if (!TextUtils.isEmpty(imageURL)) {
                images.add(imageURL);
            }
        }
        return images;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            PreferencesActivity_.intent(this).start();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click
    public void setWallpaper() {
        Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(this, CustomWallpaperService.class));
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (slideShowHandler != null) {
            slideShowHandler.removeCallbacks(runSlideShow);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        slideShowHandler.postDelayed(runSlideShow, 1000);
    }
}
