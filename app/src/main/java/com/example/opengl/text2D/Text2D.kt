package com.example.opengl.text2D

class Text2D(
    text: String,
    font: FontData,
    var x: Float,
    var y: Float,
    var scale: Float,
    var color: Int,
    var isShow: Boolean = true
) {
    var text: String = text
        private set

    var font: FontData = font
        private set

    val positions: MutableList<Float> = ArrayList()

    init {
        updatePositions()
    }

    fun setText(text: String) {
        this.text = text
        updatePositions()
    }

    fun setFont(font: FontData) {
        this.font = font
        updatePositions()
    }

    private fun updatePositions() {
        positions.clear()
        val w = FloatArray(text.length)
        font.paint.getTextWidths(text, w)
        var sum = 0f
        for (i in text.indices){
            positions.add(sum)
            sum += w[i]
        }
    }

}