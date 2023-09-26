package com.example.opengl.text2D

object TextStorage {
    @JvmStatic
    private val texts: MutableMap<String, Text2D> = HashMap()

    fun addText2D(name: String, text: Text2D) {
        texts[name] = text
    }

    fun getText2D(name : String) : Text2D? {
        return texts[name]
    }

    fun removeText2D(name: String) {
        texts.remove(name)
    }
}