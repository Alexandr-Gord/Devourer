package com.example.opengl.text2D

import android.graphics.Color

class Label(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
    var isShow: Boolean = true
) {
    private val texts: MutableMap<String, Text2D> = HashMap()
    var backgroundColor: Int = Color.DKGRAY
        private set
    var backgroundAssetFile: String = ""
        private set
    var backgroundResourceId: Int = -1
        private set
    var u: Float = 0f
    var v: Float = 0f
    var textureWidth: Float = 0f
    var textureHeight: Float = 0f

    fun addText2D(name: String, text: Text2D) {
        texts[name] = text
    }

    fun getText2D(name: String): Text2D? {
        return texts[name]
    }

    fun getTexts(): MutableMap<String, Text2D> {
        return texts
    }

    fun removeText2D(name: String) {
        texts.remove(name)
    }

    fun setBackgroundColor(color: Int) {
        backgroundColor = color
        backgroundAssetFile = ""
        backgroundResourceId = -1
    }

    fun setBackgroundAssetFile(assetFile: String) {
        backgroundAssetFile = assetFile
        backgroundColor = Color.DKGRAY
        backgroundResourceId = -1
    }

    fun setBackgroundResourceId(resourceId: Int) {
        backgroundResourceId = resourceId
        backgroundColor = Color.DKGRAY
        backgroundAssetFile = ""
    }
}