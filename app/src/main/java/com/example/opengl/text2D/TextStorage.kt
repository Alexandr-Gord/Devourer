package com.example.opengl.text2D

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.IOException
import java.io.InputStream
import java.nio.FloatBuffer
import kotlin.math.sqrt


object TextStorage {
    @JvmStatic
    private val texts: MutableMap<String, Text2D> = HashMap()

    @JvmStatic
    private val labels: MutableMap<String, Label> = HashMap()

    private var textureImagesWidth: Int = 0
    private var textureImagesHeight: Int = 0
    private const val MAX_TEXTURE_HEIGHT = 2048
    private const val MAX_TEXTURE_WIDTH = 2048

    private data class Rectangle(
        var x: Int,
        var y: Int,
        var label: Label,
        var bitmap: Bitmap
    )

    fun addText2D(name: String, text: Text2D) {
        texts[name] = text
    }

    fun getText2D(name: String): Text2D? {
        return texts[name]
    }

    fun removeText2D(name: String) {
        texts.remove(name)
    }

    fun addLabel(name: String, label: Label) {
        labels[name] = label
    }

    fun getLabel(name: String): Label? {
        return labels[name]
    }

    fun removeLabel(name: String) {
        labels.remove(name)
    }

    fun createRenderDataText(): FloatBuffer {
        val textData: MutableList<Float> = ArrayList()
        synchronized(texts) {
            for (textEntity in texts) {
                val text2D: Text2D = textEntity.value
                if (!text2D.isShow) continue
                val text = text2D.text.toCharArray()
                val charSpaces = text2D.font.charsLeftSpaceInTexture + text2D.font.charsRightSpaceInTexture
                for (i in text.indices) {
                    val char = text2D.font.getCharacter(text[i]) ?: continue
                    textData.add(text2D.positions[i] - text2D.font.charsLeftSpaceInTexture)
                    textData.add(0f)
                    textData.add(char.width + charSpaces)
                    textData.add(char.height)
                    textData.add(char.u)
                    textData.add(char.v)
                    val r: Float = Color.red(text2D.color) / 255f
                    val g: Float = Color.green(text2D.color) / 255f
                    val b: Float = Color.blue(text2D.color) / 255f
                    textData.add(r)
                    textData.add(g)
                    textData.add(b)
                    textData.add(text2D.x)
                    textData.add(text2D.y)
                    textData.add(text2D.scale)
                    textData.add(text2D.scale)
                    textData.add(1f)
                }
            }
        }

        synchronized(labels) {
            for (labelEntity in labels) {
                val label: Label = labelEntity.value
                if (!label.isShow) continue
                synchronized(label) {
                    textData.add(label.x)
                    textData.add(label.y)
                    textData.add(label.textureWidth)
                    textData.add(label.textureHeight)
                    textData.add(label.u)
                    textData.add(label.v)
                    textData.add(0f)
                    textData.add(0f)
                    textData.add(0f)
                    textData.add(0f)
                    textData.add(0f)
                    textData.add(label.width / label.textureWidth)
                    textData.add(label.height / label.textureHeight)
                    textData.add(2f)

                    for (textEntity in label.getTexts()) {
                        val text2D: Text2D = textEntity.value
                        if (!text2D.isShow) continue
                        val text = text2D.text.toCharArray()
                        val charSpaces = text2D.font.charsLeftSpaceInTexture + text2D.font.charsRightSpaceInTexture
                        for (i in text.indices) {
                            val char = text2D.font.getCharacter(text[i]) ?: continue
                            textData.add(text2D.positions[i] - text2D.font.charsLeftSpaceInTexture)
                            textData.add(0f)
                            textData.add(char.width + charSpaces)
                            textData.add(char.height)
                            textData.add(char.u)
                            textData.add(char.v)
                            val r: Float = Color.red(text2D.color) / 255f
                            val g: Float = Color.green(text2D.color) / 255f
                            val b: Float = Color.blue(text2D.color) / 255f
                            textData.add(r)
                            textData.add(g)
                            textData.add(b)
                            textData.add(text2D.x + label.x)
                            textData.add(text2D.y + label.y)
                            textData.add(text2D.scale)
                            textData.add(text2D.scale)
                            textData.add(1f)
                        }
                    }
                }
            }
        }

        val result = FloatBuffer.allocate(textData.size)
        for (fl in textData) {
            result.put(fl)
        }
        return result
    }

