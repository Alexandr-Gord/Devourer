package com.example.opengl.text2D

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.sqrt


object FontDataStorage {
    private val fonts: MutableMap<String, FontData> = HashMap()
    private val rectangles: MutableList<Rectangle> = ArrayList()
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private var defaultFontData: FontData? = null
    private const val MAX_TEXTURE_HEIGHT = 2048
    private const val MAX_TEXTURE_WIDTH = 2048
    private const val MIN_TEXTURE_WIDTH = 16

    private data class Rectangle(
        var x: Float,
        var y: Float,
        val character: FontData.Companion.Character,
        val symbol: Char,
        val paint: Paint,
        val charsLeftSpaceInTexture: Float,
        val charsRightSpaceInTexture: Float
    )

    fun addFontData(name: String, font: FontData) {
        fonts[name] = font
    }

    fun getFontData(name: String): FontData {
        return fonts[name] ?: defaultFontData!!
    }

    fun removeFontData(name: String) {
        fonts.remove(name)
    }

    fun setDefaultFontData(font: FontData) {
        defaultFontData = font
    }

    fun createFontsBitmap(): Bitmap {
        val square = initRectangles()
        textureHeight = packRectangles()
        val k = square / (textureWidth * textureHeight)
        if (textureHeight == 0) {
            throw RuntimeException("Text Fonts Texture height is 0")
        }

        for (rectangle in rectangles) {
            rectangle.y = textureHeight - rectangle.y
            rectangle.character.u = (rectangle.x) / textureWidth
            rectangle.character.v = (rectangle.y - rectangle.character.height) / textureHeight
        }

        val bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ALPHA_8)
        //val bitmap = Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        bitmap.eraseColor(Color.argb(0, 255, 255, 255));
        for (rectangle in rectangles) {
            /*
            canvas.drawRect(
                rectangle.x,
                rectangle.y - rectangle.character.height,
                rectangle.x + rectangle.charsLeftSpaceInTexture + rectangle.character.width + rectangle.charsRightSpaceInTexture,
                rectangle.y,
                rectangle.paint
            )
            */
            canvas.drawText(
                rectangle.symbol.toString(),
                rectangle.x + rectangle.charsLeftSpaceInTexture,
                rectangle.y - rectangle.paint.fontMetrics.descent,
                rectangle.paint
            )
        }
        rectangles.clear()
        return bitmap
    }

    private fun initRectangles(): Float {
        var square = 0f
        var maxCharWidth = 0f
        if (defaultFontData != null) {
            for (characterEntry in defaultFontData!!.characters) {
                val character = characterEntry.value
                val symbol = characterEntry.key
                val fullCharWidth =
                    defaultFontData!!.charsLeftSpaceInTexture + character.width + defaultFontData!!.charsRightSpaceInTexture
                square += fullCharWidth * character.height
                rectangles.add(
                    Rectangle(
                        0f,
                        0f,
                        character,
                        symbol,
                        defaultFontData!!.paint,
                        defaultFontData!!.charsLeftSpaceInTexture,
                        defaultFontData!!.charsRightSpaceInTexture
                    )
                )
                if (fullCharWidth > maxCharWidth) {
                    maxCharWidth = fullCharWidth
                }
            }
        }
        for (font in fonts.values) {
            for (characterEntry in font.characters) {
                val character = characterEntry.value
                val symbol = characterEntry.key
                val fullCharWidth =
                    defaultFontData!!.charsLeftSpaceInTexture + character.width + defaultFontData!!.charsRightSpaceInTexture
                square += fullCharWidth * character.height
                rectangles.add(
                    Rectangle(
                        0f,
                        0f,
                        character,
                        symbol,
                        font.paint,
                        defaultFontData!!.charsLeftSpaceInTexture,
                        defaultFontData!!.charsRightSpaceInTexture
                    )
                )
                if (fullCharWidth > maxCharWidth) {
                    maxCharWidth = fullCharWidth
                }
            }
        }
        var side: Float = sqrt(square * 1.2f)
        if (side < maxCharWidth) {
            side = maxCharWidth
        }

        textureWidth = (side + 0.5f).toInt()
        if (textureWidth > MAX_TEXTURE_WIDTH) textureWidth = MAX_TEXTURE_WIDTH
        if (textureWidth < MIN_TEXTURE_WIDTH) textureWidth = MIN_TEXTURE_WIDTH
        return square
    }

    private fun packRectangles(): Int {
        val sortedList = rectangles.sortedByDescending { it.character.height }
        var xPos = 0f
        var yPos = 0f
        var largestHThisRow = 0f
        var textureHeight = 0f

        for (rectangle in sortedList) {
            val fullCharWidth =
                rectangle.charsLeftSpaceInTexture + rectangle.character.width + rectangle.charsRightSpaceInTexture
            if ((xPos + fullCharWidth) > textureWidth) {
                yPos += largestHThisRow
                xPos = 0f
                largestHThisRow = 0f
            }
            rectangle.x = xPos
            rectangle.y = yPos
            xPos += fullCharWidth
            if (rectangle.character.height > largestHThisRow) {
                largestHThisRow = rectangle.character.height
            }
            textureHeight = yPos + largestHThisRow
        }
        if (textureHeight > MAX_TEXTURE_HEIGHT) {
            textureHeight = 0f
        }
        return (textureHeight + 0.5f).toInt()
    }
}
