package com.example.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import com.example.opengl.Game.Companion.instance

class SelectButton(context: Context?, attrs: AttributeSet?) : AppCompatImageButton(
    context!!, attrs
) {
    enum class FlashEnum {
        BUILD, VIEW, DELETE
    }

    private val images: IntArray
    private val bitmaps: Array<Bitmap?>
    private val btnBitmap: Bitmap
    var canvas: Canvas
    private val imageWidth: Int
    private val imageHeight: Int
    private fun onView() {
        instance.onBtnViewClickHandler()
    }

    private fun onBuild() {
        instance.onBtnBuildClickHandler()
    }

    private fun onDelete() {
        instance.onBtnDeleteClickHandler()
    }

    private var state: FlashEnum? = null

    init {
        super.setClickable(true)
        setState(FlashEnum.VIEW)
        setOnTouchListener(TouchListener())
        images = IntArray(FlashEnum.values().size)
        images[0] = R.drawable.add_btn
        images[1] = R.drawable.click_btn
        images[2] = R.drawable.delete_btn
        bitmaps = arrayOfNulls(images.size)
        for (i in images.indices) {
            bitmaps[i] = BitmapFactory.decodeResource(resources, images[i])
        }
        imageWidth = (bitmaps[0]!!.width * (images.size + SELECTED_BUTTON_SIZE_FACTOR - 1)).toInt()
        imageHeight = (bitmaps[0]!!.height * SELECTED_BUTTON_SIZE_FACTOR).toInt()
        btnBitmap = Bitmap.createBitmap(imageWidth, imageHeight, bitmaps[0]!!.config)
        canvas = Canvas(btnBitmap)
        setButtonImage()
    }

    private fun setButtonImage() {
        btnBitmap.eraseColor(Color.TRANSPARENT)
        val originalWidth = bitmaps[0]!!.width
        val originalHeight = bitmaps[0]!!.height
        val selectedWidth = (originalWidth * SELECTED_BUTTON_SIZE_FACTOR).toInt()
        val selectedHeight = (originalHeight * SELECTED_BUTTON_SIZE_FACTOR).toInt()
        var offsetX = 0
        val offsetY = (bitmaps[0]!!.height * (SELECTED_BUTTON_SIZE_FACTOR - 1) * 0.5f).toInt()
        val rectSrc = Rect(0, 0, originalWidth, originalHeight)
        var rectDst: Rect
        for (i in images.indices) {
            if (state!!.ordinal == i) { // selected
                rectDst = Rect(0, 0, selectedWidth, selectedHeight)
                rectDst.offset(offsetX, 0)
                canvas.drawBitmap(bitmaps[i]!!, rectSrc, rectDst, null)
                offsetX += selectedWidth
            } else {
                rectDst = Rect(0, 0, originalWidth, originalHeight)
                rectDst.offset(offsetX, offsetY)
                canvas.drawBitmap(
                    changeBitmapContrastBrightness(bitmaps[i], 1f, -110f),
                    rectSrc,
                    rectDst,
                    null
                )
                offsetX += originalWidth
            }
        }
        setImageBitmap(btnBitmap)
    }

    private fun performSelectClick() {
        when (state) {
            FlashEnum.VIEW -> onView()
            FlashEnum.BUILD -> onBuild()
            FlashEnum.DELETE -> onDelete()
            else -> {}
        }
    }

    private inner class TouchListener : OnTouchListener {
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {}
                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    val next = getStateByTouchPos(event.x)
                    if (next != state!!.ordinal) {
                        setState(FlashEnum.values()[next])
                        setButtonImage()
                        performSelectClick()
                    }
                }
                else -> {}
            }
            return true
        }
    }

    private fun getStateByTouchPos(posX: Float): Int {
        var result = 0
        var boundX = 0
        val scale = width.toFloat() / imageWidth
        for (i in images.indices) {
            boundX += if (state!!.ordinal == i) { // selected
                (bitmaps[i]!!.width * SELECTED_BUTTON_SIZE_FACTOR * scale).toInt()
            } else {
                (bitmaps[i]!!.width * scale).toInt()
            }
            if (posX < boundX) {
                return result
            }
            result++
        }
        return --result
    }

    fun setState(state: FlashEnum?) {
        if (state == null) {
            return
        }
        this.state = state
    }

    fun getState(): FlashEnum? {
        return state
    }

    companion object {
        private const val SELECTED_BUTTON_SIZE_FACTOR = 1.5f

        // contrast 0..10 1 is default
        // brightness -255..255 0 is default
        fun changeBitmapContrastBrightness(
            bmp: Bitmap?,
            contrast: Float,
            brightness: Float
        ): Bitmap {
            val cm = ColorMatrix(
                floatArrayOf(
                    contrast, 0f, 0f, 0f, brightness,
                    0f, contrast, 0f, 0f, brightness,
                    0f, 0f, contrast, 0f, brightness,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            val ret = Bitmap.createBitmap(bmp!!.width, bmp.height, bmp.config)
            val canvas = Canvas(ret)
            val paint = Paint()
            paint.colorFilter = ColorMatrixColorFilter(cm)
            canvas.drawBitmap(bmp, 0f, 0f, paint)
            return ret
        }
    }
}