package com.example.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SelectButton extends androidx.appcompat.widget.AppCompatImageButton {
    public enum FlashEnum {
        BUILD, VIEW, DELETE
    }

    private static final float SELECTED_BUTTON_SIZE_FACTOR = 1.5f;
    private final int[] images;
    private final Bitmap[] bitmaps;
    private final Bitmap btnBitmap;
    Canvas canvas;
    private final int imageWidth;
    private final int imageHeight;

    public void onView() {
        Game.getInstance().mode = Game.Mode.VIEW;
    }

    public void onBuild() {
        Game.getInstance().mode = Game.Mode.BUILD;
    }

    public void onDelete() {
        Game.getInstance().mode = Game.Mode.DELETE;
    }

    private FlashEnum state;

    public SelectButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
        setState(FlashEnum.VIEW);
        setOnTouchListener(new TouchListener());
        images = new int[FlashEnum.values().length];
        images[0] = R.drawable.addbtn;
        images[1] = R.drawable.clickbtn;
        images[2] = R.drawable.deletebtn;
        bitmaps = new Bitmap[images.length];
        for (int i = 0; i < images.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(getResources(), images[i]);
        }
        imageWidth = (int) (bitmaps[0].getWidth() * (images.length + SELECTED_BUTTON_SIZE_FACTOR - 1));
        imageHeight = (int) (bitmaps[0].getHeight() * SELECTED_BUTTON_SIZE_FACTOR);
        btnBitmap = Bitmap.createBitmap(imageWidth, imageHeight, bitmaps[0].getConfig());
        canvas = new Canvas(btnBitmap);
        setButtonImage();
    }

    private void setButtonImage() {
        btnBitmap.eraseColor(Color.TRANSPARENT);
        int originalWidth = bitmaps[0].getWidth();
        int originalHeight = bitmaps[0].getHeight();
        int selectedWidth = (int) (originalWidth * SELECTED_BUTTON_SIZE_FACTOR);
        int selectedHeight = (int) (originalHeight * SELECTED_BUTTON_SIZE_FACTOR);
        int offsetX = 0;
        int offsetY = (int) (bitmaps[0].getHeight() * (SELECTED_BUTTON_SIZE_FACTOR - 1) * 0.5f);
        Rect rectSrc = new Rect(0, 0, originalWidth, originalHeight);
        Rect rectDst;

        for (int i = 0; i < images.length; i++) {
            if (state.ordinal() == i) { // selected
                rectDst = new Rect(0, 0, selectedWidth, selectedHeight);
                rectDst.offset(offsetX, 0);
                canvas.drawBitmap(bitmaps[i], rectSrc, rectDst, null);
                offsetX += selectedWidth;
            } else {
                rectDst = new Rect(0, 0, originalWidth, originalHeight);
                rectDst.offset(offsetX, offsetY);
                canvas.drawBitmap(changeBitmapContrastBrightness(bitmaps[i], 1, -110), rectSrc, rectDst, null);
                offsetX += originalWidth;
            }
        }
        setImageBitmap(btnBitmap);
    }

    private void performSelectClick() {
        switch (state) {
            case VIEW:
                onView();
                break;
            case BUILD:
                onBuild();
                break;
            case DELETE:
                onDelete();
                break;
        }
    }

    private class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //some code....
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    int next = getStateByTouchPos(event.getX());
                    if (next != state.ordinal()) {
                        setState(FlashEnum.values()[next]);
                        setButtonImage();
                        performSelectClick();
                    }
                    break;
                default:
                    break;
            }
            return true;
        }
    }

    private int getStateByTouchPos(float posX) {
        int result = 0;
        int boundX = 0;
        float scale = (float) getWidth() / imageWidth;
        for (int i = 0; i < images.length; i++) {
            if (state.ordinal() == i) { // selected
                boundX += bitmaps[i].getWidth() * SELECTED_BUTTON_SIZE_FACTOR * scale;
            } else {
                boundX += bitmaps[i].getWidth() * scale;
            }
            if (posX < boundX) {
                return result;
            }
            result++;
        }
        return --result;
    }

    public void setState(FlashEnum state) {
        if (state == null) {
            return;
        }
        this.state = state;
    }

    public FlashEnum getState() {
        return state;
    }

    // contrast 0..10 1 is default
    // brightness -255..255 0 is default
    public static Bitmap changeBitmapContrastBrightness(Bitmap bmp, float contrast, float brightness) {
        ColorMatrix cm = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });

        Bitmap ret = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());

        Canvas canvas = new Canvas(ret);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(bmp, 0, 0, paint);

        return ret;
    }
}

