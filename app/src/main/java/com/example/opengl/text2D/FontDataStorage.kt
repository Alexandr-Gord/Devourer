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
        val paint: Paint
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
            throw RuntimeException("Text Texture height is 0")
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
                rectangle.x + rectangle.character.width,
                rectangle.y,
                rectangle.paint
            )
            */
            canvas.drawText(rectangle.symbol.toString(), rectangle.x, rectangle.y - rectangle.paint.fontMetrics.descent, rectangle.paint)
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
                square += character.width * character.height
                rectangles.add(Rectangle(0f, 0f, character, symbol, defaultFontData!!.paint))
                if (character.width > maxCharWidth) {
                    maxCharWidth = character.width
                }
            }
        }
        for (font in fonts.values) {
            for (characterEntry in font.characters) {
                val character = characterEntry.value
                val symbol = characterEntry.key
                square += character.width * character.height
                rectangles.add(Rectangle(0f, 0f, character, symbol, font.paint))
                if (character.width > maxCharWidth) {
                    maxCharWidth = character.width
                }
            }
        }
        var side: Float = sqrt(square * 1.2f)
        if (side < maxCharWidth) {
            side = maxCharWidth
        }
        /*
        textureWidth = if (side <= 128)
            128;
        else if (side <= 256)
            256;
        else if (side <= 512)
            512;
        else if (side <= 1024)
            1024;
        else
            MAX_TEXTURE_WIDTH;
         */
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
            if ((xPos + rectangle.character.width) > textureWidth) {
                yPos += largestHThisRow
                xPos = 0f
                largestHThisRow = 0f
            }
            rectangle.x = xPos
            rectangle.y = yPos
            xPos += rectangle.character.width
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

/*
struct Rect
{
  int x, y;
  int w, h;
  bool wasPacked = false;
};

void PackRectsNaiveRows(std::vector<Rect>& rects)
{
  // Sort by a heuristic
  std::sort(rects.begin(), rects.end(), SortByHeight());

  int xPos = 0;
  int yPos = 0;
  int largestHThisRow = 0;

  // Loop over all the rectangles
  for (Rect& rect : rects)
  {
    // If this rectangle will go past the width of the image
    // Then loop around to next row, using the largest height from the previous row
    if ((xPos + rect.w) > 700)
    {
      yPos += largestHThisRow;
      xPos = 0;
      largestHThisRow = 0;
    }

    // If we go off the bottom edge of the image, then we've failed
    if ((yPos + rect.h) > 700)
      break;

    // This is the position of the rectangle
    rect.x = xPos;
    rect.y = yPos;

    // Move along to the next spot in the row
    xPos += rect.w;

    // Just saving the largest height in the new row
    if (rect.h > largestHThisRow)
      largestHThisRow = rect.h;

    // Success!
    rect.wasPacked = true;
  }
}
 */