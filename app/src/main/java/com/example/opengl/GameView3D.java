package com.example.opengl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class GameView3D extends GLSurfaceView {
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    static final int CLICK = 5;
    int mode = NONE;
    PointF last = new PointF();  //point of end of touch in current frame
    PointF start = new PointF(); //point of start touch on a screen
    ScaleGestureDetector scaleDetector;
    float minScale = 0.3f;
    float maxScale = 3f;
    float currentScale = 1.0f;
    public static final int SPRITE_WIDTH = 149;
    public static final int SPRITE_HEIGHT = 129;
    public float betweenTileCentersX = SPRITE_WIDTH * 0.75f;
    public float betweenTileCentersY = SPRITE_HEIGHT;
    public OpenGLRenderer openGLRenderer;
    int viewWidth, viewHeight; //size of imageView
    private float translateX, translateY;
    private float leftTranslateBound, rightTranslateBound, topTranslateBound, bottomTranslateBound;
    public boolean testMode = false;

    public GameView3D(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(3); // OpenGL ES version 3.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        //getHolder().setFormat(PixelFormat.TRANSLUCENT);
        openGLRenderer = new OpenGLRenderer(context);
        setRenderer(openGLRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        //setZOrderMediaOverlay(true);
        init(context);

    }

    public void runTest() {
        //////////////////////////////////////////////////////////////////////
    }

    private void init(Context context) {
        super.setClickable(true);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        setOnTouchListener(new TouchListener());
    }

    public float getTouchGlobalX() {
        return (start.x - translateX) / currentScale;
    }

    public float getTouchGlobalY() {
        return (viewHeight - start.y - translateY) / currentScale;
    }

    private class TouchListener implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            scaleDetector.onTouchEvent(event);

            PointF curr = new PointF(event.getX(), event.getY());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(curr);
                    start.set(last);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        translateX += deltaX;
                        translateY -= deltaY;
                        fixTranslate();

                        Game.getInstance().setMessage1("maxTransX:" + Game.getInstance().tileMapHeight);

                        queueEvent(new Runnable() {
                            // This method will be called on the rendering
                            // thread:
                            public void run() {
                                openGLRenderer.setTranslate(translateX, translateY);
                            }
                        });
                        last.set(curr.x, curr.y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK && yDiff < CLICK) {
                        int[] indPos = calcMapIndex(getTouchGlobalX(), getTouchGlobalY());
                        Game.getInstance().tileClickHandler(indPos[0], indPos[1]);
                        performClick();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
            }
            return true; // indicate event was handled
        }
    }

    private void calculateTranslateBounds() {
        leftTranslateBound = -SPRITE_WIDTH * currentScale * 0.5f;
        rightTranslateBound = -((Game.getInstance().tileMapWidth - 1) * betweenTileCentersX * currentScale - leftTranslateBound - viewWidth);
        bottomTranslateBound = -SPRITE_HEIGHT * currentScale * 0.5f;
        topTranslateBound = -((Game.getInstance().tileMapHeight) * betweenTileCentersY * currentScale - viewHeight);
    }

    private void calculateMinScale() {
        float minScaleX = (float) viewWidth / ((Game.getInstance().tileMapWidth - 1) * betweenTileCentersX);
        float minScaleY = (float) viewHeight / ((Game.getInstance().tileMapHeight - 0.5f) * betweenTileCentersY);
        minScale = Math.max(minScaleX, minScaleY);
    }

    private void fixTranslate() {
        if (translateX > leftTranslateBound) {
            translateX = leftTranslateBound;
        }

        if (translateX < rightTranslateBound) {
            translateX = rightTranslateBound;
        }


        if (translateY > bottomTranslateBound) {
            translateY = bottomTranslateBound;
        }

        if (translateY < topTranslateBound) {
            translateY = topTranslateBound;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mode = ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float mScaleFactor = detector.getScaleFactor();

            if (mScaleFactor != 0) {
                float origScale = currentScale;

                currentScale *= mScaleFactor;
                if (currentScale > maxScale) {
                    currentScale = maxScale;
                    mScaleFactor = maxScale / origScale;
                } else if (currentScale < minScale) {
                    currentScale = minScale;
                    mScaleFactor = minScale / origScale;
                }
                translateX += (-translateX + detector.getFocusX()) * (1 - mScaleFactor);
                translateY += (-translateY + viewHeight - detector.getFocusY()) * (1 - mScaleFactor);

                calculateTranslateBounds();
                fixTranslate();

                Game.getInstance().setMessage1("focusX:" + detector.getFocusX() + " focusY:" + detector.getFocusY());

                //translateY = translateY * ( -mScaleFactor) - detector.getFocusY() * (1 - mScaleFactor);
                //bitmapTransY = -transMatrix[Matrix.MTRANS_Y] * mScaleFactor - detector.getFocusY() * (1 - mScaleFactor);
                queueEvent(new Runnable() {
                    public void run() {
                        openGLRenderer.setTranslate(translateX, translateY);
                        openGLRenderer.setCurrentScale(currentScale);
                    }
                });
            }
            return true;
        }
    }

    /*
    public void redraw() {
        queueEvent(new Runnable() {
            // This method will be called on the rendering
            // thread:
            public void run() {
                //int globalMapWidth = Game.getInstance().tileMapWidth;
                //int globalMapHeight = Game.getInstance().tileMapHeight;
                openGLRenderer.prepareTileData(Game.getInstance().createRenderDataTile());
                openGLRenderer.prepareMovingData(Game.getInstance().createRenderDataMoving());
            }
        });
    }
     */

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        viewHeight = MeasureSpec.getSize(heightMeasureSpec);
        calculateTranslateBounds();
        calculateMinScale();
        translateX = (leftTranslateBound + rightTranslateBound) * 0.5f;
        translateY = (topTranslateBound + bottomTranslateBound) * 0.5f;
        queueEvent(new Runnable() {
            public void run() {
                openGLRenderer.setTranslate(translateX, translateY);
                openGLRenderer.setCurrentScale(currentScale);
            }
        });
    }

    public int[] calcMapIndex(float pointX, float pointY) {
        int[] mapInd = new int[2];

        float dBetweenTileCentersX = betweenTileCentersX + betweenTileCentersX;
        int rectIndX = (int) (pointX / dBetweenTileCentersX);
        int rectIndY = (int) (pointY / betweenTileCentersY);

        float[][] nearCenters = new float[5][3];

        nearCenters[0][0] = rectIndX * dBetweenTileCentersX;
        nearCenters[0][1] = rectIndY * betweenTileCentersY;
        nearCenters[0][2] = square(nearCenters[0][0] - pointX) + square(nearCenters[0][1] - pointY);

        nearCenters[1][0] = rectIndX * dBetweenTileCentersX + dBetweenTileCentersX;
        nearCenters[1][1] = rectIndY * betweenTileCentersY;
        nearCenters[1][2] = square(nearCenters[1][0] - pointX) + square(nearCenters[1][1] - pointY);

        nearCenters[2][0] = rectIndX * dBetweenTileCentersX + dBetweenTileCentersX;
        nearCenters[2][1] = rectIndY * betweenTileCentersY + betweenTileCentersY;
        nearCenters[2][2] = square(nearCenters[2][0] - pointX) + square(nearCenters[2][1] - pointY);

        nearCenters[3][0] = rectIndX * dBetweenTileCentersX;
        nearCenters[3][1] = rectIndY * betweenTileCentersY + betweenTileCentersY;
        nearCenters[3][2] = square(nearCenters[3][0] - pointX) + square(nearCenters[3][1] - pointY);

        nearCenters[4][0] = rectIndX * dBetweenTileCentersX + betweenTileCentersX;
        nearCenters[4][1] = rectIndY * betweenTileCentersY + betweenTileCentersY / 2;
        nearCenters[4][2] = square(nearCenters[4][0] - pointX) + square(nearCenters[4][1] - pointY);

        int minInd = 0;
        float min = nearCenters[0][2];
        for (int i = 1; i < 5; i++) {
            if (nearCenters[i][2] < min) {
                min = nearCenters[i][2];
                minInd = i;
            }
        }

        mapInd[0] = (int) (nearCenters[minInd][0] / betweenTileCentersX) - 1;
        if ((mapInd[0] % 2) == 0) {
            mapInd[1] = (int) (nearCenters[minInd][1] / betweenTileCentersY);
        } else {
            //mapInd[1] = (int) ((nearCenters[minInd][1] - betweenTileCentersY / 2) / betweenTileCentersY) + 1;
            mapInd[1] = (int) ((nearCenters[minInd][1] - betweenTileCentersY / 2) / betweenTileCentersY);
        }
        //showMessage("indX:" + mapInd[0] + " indY:" + mapInd[1] + " tile:" + tileMap[mapInd[0]][mapInd[1]].entity);
        return mapInd;
    }

    public float square(float var) {
        return var * var;
    }

}
