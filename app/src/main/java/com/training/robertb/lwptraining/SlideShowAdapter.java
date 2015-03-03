package com.training.robertb.lwptraining;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by RobertB on 2/26/2015.
 */
public class SlideShowAdapter extends PagerAdapter {
    Context context;
    LayoutInflater inflater;
    String[] imagePaths;

    private final Display mDisplay;
    private final int requiredWidth;
    private final int requiredHeight;

    public SlideShowAdapter(Context context, String[] imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        WindowManager windowManager =
                (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mDisplay = windowManager.getDefaultDisplay();
        requiredWidth = mDisplay.getWidth();
        requiredHeight = mDisplay.getHeight();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int virtualPosition = position % getRealCount();

        return instantiateVirtualItem(container, virtualPosition);
    }

    public Object instantiateVirtualItem(ViewGroup container, final int position) {
        View item = inflater.inflate(R.layout.slide, container, false);

        ImageView imageView = (ImageView) item.findViewById(R.id.slide);
        Set<Uri> uris = new HashSet<>();
        String path;
        for (String imagePath : imagePaths) {
            path = imagePath;
            if (!TextUtils.isEmpty(path)) {
                uris.add(Uri.parse(path));
            }
        }

        List<Uri> listUris = new ArrayList<>(uris);
        File file = new File(listUris.get(position).getPath());
        imageView.setImageBitmap(optimizeBitmap(file));

        container.addView(item);

        return item;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public int getRealCount(){
        return imagePaths.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        int virtualPosition = position % getRealCount();
        destroyVirtualItem(container, virtualPosition, object);
    }

    public void destroyVirtualItem(ViewGroup container, int position, Object object){
        container.removeView((ImageView) object);
    }

    private Bitmap optimizeBitmap(File pictureFile) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pictureFile.getPath(), options);

        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = calculateInSampleSize(options);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(pictureFile.getPath(), options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > requiredHeight || width > requiredWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > requiredHeight
                    && (halfWidth / inSampleSize) > requiredWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
