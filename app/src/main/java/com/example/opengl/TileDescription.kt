package com.example.opengl

import android.graphics.Color
import com.example.opengl.text2D.FontDataStorage
import com.example.opengl.text2D.Label
import com.example.opengl.text2D.Text2D
import com.example.opengl.text2D.TextStorage
import com.example.opengl.tiles.Tile
import com.example.opengl.tiles.TileMap

class TileDescription(
    private val tileMap: TileMap,
    private val view: GameView3D,
    var width: Float = 320f,
    var height: Float = 200f
) {
    private var tile: Tile? = null
    private val label: Label = Label(0f, 0f, width, height, false)

    init {
        label.setBackgroundAssetFile("images/frame.png")
        val textName =
            Text2D(getTileName(tile), FontDataStorage.getFontData(""), 50f, 110f, 1.0f, Color.RED)
        val textMessage =
            Text2D("message", FontDataStorage.getFontData("pixel"), 50f, 40f, 1.0f, Color.GREEN)
        label.addText2D("name", textName)
        label.addText2D("message", textMessage)
        TextStorage.addLabel("labelTileDescription", label)
    }

    fun setIsShow(isShow: Boolean) {
        label.isShow = isShow
        for (text in label.getTexts().values) {
            text.isShow = isShow
        }
    }

    fun setPosition(x: Float, y: Float) {
        label.x = x
        label.y = y
    }

    fun initTile(mapIndX: Int, mapIndY: Int) {
        tile = tileMap.getTile(mapIndX, mapIndY)
        synchronized(label) {
            label.getText2D("name")?.setText(getTileName(tile))
        }
        calculatePosition(mapIndX, mapIndY)
    }

    fun calculatePosition(mapIndX: Int, mapIndY: Int) {
        val tilePosition = Position()
        tilePosition.x = mapIndX * GameView3D.betweenTileCentersX
        if (mapIndX % 2 == 0) {
            tilePosition.y = mapIndY * GameView3D.betweenTileCentersY
        } else {
            tilePosition.y = (mapIndY + 0.5f) * GameView3D.betweenTileCentersY
        }

        tilePosition.x *= view.currentScale
        tilePosition.y *= view.currentScale

        val tileWidth = GameView3D.SPRITE_WIDTH * view.currentScale
        val tileHeight = GameView3D.SPRITE_HEIGHT * view.currentScale

        val distTileToTopBorder = view.viewHeight - view.translateY - tilePosition.y - tileHeight

        if (distTileToTopBorder > (height + SPACE_TO_LABEL)) { // label on top of tile
            label.y = tilePosition.y + tileHeight + SPACE_TO_LABEL + view.translateY
        } else { // label on bottom of tile
            label.y = tilePosition.y - SPACE_TO_LABEL - height + view.translateY
        }

        val distLabelToTopBorder = view.viewHeight - label.y - height
        val distLabelToBottomBorder = label.y
        if (distLabelToTopBorder < distLabelToBottomBorder) { // near top border
            if (distLabelToTopBorder < 0) label.y += distLabelToTopBorder
        } else { // near bottom border
            if (distLabelToBottomBorder < 0) label.y -= distLabelToBottomBorder
        }

        label.x = tilePosition.x + view.translateX + tileWidth / 2 - width / 2

        val distLabelToRightBorder = view.viewWidth - label.x - width
        val distLabelToLeftBorder = label.x
        if (distLabelToRightBorder < distLabelToLeftBorder) { // near right border
            if (distLabelToRightBorder < 0) label.x += distLabelToRightBorder
        } else { // near left border
            if (distLabelToLeftBorder < 0) label.x -= distLabelToLeftBorder
        }
    }

    companion object {
        private const val SPACE_TO_LABEL = 20f

        private fun getTileName(tile: Tile?): String {
            var result: String = "Nothing"
            if (tile == null) return result
            if (tile.entity != null) {
                result = "Entity"
            } else if (tile.mineral != null) {
                result = tile.mineral!!.type?.name ?: "Mineral"
            } else if (tile.basis != null) {
                result = "Dirt"
            }
            return result
        }

    }
}