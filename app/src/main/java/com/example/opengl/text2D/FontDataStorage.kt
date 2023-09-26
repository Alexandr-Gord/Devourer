package com.example.opengl.text2D

object FontDataStorage {
    private val fonts: MutableMap<String, FontData> = HashMap()
    private var defaultFontData : FontData? = null

    fun addFontData(name: String, font : FontData) {
        fonts[name] = font
    }

    fun getFontData(name : String) : FontData {
        return fonts[name] ?: defaultFontData!!
    }

    fun removeFontData(name: String) {
        fonts.remove(name)
    }

    fun setDefaultFontData (font : FontData) {
        defaultFontData = font
    }

}