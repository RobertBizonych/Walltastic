package com.training.robertb.lwptraining;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Set;

/**
 * Created by RobertB on 2/24/2015.
 */
@EActivity(R.layout.grid)
public class GridActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsSelection;
    private String[] arrPath;
    private SharedPreferences preferences;
    private ImageAdapter imageAdapter;

    @ViewById
    protected GridView phoneImageGrid;

    @AfterViews
    final void init() {
        getLoaderManager().initLoader(0, null, this);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @UiThread
    protected void populateGrid(Cursor imageCursor) {
        this.count = imageCursor.getCount();
        this.thumbnails = new Bitmap[this.count];
        this.arrPath = new String[this.count];
        this.thumbnailsSelection = new boolean[this.count];

        int imageColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);

        for (int i = 0; i < this.count; i++) {
            imageCursor.moveToPosition(i);
            int id = imageCursor.getInt(imageColumnIndex);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(
                    getApplicationContext().getContentResolver(), id,
                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
            arrPath[i] = imageCursor.getString(dataColumnIndex);
        }
        imageAdapter = new ImageAdapter();
        phoneImageGrid.setAdapter(imageAdapter);
    }

    @UiThread
    protected void setupThumbnailsSelection() {
        Set<String> savedImages = preferences.getStringSet("images", Collections.EMPTY_SET);
        for (int i = 0; i < arrPath.length; i++) {
            thumbnailsSelection[i] = savedImages.contains(arrPath[i]);
        }
        imageAdapter.notifyDataSetChanged();
    }

    @Click
    final void clearSelection() {
        if (arrPath != null) {
            preferences.edit().putStringSet("images", Collections.EMPTY_SET).apply();
            setupThumbnailsSelection();
        }
    }

    @Click
    final void selectImages() {
        final int length = thumbnailsSelection.length;
        int selectedCount = 0;
        String selectImages = "";
        String[] selectedPaths = new String[length];
        for (int i = 0; i < length; i++) {
            if (thumbnailsSelection[i]) {
                selectedCount++;
                selectImages = selectImages + arrPath[i] + "|";
                selectedPaths[i] = arrPath[i];
            }
        }
        if (selectedCount == 0) {
            Toast.makeText(getApplicationContext(),
                    "Please select at least one image",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    "You've selected Total " + selectedCount + " image(s).",
                    Toast.LENGTH_LONG).show();
            Log.d("SelectedImages", selectImages);

            Intent passSelection = new Intent();
            passSelection.putExtra("images", selectedPaths);
            setResult(Activity.RESULT_OK, passSelection);
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media._ID;
        return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                columns, null, null, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        populateGrid(data);
        setupThumbnailsSelection();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.grid_item, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (thumbnailsSelection[id]) {
                        cb.setChecked(false);
                        thumbnailsSelection[id] = false;
                    } else {
                        cb.setChecked(true);
                        thumbnailsSelection[id] = true;
                    }
                }
            });
            holder.imageview.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    int id = v.getId();
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + arrPath[id]), "image/*");
                    startActivity(intent);
                }
            });
            holder.imageview.setImageBitmap(thumbnails[position]);
            holder.checkbox.setChecked(thumbnailsSelection[position]);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }
}
