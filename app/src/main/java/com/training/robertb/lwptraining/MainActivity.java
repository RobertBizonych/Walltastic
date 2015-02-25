package com.training.robertb.lwptraining;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.marvinlabs.widget.slideshow.SlideShowView;
import com.marvinlabs.widget.slideshow.adapter.RemoteBitmapAdapter;
import com.marvinlabs.widget.slideshow.adapter.ResourceBitmapAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@EActivity(R.layout.activity_main)
public class MainActivity extends ActionBarActivity {
    @ViewById
    SlideShowView slideShow;
    private static final int INTENT_REQUEST_GET_IMAGES = 13;
    private RemoteBitmapAdapter adapter;
    private String[] imagesUri;

    @AfterViews
    final void init() {
        if (adapter == null) {
            ResourceBitmapAdapter bitmapAdapter =
                    new ResourceBitmapAdapter(this, new int[]{R.drawable.cat_icon});
            slideShow.setAdapter(bitmapAdapter);
            slideShow.play();
        }
    }

    @Click
    public void selectImages() {
        GridActivity_.intent(this).startForResult(INTENT_REQUEST_GET_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == INTENT_REQUEST_GET_IMAGES) {
            imagesUri = data.getStringArrayExtra("images");
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            preferences.edit().putStringSet("images", transform(imagesUri)).apply();

//            playSlideShow();
        }
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

    private void playSlideShow() {
        Collection<String> images = new HashSet<>();

        for (String imageURL : imagesUri) {
            images.add("file://" + imageURL);
        }
        adapter = new RemoteBitmapAdapter(this, images);

        slideShow.setAdapter(adapter);
        slideShow.play();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
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
    protected void onDestroy() {
        super.onDestroy();
        slideShow.stop();
    }

    @Override
    protected void onStop() {
        super.onStop();
        slideShow.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        slideShow.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            slideShow.play();
        }
    }
}
