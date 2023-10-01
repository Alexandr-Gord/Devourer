package com.example.opengl.text2D

import android.graphics.Color
import java.nio.FloatBuffer

object TextStorage {
    @JvmStatic
    private val texts: MutableMap<String, Text2D> = HashMap()

    fun addText2D(name: String, text: Text2D) {
        texts[name] = text
    }

    fun getText2D(name: String): Text2D? {
        return texts[name]
    }

    fun removeText2D(name: String) {
        texts.remove(name)
    }

    fun createRenderDataText(): FloatBuffer {
        synchronized(texts) {
            val textData: MutableList<Float> = ArrayList()
            for (textEntity in texts) {
                val text2D: Text2D = textEntity.value
                if (!text2D.isShow) continue
                val text = text2D.text.toCharArray()
                for (i in text.indices) {
                    val char = text2D.font.getCharacter(text[i]) ?: continue
                    textData.add(text2D.positions[i])
                    textData.add(0f)
                    textData.add(char.width)
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
                }
            }
            val result = FloatBuffer.allocate(textData.size)
            for (fl in textData) {
                result.put(fl)
            }
            return result
        }
    }
}