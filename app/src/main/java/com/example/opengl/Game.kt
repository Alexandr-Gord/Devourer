package com.example.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.opengl.devourer.Devourer
import com.example.opengl.text2D.FontData
import com.example.opengl.text2D.FontDataStorage
import com.example.opengl.text2D.Text2D
import com.example.opengl.text2D.TextStorage
import com.example.opengl.tiles.MineralType
import com.example.opengl.tiles.TileMap
import java.io.IOException
import java.nio.ShortBuffer
import java.util.Properties
import java.util.Timer
import java.util.TimerTask

class Game private constructor() : BaseObservable() {
    var gameActivity: GameActivity? = null
    var gameView3D: GameView3D? = null
    var tileMap: TileMap? = null
    var tileMapWidth = 35
    var tileMapHeight = 25
    var devourer: Devourer? = null

    @JvmField
    var mode = Mode.VIEW
    private var isShowMessage = false
    private var mineralTimer: Timer? = null
    private var message1: String = ""
    private var message2: String = ""

    enum class Mode {
        BUILD, VIEW, DELETE
    }

    fun startUp() {
        val mapProperties = getMapProperties(R.raw.map_properties, gameActivity!!)
            ?: fail("Error reading properties file")
        tileMapWidth = try {
            mapProperties.getProperty("width").toInt()
        } catch (e: Exception) {
            fail("Error property Width")
        }
        tileMapHeight = try {
            mapProperties.getProperty("height").toInt()
        } catch (e: Exception) {
            fail("Error property Height")
        }
        tileMap = TileMap(tileMapWidth, tileMapHeight, mapProperties).apply {
            loadTileMap(
                gameActivity!!.resources.openRawResource(R.raw.basis_map),
                gameActivity!!.resources.openRawResource(R.raw.mineral_map),
                gameActivity!!.resources.openRawResource(R.raw.entity_map)
            )
        }
        devourer =
            Devourer(tileMap!!, gameView3D!!.betweenTileCentersX, gameView3D!!.betweenTileCentersY)
        mineralTimer?.cancel()
        mineralTimer = Timer()
        val runnable = Runnable {
            initWithDelay()
        }
        gameView3D!!.postDelayed(runnable, 1000) // TODO remake
        setShowMessage(false)
    }

    private fun initWithDelay() {
        redraw()
        mineralTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mineralTimerTick()
            }
        }, 0, 33)

        val fontData: FontData =
            FontData(context = gameView3D!!.context, fontFilename = "", size = 50.0f)
        FontDataStorage.setDefaultFontData(fontData)
        val fontPixel: FontData =
            FontData(context = gameView3D!!.context, fontFilename = "fonts/advanced_pixel-7.ttf", size = 80.0f)
        FontDataStorage.addFontData("pixel", fontPixel)
        //val myColor: Int = R.color.white
        val text1 = Text2D("Welcome", FontDataStorage.getFontData(""), 100f, 600f, 2f, Color.RED)
        TextStorage.addText2D("greetings", text1)
        val text2 = Text2D("Username", FontDataStorage.getFontData("pixel"), 100f, 500f, 1.5f, Color.YELLOW)
        TextStorage.addText2D("user", text2)
        val bitmap : Bitmap = FontDataStorage.createFontsBitmap();
        gameView3D!!.queueEvent {
            gameView3D!!.openGLRenderer.renderText?.initTexture(bitmap)
            bitmap.recycle()
        }
    }

    private fun mineralTimerTick() {
        //if (devourer.moveResources()) {
        devourer!!.moveResources()
        redraw()
        //}
    }

    @Bindable
    fun getMessage1(): String {
        return message1
    }

    fun setMessage1(message: String) {
        if (isShowMessage) {
            message1 = message
            notifyPropertyChanged(BR.message1)
        }
    }

    @Bindable
    fun getMessage2(): String {
        return message2
    }

    fun setMessage2(message: String) {
        if (isShowMessage) {
            message2 = message
            notifyPropertyChanged(BR.message2)
        }
    }

    fun setShowMessage(showMessage: Boolean) {
        isShowMessage = showMessage
        if (!showMessage) {
            message1 = ""
            message2 = ""
            notifyPropertyChanged(BR.message1)
            notifyPropertyChanged(BR.message2)
        }
    }

    @get:Bindable
    val countMineral0: String
        get() = if (devourer != null) devourer!!.getMineralCount(MineralType.CRYSTAL)
            .toString() else "0"

    @get:Bindable
    val countMineral1: String
        get() = if (devourer != null) devourer!!.getMineralCount(MineralType.DIAMOND)
            .toString() else "0"

    @get:Bindable
    val countMineral2: String
        get() = if (devourer != null) devourer!!.getMineralCount(MineralType.GOLD)
            .toString() else "0"

    fun showMineralsCounts() {
        notifyPropertyChanged(BR.countMineral0)
        notifyPropertyChanged(BR.countMineral1)
        notifyPropertyChanged(BR.countMineral2)
    }

    private fun createRenderDataTile(): ShortBuffer {
        val tilesData = tileMap!!.tileMapData
        val result = ShortBuffer.allocate(tilesData.size)
        for (sh in tilesData) {
            result.put(sh)
        }
        return result
    }

    fun tileClickHandler(indX: Int, indY: Int) {
        when (mode) {
            Mode.VIEW -> selectTile(indX, indY)
            Mode.BUILD -> {
                removeSelectTile()
                devourer!!.placeDevourer(indX, indY)
                redraw()
            }

            Mode.DELETE -> {
                removeSelectTile()
                devourer!!.deleteDevourer(indX, indY)
                redraw()
            }

            else -> {}
        }
    }

    private fun redraw() {
        gameView3D!!.queueEvent {
            gameView3D!!.openGLRenderer.renderTileObjects?.prepareTileData(createRenderDataTile())
            gameView3D!!.openGLRenderer.renderFreeObjects?.prepareMovingData(devourer!!.createRenderDataMoving())
            gameView3D!!.openGLRenderer.renderText?.prepareTextData(TextStorage.createRenderDataText())
        }
    }

    private fun selectTile(indX: Int, indY: Int) {
        tileMap!!.lastSelectedTileIndX = indX
        tileMap!!.lastSelectedTileIndY = indY
        val node = devourer!!.getDevourerNode(indX, indY)
        val dist = node?.dist ?: -1
        //showMessage("indX:" + indX + " indY:" + indY + " dist:" + dist);
        setMessage1("indX:$indX indY:$indY dist:$dist")
        redraw()
    }

    private fun removeSelectTile() {
        tileMap!!.lastSelectedTileIndX = -1
        tileMap!!.lastSelectedTileIndY = -1
    }

    companion object {
        @JvmStatic
        val instance = Game()
        fun getMapProperties(mapPropertiesId: Int, context: Context): Properties? {
            return try {
                val inputStream = context.resources.openRawResource(mapPropertiesId)
                val properties = Properties()
                properties.load(inputStream)
                inputStream.close()
                properties
            } catch (e: IOException) {
                null
            }
        }

        fun fail(message: String): Nothing {
            throw RuntimeException(message)
        }
    }
}