    fun createLabelsBitmap(context: Context): Bitmap {
        synchronized(labels) {
            val rectangles: MutableList<Rectangle> = ArrayList()
            var textureWidth: Int = 0
            var textureHeight: Int = 0
            var square = 0f
            var maxCharWidth = 0f

            for (label in labels.values) {
                val bitmap: Bitmap = loadBitmap(context, label) ?: continue
                rectangles.add(Rectangle(0, 0, label, bitmap))
                square += bitmap.width * bitmap.height
                if (bitmap.width > maxCharWidth) {
                    maxCharWidth = bitmap.width.toFloat()
                }
            }

            var side: Float = sqrt(square * 1.2f)
            if (side < maxCharWidth) {
                side = maxCharWidth
            }
            textureWidth = (side + 0.5f).toInt()
            if (textureWidth > MAX_TEXTURE_WIDTH) textureWidth = MAX_TEXTURE_WIDTH

            textureHeight = packRectangles(rectangles, textureWidth)
            if (textureHeight == 0) throw RuntimeException("Text images Texture height is 0")

            textureImagesWidth = textureWidth
            textureImagesHeight = textureHeight

            for (rectangle in rectangles) {
                //rectangle.y = textureHeight - rectangle.y
                rectangle.label.u = rectangle.x.toFloat() / textureWidth
                //rectangle.label.v = (rectangle.y - rectangle.bitmap.height).toFloat() / textureHeight
                rectangle.label.v = rectangle.y.toFloat() / textureHeight
                rectangle.label.textureWidth = rectangle.bitmap.width.toFloat()
                rectangle.label.textureHeight = rectangle.bitmap.height.toFloat()
            }

            val resultBitmap =
                Bitmap.createBitmap(textureWidth, textureHeight, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(resultBitmap)
            val paint = android.graphics.Paint()
            for (rectangle in rectangles) {
                canvas.drawBitmap(
                    rectangle.bitmap,
                    rectangle.x.toFloat(),
                    rectangle.y.toFloat(),
                    paint
                )
                rectangle.bitmap.recycle()
            }
            rectangles.clear()
            return resultBitmap
        }
    }

    // return texture height if success, 0 if fail.
    private fun packRectangles(rectangles: List<Rectangle>, width: Int): Int {
        rectangles.sortedByDescending { it.bitmap.height }
        var xPos = 0
        var yPos = 0
        var largestHThisRow = 0
        var textureHeight = 0

        for (rectangle in rectangles) {
            if ((xPos + rectangle.bitmap.width) > width) {
                yPos += largestHThisRow
                xPos = 0
                largestHThisRow = 0
            }
            rectangle.x = xPos
            rectangle.y = yPos
            xPos += rectangle.bitmap.width
            if (rectangle.bitmap.height > largestHThisRow) {
                largestHThisRow = rectangle.bitmap.height
            }
            textureHeight = yPos + largestHThisRow
        }
        if (textureHeight > MAX_TEXTURE_HEIGHT) {
            textureHeight = 0
        }
        return textureHeight
    }

    private fun loadBitmap(context: Context, label: Label): Bitmap? {
        if (label.backgroundAssetFile != "") {
            val assetManager = context.assets
            var inputStream: InputStream? = null
            var bitmap: Bitmap? = null
            try {
                inputStream = assetManager.open(label.backgroundAssetFile)
                bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
            } catch (e: IOException) {
                inputStream?.close()
                e.printStackTrace()
                return null
            }
            return bitmap
        } else if (label.backgroundResourceId > 0) {
            return BitmapFactory.decodeResource(context.resources, label.backgroundResourceId)
        } else {
            val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(label.backgroundColor)
            return bitmap
        }
    }


}