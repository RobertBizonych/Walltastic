package com.training.robertb.lwptraining;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.io.File;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Set;

/**
 * Created by RobertB on 2/22/2015.
 */
public class CustomWallpaperService extends WallpaperService {

    @Override
    public Engine onCreateEngine() {
        return new CustomWallpaperEngine(this);
    }

    private class CustomWallpaperEngine extends Engine {
        private final Display mDisplay;
        private final int requiredWidth;
        private final int requiredHeight;
        private final Rect mRect;

        private final Paint paint = new Paint();
        private final ArrayDeque<String> pictureQueue = new ArrayDeque<>();
        private final ImageView slideShow;
        private boolean visible = true;

        private int maxImageCount;
        private Set<String> imagesSet;

        private final SharedPreferences.OnSharedPreferenceChangeListener changeListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if ("maxImageCount".equals(key)) {
                            maxImageCount = Integer.valueOf(sharedPreferences.getString(key, "10"));
                        }
                        if ("images".equals(key)) {
                            imagesSet = sharedPreferences.getStringSet("images", Collections.EMPTY_SET);
                        }
                    }
                };

        private final Handler handler = new Handler();
        private final Runnable drawRunner = new Runnable() {
            @Override
            public void run() {
                draw();
            }
        };
        private Canvas canvas;

        public CustomWallpaperEngine(Context context) {
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(CustomWallpaperService.this);
            preferences.registerOnSharedPreferenceChangeListener(changeListener);
            maxImageCount = Integer.valueOf(preferences.getString("maxImageCount", "10"));
            imagesSet = preferences.getStringSet("images", Collections.EMPTY_SET);
            paint.setAntiAlias(true);
            handler.post(drawRunner);

            WindowManager windowManager = (WindowManager)
                    getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            mDisplay = windowManager.getDefaultDisplay();
            requiredWidth = mDisplay.getWidth();
            requiredHeight = mDisplay.getHeight();

            mRect = new Rect();
            mRect.set(0, 0, mDisplay.getWidth(), mDisplay.getHeight());

            slideShow = new ImageView(context);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            this.visible = visible;
            if (visible) {
                handler.post(drawRunner);
            } else {
                handler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            this.visible = false;
            handler.removeCallbacks(drawRunner);
        }

        private void draw() {
            SurfaceHolder holder = getSurfaceHolder();
            canvas = null;
            try {
                canvas = holder.lockCanvas();
                if (canvas != null && !imagesSet.isEmpty()) {
                    startDraw();
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }

        public void startDraw() {
            if (isInitialState()) {
                createCatQueue();
            }
            drawOnCanvas();
            scheduleNextDrawEvent();
        }

        private void scheduleNextDrawEvent() {
            handler.removeCallbacks(drawRunner);
            if (visible) {
                handler.postDelayed(drawRunner, 1000);
            }
        }

        private void drawOnCanvas() {
            String path = pictureQueue.pop();
            File pictureFile = new File(URI.create("file://" + path));
            if (pictureFile.exists()) {
                slideShow.setBackgroundColor(Color.WHITE);

                //Measure the view at the exact dimensions (otherwise the text won't center correctly)
                int widthSpec = View.MeasureSpec.makeMeasureSpec(mRect.width(),
                        View.MeasureSpec.EXACTLY);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(mRect.height(),
                        View.MeasureSpec.EXACTLY);
                slideShow.measure(widthSpec, heightSpec);

                slideShow.setImageBitmap(optimizeBitmap(pictureFile));

                //Lay the view out at the rect width and height
                slideShow.layout(0, 0, mRect.width(), mRect.height());

                canvas.save();

//                RotateAnimation anim = new RotateAnimation(0f, 350f, 15f, 15f);
//                anim.setInterpolator(new LinearInterpolator());
//                anim.setRepeatCount(Animation.INFINITE);
//                anim.setDuration(700);
//                slideShow.startAnimation(anim);

                slideShow.draw(canvas);
            }
        }

        public void imageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
            final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
            final Animation anim_in  = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
            anim_out.setAnimationListener(new Animation.AnimationListener()
            {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}
                @Override public void onAnimationEnd(Animation animation)
                {
                    v.setImageBitmap(new_image);
                    anim_in.setAnimationListener(new Animation.AnimationListener() {
                        @Override public void onAnimationStart(Animation animation) {}
                        @Override public void onAnimationRepeat(Animation animation) {}
                        @Override public void onAnimationEnd(Animation animation) {}
                    });
                    v.startAnimation(anim_in);
                }
            });
            v.startAnimation(anim_out);
        }

        private Bitmap optimizeBitmap(File pictureFile) {
            // First decode with inJustDecodeBounds=true to check dimensions
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

        private boolean isInitialState() {
            return pictureQueue.isEmpty();
        }

        private void createCatQueue() {
            pictureQueue.addAll(imagesSet);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(CustomWallpaperService.this);
            preferences.unregisterOnSharedPreferenceChangeListener(changeListener);
        }
    }
}
