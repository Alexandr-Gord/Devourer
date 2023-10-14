package com.example.opengl.text2D

import android.content.Context
import android.graphics.Paint

import android.graphics.Typeface
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.example.opengl.R


class FontData(
    context: Context,
    fontFilename: String,
    size: Float,
    characterSet: CharArray = DEFAULT_CHARACTER_SET,
    val charsLeftSpaceInTexture : Float = 1f,
    val charsRightSpaceInTexture : Float = 1f
) {
    var characters: MutableMap<Char, Character> = HashMap()
        private set
    var paint: Paint

    init {
        val typeface = try {
            Typeface.createFromAsset(context.assets, fontFilename)
        } catch (e: Exception) {
            ResourcesCompat.getFont(
                context,
                R.font.yeseva_one
            )
        }
        paint = Paint().apply {
            this.isAntiAlias = true
            this.textSize = size
            this.setARGB(255, 255, 255, 255);
            this.isAntiAlias = true
            this.typeface = typeface
            //this.style = Paint.Style.STROKE;
        }

        val w = FloatArray(1)
        for (c in characterSet) {
            paint.getTextWidths(c.toString(), w)
            characters[c] = Character(w[0], paint.textSize, 0f, 0f)
        }
    }

    fun getFontSize(): Float {
        return paint.textSize
    }

    fun getCharacter(c: Char): Character? {
        return characters[c]
    }

    companion object {
        data class Character(val width: Float, val height: Float, var u: Float, var v: Float)

        private val DEFAULT_CHARACTER_SET: CharArray =
            " !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~".toCharArray()
    }
}

// Font's measurement
// https://stackoverflow.com/questions/60299272/css-is-font-size-not-acurrate
// https://proandroiddev.com/android-and-typography-101-5f06722dd611