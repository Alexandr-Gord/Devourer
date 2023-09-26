package com.example.opengl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.graphics.PointF
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View

class GameView3D(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {
    var mode = NONE
    var last = PointF() //point of end of touch in current frame
    var start = PointF() //point of start touch on a screen
    var scaleDetector: ScaleGestureDetector? = null
    var minScale = 0.3f
    var maxScale = 3f
    var currentScale = 1.0f
    @JvmField
    var betweenTileCentersX = SPRITE_WIDTH * 0.75f
    @JvmField
    var betweenTileCentersY = SPRITE_HEIGHT.toFloat()
    @JvmField
    var openGLRenderer: OpenGLRenderer
    var viewWidth = 0
    var viewHeight //size of imageView
            = 0
    private var translateX = 0f
    private var translateY = 0f
    private var leftTranslateBound = 0f
    private var rightTranslateBound = 0f
    private var topTranslateBound = 0f
    private var bottomTranslateBound = 0f
    @JvmField
    var testMode = false

    init {
        setEGLContextClientVersion(3) // OpenGL ES version 3.0
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        holder.setFormat(PixelFormat.RGBA_8888)
        //getHolder().setFormat(PixelFormat.TRANSLUCENT);
        openGLRenderer = OpenGLRenderer(context)
        setRenderer(openGLRenderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        //setZOrderMediaOverlay(true);
        init(context)
    }

    fun runTest() {
        //////////////////////////////////////////////////////////////////////
    }

    private fun init(context: Context) {
        super.setClickable(true)
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
        setOnTouchListener(TouchListener())
    }

    val touchGlobalX: Float
        get() = (start.x - translateX) / currentScale
    val touchGlobalY: Float
        get() = (viewHeight - start.y - translateY) / currentScale

    private inner class TouchListener() : OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            scaleDetector!!.onTouchEvent(event)
            val curr = PointF(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    translateX += deltaX
                    translateY -= deltaY
                    fixTranslate()
                    Game.instance.setMessage1("maxTransX:" + Game.instance.tileMapHeight)
                    queueEvent(Runnable
                    // This method will be called on the rendering
                    // thread:
                    { openGLRenderer.setTranslate(translateX, translateY) })
                    last[curr.x] = curr.y
                }

                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK) {
                        val indPos = calcMapIndex(touchGlobalX, touchGlobalY)
                        Game.instance.tileClickHandler(indPos[0], indPos[1])
                        performClick()
                    }
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }
            return true // indicate event was handled
        }
    }

    private fun calculateTranslateBounds() {
        leftTranslateBound = -SPRITE_WIDTH * currentScale * 0.5f
        rightTranslateBound =
            -(((Game.instance.tileMapWidth - 1) * betweenTileCentersX * currentScale) - leftTranslateBound - viewWidth)
        bottomTranslateBound = -SPRITE_HEIGHT * currentScale * 0.5f
        topTranslateBound =
            -((Game.instance.tileMapHeight) * betweenTileCentersY * currentScale - viewHeight)
    }

    private fun calculateMinScale() {
        val minScaleX =
            viewWidth.toFloat() / ((Game.instance.tileMapWidth - 1) * betweenTileCentersX)
        val minScaleY =
            viewHeight.toFloat() / ((Game.instance.tileMapHeight - 0.5f) * betweenTileCentersY)
        minScale = Math.max(minScaleX, minScaleY)
    }

    private fun fixTranslate() {
        if (translateX > leftTranslateBound) {
            translateX = leftTranslateBound
        }
        if (translateX < rightTranslateBound) {
            translateX = rightTranslateBound
        }
        if (translateY > bottomTranslateBound) {
            translateY = bottomTranslateBound
        }
        if (translateY < topTranslateBound) {
            translateY = topTranslateBound
        }
    }

    private inner class ScaleListener() : SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            if (mScaleFactor != 0f) {
                val origScale = currentScale
                currentScale *= mScaleFactor
                if (currentScale > maxScale) {
                    currentScale = maxScale
                    mScaleFactor = maxScale / origScale
                } else if (currentScale < minScale) {
                    currentScale = minScale
                    mScaleFactor = minScale / origScale
                }
                translateX += (-translateX + detector.focusX) * (1 - mScaleFactor)
                translateY += (-translateY + viewHeight - detector.focusY) * (1 - mScaleFactor)
                calculateTranslateBounds()
                fixTranslate()
                Game.instance
                    .setMessage1("focusX:" + detector.focusX + " focusY:" + detector.focusY)

                //translateY = translateY * ( -mScaleFactor) - detector.getFocusY() * (1 - mScaleFactor);
                //bitmapTransY = -transMatrix[Matrix.MTRANS_Y] * mScaleFactor - detector.getFocusY() * (1 - mScaleFactor);
                queueEvent(object : Runnable {
                    override fun run() {
                        openGLRenderer.setTranslate(translateX, translateY)
                        openGLRenderer.setCurrentScale(currentScale)
                    }
                })
            }
            return true
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
    override fun onResume() {
        super.onResume()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        calculateTranslateBounds()
        calculateMinScale()
        translateX = (leftTranslateBound + rightTranslateBound) * 0.5f
        translateY = (topTranslateBound + bottomTranslateBound) * 0.5f
        queueEvent(object : Runnable {
            override fun run() {
                openGLRenderer.setTranslate(translateX, translateY)
                openGLRenderer.setCurrentScale(currentScale)
            }
        })
    }

    fun calcMapIndex(pointX: Float, pointY: Float): IntArray {
        val mapInd = IntArray(2)
        val dBetweenTileCentersX = betweenTileCentersX + betweenTileCentersX
        val rectIndX = (pointX / dBetweenTileCentersX).toInt()
        val rectIndY = (pointY / betweenTileCentersY).toInt()
        val nearCenters = Array(5) { FloatArray(3) }
        nearCenters[0][0] = rectIndX * dBetweenTileCentersX
        nearCenters[0][1] = rectIndY * betweenTileCentersY
        nearCenters[0][2] = square(nearCenters[0][0] - pointX) + square(nearCenters[0][1] - pointY)
        nearCenters[1][0] = rectIndX * dBetweenTileCentersX + dBetweenTileCentersX
        nearCenters[1][1] = rectIndY * betweenTileCentersY
        nearCenters[1][2] = square(nearCenters[1][0] - pointX) + square(nearCenters[1][1] - pointY)
        nearCenters[2][0] = rectIndX * dBetweenTileCentersX + dBetweenTileCentersX
        nearCenters[2][1] = rectIndY * betweenTileCentersY + betweenTileCentersY
        nearCenters[2][2] = square(nearCenters[2][0] - pointX) + square(nearCenters[2][1] - pointY)
        nearCenters[3][0] = rectIndX * dBetweenTileCentersX
        nearCenters[3][1] = rectIndY * betweenTileCentersY + betweenTileCentersY
        nearCenters[3][2] = square(nearCenters[3][0] - pointX) + square(nearCenters[3][1] - pointY)
        nearCenters[4][0] = rectIndX * dBetweenTileCentersX + betweenTileCentersX
        nearCenters[4][1] = rectIndY * betweenTileCentersY + betweenTileCentersY / 2
        nearCenters[4][2] = square(nearCenters[4][0] - pointX) + square(nearCenters[4][1] - pointY)
        var minInd = 0
        var min = nearCenters[0][2]
        for (i in 1..4) {
            if (nearCenters[i][2] < min) {
                min = nearCenters[i][2]
                minInd = i
            }
        }
        mapInd[0] = (nearCenters[minInd][0] / betweenTileCentersX).toInt() - 1
        if ((mapInd[0] % 2) == 0) {
            mapInd[1] = (nearCenters[minInd][1] / betweenTileCentersY).toInt()
        } else {
            //mapInd[1] = (int) ((nearCenters[minInd][1] - betweenTileCentersY / 2) / betweenTileCentersY) + 1;
            mapInd[1] =
                ((nearCenters[minInd][1] - betweenTileCentersY / 2) / betweenTileCentersY).toInt()
        }
        //showMessage("indX:" + mapInd[0] + " indY:" + mapInd[1] + " tile:" + tileMap[mapInd[0]][mapInd[1]].entity);
        return mapInd
    }

    fun square(`var`: Float): Float {
        return `var` * `var`
    }

    companion object {
        val NONE = 0
        val DRAG = 1
        val ZOOM = 2
        val CLICK = 5
        val SPRITE_WIDTH = 149
        val SPRITE_HEIGHT = 129
    }
}