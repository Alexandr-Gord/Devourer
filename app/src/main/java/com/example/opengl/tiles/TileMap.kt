package com.example.opengl.tiles

import com.example.opengl.Resource
import com.example.opengl.ResourceDepository
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
                mineral.id = id
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
            2 -> entity = EntityTransport()
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
                code = code + 1
            }
            if (isTileExist(indX + 1, indY - 1) && isMineralEqual(indX + 1, indY - 1, type)) {
                code = code + 2
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code = code + 4
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code = code + 8
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code = code + 16
            }
            if (isTileExist(indX - 1, indY - 1) && isMineralEqual(indX - 1, indY - 1, type)) {
                code = code + 32
            }
        } else {
            if (isTileExist(indX, indY - 1) && isMineralEqual(indX, indY - 1, type)) {
                code = code + 1
            }
            if (isTileExist(indX + 1, indY) && isMineralEqual(indX + 1, indY, type)) {
                code = code + 2
            }
            if (isTileExist(indX + 1, indY + 1) && isMineralEqual(indX + 1, indY + 1, type)) {
                code = code + 4
            }
            if (isTileExist(indX, indY + 1) && isMineralEqual(indX, indY + 1, type)) {
                code = code + 8
            }
            if (isTileExist(indX - 1, indY + 1) && isMineralEqual(indX - 1, indY + 1, type)) {
                code = code + 16
            }
            if (isTileExist(indX - 1, indY) && isMineralEqual(indX - 1, indY, type)) {
                code = code + 32
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
                code = code + 1
            }
            if (isTileExist(indX + 1, indY - 1) && isEntity(indX + 1, indY - 1)) {
                code = code + 2
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code = code + 4
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code = code + 8
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code = code + 16
            }
            if (isTileExist(indX - 1, indY - 1) && isEntity(indX - 1, indY - 1)) {
                code = code + 32
            }
        } else {
            if (isTileExist(indX, indY - 1) && isEntity(indX, indY - 1)) {
                code = code + 1
            }
            if (isTileExist(indX + 1, indY) && isEntity(indX + 1, indY)) {
                code = code + 2
            }
            if (isTileExist(indX + 1, indY + 1) && isEntity(indX + 1, indY + 1)) {
                code = code + 4
            }
            if (isTileExist(indX, indY + 1) && isEntity(indX, indY + 1)) {
                code = code + 8
            }
            if (isTileExist(indX - 1, indY + 1) && isEntity(indX - 1, indY + 1)) {
                code = code + 16
            }
            if (isTileExist(indX - 1, indY) && isEntity(indX - 1, indY)) {
                code = code + 32
            }
        }
        return code
    }// tile number

    // texture number
// tile number
    // texture number
// tile number
    // texture number
//if ((tile.getEntity() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {// tile number
    // texture number
// tile number
    // texture number
    //if ((tile.getBasis() != null) && (tile.getFogTileId() != FULL_FOG_TILE_CODE)) {
    val tileMapData: List<Short>
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
                        if (tile!!.isFogged) {
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

    private fun isNearEntityExist(indX: Int, indY: Int): Boolean {
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

    fun placeDevourerTile(mapIndX: Int, mapIndY: Int) {
        if (isNearEntityExist(mapIndX, mapIndY)) {
            synchronized(tileList) {
                getTile(mapIndX, mapIndY)!!.entity = EntityTransport()
                fixNearTile(mapIndX, mapIndY)
                generateFogMap() // todo generate not full map, only part
            }
        }
    }

    fun deleteDevourerTile(mapIndX: Int, mapIndY: Int) {
        if (isEntity(mapIndX, mapIndY)) {
            synchronized(tileList) {
                getTile(mapIndX, mapIndY)!!.entity = null
                fixNearTile(mapIndX, mapIndY)
                generateFogMap() // todo generate not full map, only part
            }
        }
    }

    private fun setFogAllMap() {
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                getTile(x, y)!!.isFogged = true
            }
        }
    }

    private fun generateFogMap() {
        setFogAllMap() // todo do or not in settings
        var tile: Tile?
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y)
                if (tile!!.entity != null) {
                    tile.isFogged = false
                    //resetFogAroundEntity(x, y); // todo fog radius in setting
                }
            }
        }
        for (y in 0 until tileMapHeight) {
            for (x in 0 until tileMapWidth) {
                tile = getTile(x, y)
                if (tile!!.isFogged) {
                    tile.fogTileId = calcFogSpriteCode(x, y)
                }
            }
        }
    }

    private fun resetFogAroundEntity(indX: Int, indY: Int) {
        val nearInd = getNearIndexes(indX, indY)
        var nearIndX: Int
        var nearIndY: Int
        for (i in 0..5) {
            nearIndX = nearInd[i][0]
            nearIndY = nearInd[i][1]
            if (nearIndX < tileMapWidth && nearIndY < tileMapHeight && nearIndX > -1 && nearIndY > -1) {
                getTile(nearIndX, nearIndY)!!.isFogged = false
            }
        }
    }

    private fun isFogged(indX: Int, indY: Int): Boolean {
        return if (indX < tileMapWidth && indX > -1 && indY < tileMapHeight && indY > -1) {
            getTile(indX, indY)!!.isFogged
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
    }
}