package com.example.opengl.tiles

import com.example.opengl.Resource
import com.example.opengl.ResourceDepository
import com.example.opengl.constants.DevourerType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Collections
import java.util.Properties

class TileMap(
    private val tileMapWidth: Int,
    private val tileMapHeight: Int,
    private val mapProperties: Properties
) {
    private val tileList: MutableList<Tile>
    var lastSelectedTileIndX = -1
    var lastSelectedTileIndY = -1
    var mainDevourerTileIndX = -1
        private set
    var mainDevourerTileIndY = -1
        private set

    init {
        val arraySize = tileMapWidth * tileMapHeight
        val initList = ArrayList<Tile>(arraySize)
        for (i in 0 until arraySize) {
            initList.add(Tile())
        }
        tileList = Collections.synchronizedList(initList)
    }

    fun getTile(x: Int, y: Int): Tile? {
        return if (isTileExist(x, y)) tileList[getTileIndex(x, y)] else null
    }

    fun setTile(tile: Tile, x: Int, y: Int) {
        tileList[getTileIndex(x, y)] = tile
    }

    private fun getTileIndex(x: Int, y: Int): Int {
        return y * tileMapWidth + x
    }

    private fun findMainDevourerTilePos() {
        mainDevourerTileIndX = 7 // del
        mainDevourerTileIndY = 16 // del
        //TODO implement
    }

    fun loadTileMap(basisIn: InputStream, mineralIn: InputStream, entityIn: InputStream) {
        try {
            readBasis(basisIn)
            readMinerals(mineralIn)
            readEntity(entityIn)
        } catch (ignored: IOException) {
            // todo log exception
        }
        fixSprites()
        findMainDevourerTilePos()
        generateFogMap()
    }

    @Throws(IOException::class)
    private fun readBasis(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        for (y in tileMapHeight - 1 downTo -1 + 1) {
            val line = reader.readLine()
            val lineArray = line.split(DELIMITER_MAP.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (x in 0 until tileMapWidth) {
                val code = lineArray[x].toByte()
                getTile(x, y)!!.basis = chooseBasis(code)
            }
        }
        reader.close()
    }

    private fun chooseBasis(code: Byte): Basis? {
        var basis: Basis? = null
        when (code.toInt()) {
            0 -> {}
            1 -> {
                basis = Dirt()
                basis.id = 0
            }
        }
        return basis
    }

    private fun readMinerals(inputStream: InputStream) {
        synchronized(tileList) {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                val prefix = "mineral_"
                for (y in tileMapHeight - 1 downTo -1 + 1) {
                    val line = reader.readLine()
                    val lineArray =
                        line.split(DELIMITER_MAP.toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    for (x in 0 until tileMapWidth) {
                        val code = lineArray[x]
                        val property = mapProperties.getProperty(prefix + code, "").split(
                            DELIMITER_PROPERTY.toRegex()
                        ).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                        if (property.size > 2) {
                            val id = property[0].toInt()
                            val count = property[1].toInt()
                            val period = property[2].toInt()
                            val mineral = chooseMineral(id)
                            if (mineral != null && count > 0) {
                                val resourceDepository = ResourceDepository(period)
                                for (i in 0 until count) {
                                    val resource = Resource(
                                        mineral.type!!
                                    )
                                    resourceDepository.addResource(resource)
                                }
                                mineral.resourceDepository = resourceDepository
                                getTile(x, y)!!.mineral = mineral
                            }
                        }
                    }
                }
                reader.close()
            } catch (e: IOException) {
            }
        }
    }

    private fun chooseMineral(id: Int): Mineral? {
        var mineral: Mineral? = null
        when (id) {
            0 -> {
                mineral = Mineral()
                mineral.id = 0
                mineral.type = MineralType.GOLD
            }

            1 -> {
                mineral = Mineral()
                mineral.id = id
                mineral.type = MineralType.CRYSTAL
            }

            2 -> {
                mineral = Mineral()
                mineral.id = id
                mineral.type = MineralType.DIAMOND
            }

            else -> {}
        }
        return mineral
    }

    @Throws(IOException::class)
    private fun readEntity(inputStream: InputStream) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        for (y in tileMapHeight - 1 downTo -1 + 1) {
            val line = reader.readLine()
            val lineArray = line.split(DELIMITER_MAP.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            for (x in 0 until tileMapWidth) {
                val code = lineArray[x].toByte()
                getTile(x, y)!!.entity = chooseEntity(code)
            }
        }
        reader.close()
    }

    private fun chooseEntity(code: Byte): Entity? {
        var entity: Entity? = null
        when (code.toInt()) {
            0 -> {}
            2 -> entity = EntityDevourerBase()
        }
        return entity
    }

    private fun fixSprites() {
        var tile: Tile?
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y)
                if (tile!!.entity != null) {
                    tile.entity!!.id = calcEntitySpriteCode(x, y)
                }
            }
        }
    }

    fun isTileExist(indX: Int, indY: Int): Boolean {
        return indX < tileMapWidth && indX > -1 && indY < tileMapHeight && indY > -1
    }

    private fun isMineralEqual(indX: Int, indY: Int, type: MineralType): Boolean {
        val mineral = getTile(indX, indY)!!.mineral
        return if (mineral == null) {
            false
        } else {
            mineral.type === type
        }
    }

    private fun calcMineralSpriteCode(indX: Int, indY: Int, type: MineralType): Int {
        var code = 0
        if (indX % 2 == 0) {
            if (isTileExist(indX, indY - 1) && isMineralEqual(indX, indY - 1, type)) {
                code += 1
            }
            if (isTileExist(indX + 1, indY - 1) && isMineralEqual(indX + 1, indY - 1, type)) {
                code += 2
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code += 4
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code += 8
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code += 16
            }
            if (isTileExist(indX - 1, indY - 1) && isMineralEqual(indX - 1, indY - 1, type)) {
                code += 32
            }
        } else {
            if (isTileExist(indX, indY - 1) && isMineralEqual(indX, indY - 1, type)) {
                code += 1
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code += 2
            }
            if (isTileExist(indX + 1, indY + 1) && isMineralEqual(indX + 1, indY + 1, type)) {
                code += 4
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code += 8
            }
            if (isTileExist(indX - 1, indY + 1) && isMineralEqual(indX - 1, indY + 1, type)) {
                code += 16
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code += 32
            }
        }
        return code
    }

    fun isEntity(indX: Int, indY: Int): Boolean {
        return getTile(indX, indY)!!.entity != null
    }

    private fun calcEntitySpriteCode(indX: Int, indY: Int): Int {
        var code = 0
        if (indX % 2 == 0) {
            if (isTileExist(indX, indY - 1) && isEntity(indX, indY - 1)) {
                code += 1
            }
            if (isTileExist(indX + 1, indY - 1) && isEntity(indX + 1, indY - 1)) {
                code += 2
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code += 4
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code += 8
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code += 16
            }
            if (isTileExist(indX - 1, indY - 1) && isEntity(indX - 1, indY - 1)) {
                code += 32
            }
        } else {
            if (isTileExist(indX, indY - 1) && isEntity(indX, indY - 1)) {
                code += 1
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code += 2
            }
            if (isTileExist(indX + 1, indY + 1) && isEntity(indX + 1, indY + 1)) {
                code += 4
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code += 8
            }
            if (isTileExist(indX - 1, indY + 1) && isEntity(indX - 1, indY + 1)) {
                code += 16
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code += 32
            }
        }
        return code
    }

    val tileMapData1: List<Short>
        get() {
            val result: MutableList<Short> = ArrayList()
            synchronized(tileList) {
                var tile: Tile?
                for (y in 0 until tileMapHeight) {
                    for (x in 0 until tileMapWidth) {
                        tile = getTile(x, y)
                        //if ((tile.getBasis() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {
                        if (tile!!.basis != null) {
                            result.add(x.toShort())
                            result.add(y.toShort())
                            result.add(0.toShort()) // tile number
                            result.add(0.toShort()) // texture number
                        }
                    }
                }
                for (y in 0 until tileMapHeight) {
                    for (x in 0 until tileMapWidth) {
                        tile = getTile(x, y)
                        if (tile!!.mineral != null) {
                            result.add(x.toShort())
                            result.add(y.toShort())
                            result.add(tile.mineral!!.id.toShort()) // tile number
                            result.add(1.toShort()) // texture number
                        }
                    }
                }
                for (y in 0 until tileMapHeight) {
                    for (x in 0 until tileMapWidth) {
                        tile = getTile(x, y)
                        //if ((tile.getEntity() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {
                        if (tile!!.entity != null) {
                            result.add(x.toShort())
                            result.add(y.toShort())
                            result.add(tile.entity!!.id.toShort()) // tile number
                            result.add(2.toShort()) // texture number
                        }
                    }
                }
                for (y in 0 until tileMapHeight) {
                    for (x in 0 until tileMapWidth) {
                        tile = getTile(x, y)
                        //if (tile!!.isFogged) {
                        if (tile!!.fogTileId == FULL_FOG_TILE_CODE) {
                            result.add(x.toShort())
                            result.add(y.toShort())
                            result.add(tile.fogTileId.toShort()) // tile number
                            result.add(3.toShort()) // texture number
                        }
                    }
                }
                if (lastSelectedTileIndX > -1 && lastSelectedTileIndY > -1) {
                    result.add(lastSelectedTileIndX.toShort())
                    result.add(lastSelectedTileIndY.toShort())
                    result.add(1.toShort()) // tile number
                    result.add(0.toShort()) // texture number
                }
            }
            return result
        }

    val tileMapData: List<Short>
        get() {
            val layerBasis: MutableList<Short> = ArrayList()
            val layerMineral: MutableList<Short> = ArrayList()
            val layerDevourer: MutableList<Short> = ArrayList()
            val layerFog: MutableList<Short> = ArrayList()
            synchronized(tileList) {
                var tile: Tile?
                for (y in 0 until tileMapHeight) {
                    for (x in 0 until tileMapWidth) {
                        // TODO show not full map, only tiles in screen
                        tile = getTile(x, y)
                        if (tile == null) continue
                        if (tile.fogTileId != FULL_FOG_TILE_CODE) {
                            if (tile.basis != null) {
                                layerBasis.add(x.toShort())
                                layerBasis.add(y.toShort())
                                layerBasis.add(0.toShort()) // tile number
                                layerBasis.add(0.toShort()) // texture number
                            }

                            if (tile.mineral != null) {
                                layerMineral.add(x.toShort())
                                layerMineral.add(y.toShort())
                                layerMineral.add(tile.mineral!!.id.toShort()) // tile number
                                layerMineral.add(1.toShort()) // texture number
                            }

                            if (tile.entity != null) {
                                layerDevourer.add(x.toShort())
                                layerDevourer.add(y.toShort())
                                when (tile.entity) {
                                    is EntityDevourerBase -> {
                                        layerDevourer.add(tile.entity!!.id.toShort()) // tile number
                                        layerDevourer.add(2.toShort()) // texture number
                                    }

                                    is EntityDevourerPipe -> {
                                        layerDevourer.add(tile.entity!!.id.toShort()) // tile number
                                        layerDevourer.add(4.toShort()) // texture number
                                    }

                                    is EntityDevourerGlow -> {
                                        layerDevourer.add(2.toShort()) // tile number
                                        layerDevourer.add(0.toShort()) // texture number
                                    }
                                }
                            }
                        }
                        if (tile.fogTileId > 0) {
                            layerFog.add(x.toShort())
                            layerFog.add(y.toShort())
                            layerFog.add(tile.fogTileId.toShort()) // tile number
                            layerFog.add(3.toShort()) // texture number
                        }
                    }
                }

                layerBasis.addAll(layerMineral)
                layerBasis.addAll(layerDevourer)
                layerBasis.addAll(layerFog)

                if (lastSelectedTileIndX > -1 && lastSelectedTileIndY > -1) {
                    layerBasis.add(lastSelectedTileIndX.toShort())
                    layerBasis.add(lastSelectedTileIndY.toShort())
                    layerBasis.add(1.toShort()) // tile number
                    layerBasis.add(0.toShort()) // texture number
                }
            }
            return layerBasis
        }


    private fun fixEntitySpriteID(tile: Tile, indX: Int, indY: Int) {
        tile.entity!!.id = calcEntitySpriteCode(indX, indY)
    }

    private fun fixNearTile(indX: Int, indY: Int) {
        val nearInd = getNearIndexes(indX, indY)
        for (i in 0..5) {
            val nearIndX = nearInd[i][0]
            val nearIndY = nearInd[i][1]
            if (nearIndX < tileMapWidth && nearIndY < tileMapHeight && nearIndX > -1 && nearIndY > -1) {
                val tile = getTile(nearIndX, nearIndY)
                if (tile!!.entity != null) {
                    fixEntitySpriteID(tile, nearIndX, nearIndY)
                }
            }
        }
        if (isEntity(indX, indY)) {
            fixEntitySpriteID(getTile(indX, indY)!!, indX, indY)
        }
    }

    fun isNearEntityExist(indX: Int, indY: Int): Boolean {
        val nearInd = getNearIndexes(indX, indY)
        var isExist = false
        for (i in 0..5) {
            val nearIndX = nearInd[i][0]
            val nearIndY = nearInd[i][1]
            if (nearIndX < tileMapWidth && nearIndY < tileMapHeight && nearIndX > -1 && nearIndY > -1) {
                if (getTile(nearIndX, nearIndY)!!.entity != null) {
                    isExist = true
                }
            }
        }
        return isExist
    }

    private fun getNearIndexes(indX: Int, indY: Int): Array<IntArray> {
        val nearInd = Array(6) { IntArray(2) }
        if (indX % 2 == 0) {
            nearInd[0][0] = indX
            nearInd[0][1] = indY - 1
            nearInd[1][0] = indX + 1
            nearInd[1][1] = indY - 1
            nearInd[2][0] = indX + 1
            nearInd[2][1] = indY
            nearInd[3][0] = indX
            nearInd[3][1] = indY + 1
            nearInd[4][0] = indX - 1
            nearInd[4][1] = indY
            nearInd[5][0] = indX - 1
            nearInd[5][1] = indY - 1
        } else {
            nearInd[0][0] = indX
            nearInd[0][1] = indY - 1
            nearInd[1][0] = indX + 1
            nearInd[1][1] = indY
            nearInd[2][0] = indX + 1
            nearInd[2][1] = indY + 1
            nearInd[3][0] = indX
            nearInd[3][1] = indY + 1
            nearInd[4][0] = indX - 1
            nearInd[4][1] = indY + 1
            nearInd[5][0] = indX - 1
            nearInd[5][1] = indY
        }
        return nearInd
    }

    fun placeDevourerTile(mapIndX: Int, mapIndY: Int, devourerType: DevourerType) {
        synchronized(tileList) {
            when (devourerType) {
                DevourerType.BASE -> {
                    getTile(mapIndX, mapIndY)!!.entity = EntityDevourerBase()
                }

                DevourerType.PIPE -> {
                    getTile(mapIndX, mapIndY)!!.entity = EntityDevourerPipe()
                }

                DevourerType.GLOW -> {
                    getTile(mapIndX, mapIndY)!!.entity = EntityDevourerGlow()
                }
            }
            fixNearTile(mapIndX, mapIndY)
            updateFogMap(mapIndX, mapIndY)
        }
    }

    fun deleteDevourerTile(mapIndX: Int, mapIndY: Int) {
        if (isEntity(mapIndX, mapIndY)) {
            synchronized(tileList) {
                getTile(mapIndX, mapIndY)?.entity = null
                fixNearTile(mapIndX, mapIndY)
                updateFogMap(mapIndX, mapIndY)
            }
        }
    }

    private fun setFogAllMap() {
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                getTile(x, y)?.let {
                    it.fogTileId = FULL_FOG_TILE_CODE
                }
            }
        }
    }

    private fun generateFogMap() { // todo segment of map
        setFogAllMap() // todo do or not in settings
        var tile: Tile
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y) ?: continue
                if (tile.entity != null) {
                    tile.fogTileId = 0
                }
            }
        }

        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y) ?: continue
                if (tile.fogTileId > 0) {
                    tile.fogTileId = calcFogSpriteCode(x, y)
                }
            }
        }

        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y) ?: continue
                if (tile.entity != null) {
                    if (tile.entity is EntityDevourerGlow) {
                        setGlowArea(x, y)
                        setGlowBorder(x, y)
                    }
                }
            }
        }
    }

    private fun setGlowArea(indX: Int, indY: Int) {
        var tile: Tile
        for (i in GLOW_AREA_X.indices) {
            if (indX % 2 == 0) {
                tile = getTile(GLOW_AREA_X[i] + indX, GLOW_AREA_Y_EVEN[i] + indY) ?: continue
                tile.fogTileId = 0
            } else {
                tile = getTile(GLOW_AREA_X[i] + indX, GLOW_AREA_Y_ODD[i] + indY) ?: continue
                tile.fogTileId = 0
            }
        }
    }

    private fun setGlowBorder(indX: Int, indY: Int) {
        var tile: Tile
        var x: Int
        var y: Int
        for (i in GLOW_BORDER_X.indices) {
            x = GLOW_BORDER_X[i] + indX
            if (indX % 2 == 0) {
                y = GLOW_BORDER_Y_EVEN[i] + indY
                tile = getTile(x, y) ?: continue
                if (tile.fogTileId > 0) {
                    tile.fogTileId = calcFogSpriteCode(x, y)
                }
            } else {
                y = GLOW_BORDER_Y_ODD[i] + indY
                tile = getTile(x, y) ?: continue
                if (tile.fogTileId > 0) {
                    tile.fogTileId = calcFogSpriteCode(x, y)
                }
            }
        }
    }

    private fun setFogAroundEntity(indX: Int, indY: Int) {
        val nearInd = getNearIndexes(indX, indY)
        var tile: Tile
        var nearIndX: Int
        var nearIndY: Int
        for (i in 0..5) {
            nearIndX = nearInd[i][0]
            nearIndY = nearInd[i][1]
            tile = getTile(nearIndX, nearIndY) ?: continue
            if (tile.fogTileId > 0) {
                tile.fogTileId = calcFogSpriteCode(nearIndX, nearIndY)
            }
        }
    }

    private fun updateFogMap(indX: Int, indY: Int) {
        val tile: Tile = getTile(indX, indY) ?: return
        when (tile.entity) {
            is EntityDevourerGlow -> {
                tile.fogTileId = 0
                setGlowArea(indX, indY)
                setGlowBorder(indX, indY)
            }

            is EntityDevourerBase, is EntityDevourerPipe -> {
                tile.fogTileId = 0
                setFogAroundEntity(indX, indY)
            }

            null -> { // entity deleted
                generateFogMap()
            }
        }
    }

    private fun isFogged(indX: Int, indY: Int): Boolean {
        return if (indX < tileMapWidth && indX > -1 && indY < tileMapHeight && indY > -1) {
            getTile(indX, indY)!!.fogTileId > 0
        } else {
            true
        }
    }

    private fun calcFogSpriteCode(indX: Int, indY: Int): Int {
        var code = 0
        if (indX % 2 == 0) {
            if (isFogged(indX, indY - 1)) {
                code += 1
            }
            if (isFogged(indX + 1, indY - 1)) {
                code += 2
            }
            if (isFogged(indX + 1, indY)) {
                code += 4
            }
            if (isFogged(indX, indY + 1)) {
                code += 8
            }
            if (isFogged(indX - 1, indY)) {
                code += 16
            }
            if (isFogged(indX - 1, indY - 1)) {
                code += 32
            }
        } else {
            if (isFogged(indX, indY - 1)) {
                code += 1
            }
            if (isFogged(indX + 1, indY)) {
                code += 2
            }
            if (isFogged(indX + 1, indY + 1)) {
                code += 4
            }
            if (isFogged(indX, indY + 1)) {
                code += 8
            }
            if (isFogged(indX - 1, indY + 1)) {
                code += 16
            }
            if (isFogged(indX - 1, indY)) {
                code += 32
            }
        }
        return FOG_TILE_CODE[code]
    }

    companion object {
        private const val DELIMITER_MAP = " "
        private const val DELIMITER_PROPERTY = ","
        private val FOG_TILE_CODE = intArrayOf(
            0, 0, 0, 1, 0, 0, 2, 7, 0, 0, 0, 1,
            3, 3, 8, 13, 0, 0, 0, 1, 0, 0, 2, 7,
            4, 4, 4, 25, 9, 9, 14, 23, 0, 6, 0, 12,
            0, 6, 2, 18, 0, 6, 0, 12, 3, 27, 8, 22,
            5, 11, 5, 17, 5, 11, 26, 21, 10, 16, 10, 20,
            15, 19, 24, 28
        )
        private const val FULL_FOG_TILE_CODE = 28

        private val GLOW_AREA_X =
            intArrayOf(0, 1, 1, 0, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -2, -2, -2, -1)
        private val GLOW_AREA_Y_EVEN =
            intArrayOf(-1, -1, 0, 1, 0, -1, -2, -2, -1, 0, 1, 1, 2, 1, 1, 0, -1, -2)
        private val GLOW_AREA_Y_ODD =
            intArrayOf(-1, 0, 1, 1, 1, 0, -2, -1, -1, 0, 1, 2, 2, 2, 1, 0, -1, -1)
        private val GLOW_BORDER_X =
            intArrayOf(0, 1, 2, 3, 3, 3, 3, 2, 1, 0, -1, -2, -3, -3, -3, -3, -2, -1)
        private val GLOW_BORDER_Y_EVEN =
            intArrayOf(-3, -3, -2, -2, -1, 0, 1, 2, 2, 3, 2, 2, 1, 0, -1, -2, -2, -3)
        private val GLOW_BORDER_Y_ODD =
            intArrayOf(-3, -2, -2, -1, 0, 1, 2, 2, 3, 3, 3, 2, 2, 1, 0, -1, -2, -2)
    }
}