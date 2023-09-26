package com.example.opengl.devourer

import com.example.opengl.Game
import com.example.opengl.Position
import com.example.opengl.PositionInd
import com.example.opengl.Resource
import com.example.opengl.ResourcesStorage
import com.example.opengl.tiles.MineralType
import com.example.opengl.tiles.TileMap
import java.nio.FloatBuffer
import java.util.Collections
import java.util.Timer
import java.util.TimerTask

class Devourer(
    private val tileMap: TileMap,
    private val betweenTileCentersX: Float,
    private val betweenTileCentersY: Float,
) {
    enum class DevourerState {
        NORMAL, EATING
    }

    private val devourerStructure: DevourerStructure = DevourerStructure()
    private val movingResources = Collections.synchronizedList(ArrayList<Resource>())
    private val resourcesStorage = ResourcesStorage()
    private val mainDevourerPos: Position
    private val animationTimer: Timer
    private var animationPhase = 0
    private var devourerState = DevourerState.NORMAL

    init {
        devourerStructure.createStructure(tileMap)
        val mainDevourerPosInd =
            PositionInd(tileMap.mainDevourerTileIndX, tileMap.mainDevourerTileIndY)
        mainDevourerPos = calculateDevourerNodePosition(mainDevourerPosInd)
        animationTimer = Timer()
        animationTimer.schedule(object : TimerTask() {
            override fun run() {
                calculateDevourerAnimationPhase()
            }
        }, 0, 33)
    }

    private fun calculateDevourerAnimationPhase() {
        when (devourerState) {
            DevourerState.NORMAL -> if (animationPhase == NORMAL_PHASES.size - 1) {
                animationPhase = 0
            } else {
                animationPhase++
            }

            DevourerState.EATING -> if (animationPhase == EATING_PHASES.size - 1) {
                animationPhase = 0
                setDevourerState(DevourerState.NORMAL)
            } else {
                animationPhase++
            }
        }
    }

    private val currentAnimationSpriteNumber: Float
        get() {
            var result = 0
            result = when (devourerState) {
                DevourerState.NORMAL -> NORMAL_PHASES[animationPhase]
                DevourerState.EATING -> EATING_PHASES[animationPhase]
            }
            return result.toFloat()
        }

    private fun setDevourerState(newDevourerState: DevourerState) {
        if (devourerState != newDevourerState) {
            devourerState = newDevourerState
            animationPhase = 0
        }
    }

    fun getDevourerNode(x: Int, y: Int): DevourerNode? {
        return devourerStructure.getDevourerNode(x, y)
    }

    fun moveResources(): Boolean {
        return if (movingResources.size > 0) {
            synchronized(movingResources) {
                // Must be in synchronized block
                val iterator = movingResources.iterator()
                while (iterator.hasNext()) {
                    val resource = iterator.next()
                    if (resource.isMoving) {
                        resource.move(MINERAL_SPEED)
                    } else {
                        resource.tryMove(MINERAL_SPEED)                    }
                    if (resource.isDelivered) {
                        resourcesStorage.addMineral(resource.mineralType, 1)
                        setDevourerState(DevourerState.EATING)
                        Game.instance.showMineralsCounts()
                        iterator.remove()
                    }
                }
            }
            true
        } else {
            false
        }
    }

    fun placeDevourer(indX: Int, indY: Int) {
        if (tileMap.getTile(indX, indY)!!.mineral != null) { // is mineral
            startEatingMineral(indX, indY)
        } else {
            tileMap.placeDevourerTile(indX, indY)
            devourerStructure.addDevourerNode(indX, indY, tileMap)
            Game.instance.setMessage1(
                "indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(
                    indX,
                    indY
                )!!.entity
            )
        }
    }

    private fun startEatingMineral(indX: Int, indY: Int) {
        tileMap.placeDevourerTile(indX, indY)
        devourerStructure.addDevourerNode(indX, indY, tileMap)
        val devourerNode = devourerStructure.getDevourerNode(indX, indY)
        if (devourerNode != null) {
            tileMap.getTile(indX, indY)!!.mineral?.resourceDepository?.startResourceMining(
                indX,
                indY,
                devourerStructure,
                calculateDevourerNodePosition(PositionInd(indX, indY)),
                movingResources
            )
        }
    }

    fun deleteDevourer(indX: Int, indY: Int) {
        tileMap.deleteDevourerTile(indX, indY)
        devourerStructure.removeDevourerNode(indX, indY, tileMap)
        Game.instance.setMessage1(
            "indX:" + indX + " indY:" + indY + " tile:" + tileMap.getTile(
                indX,
                indY
            )!!.basis
        )
    }

    fun createRenderDataMoving(): FloatBuffer {
        synchronized(movingResources) {
            val resourcesData: MutableList<Float> = ArrayList()
            for (resource in movingResources) {
                val position = calculateResourcePosition(resource)
                if (position != null) {
                    resourcesData.add(position.x)
                    resourcesData.add(position.y)
                    resourcesData.add(0f) // tile number
                    resourcesData.add(0f) // texture number
                }
            }
            resourcesData.add(mainDevourerPos.x)
            resourcesData.add(mainDevourerPos.y)
            resourcesData.add(currentAnimationSpriteNumber) // sprite number
            resourcesData.add(1f) // texture number main devourer

            //if (resourcesData.size() == 0) return null;
            val result = FloatBuffer.allocate(resourcesData.size)
            for (fl in resourcesData) {
                result.put(fl)
            }
            return result
        }
    }

    private fun calculateDevourerNodePosition(positionInd: PositionInd): Position {
        val result = Position()
        result.x = positionInd.x * betweenTileCentersX
        if (positionInd.x % 2 == 0) {
            result.y = positionInd.y * betweenTileCentersY
        } else {
            result.y = (positionInd.y + 0.5f) * betweenTileCentersY
        }
        return result
    }

    private fun calculateResourcePosition(resource: Resource): Position? { // interpolation
        val result = Position()
        if (resource.nextPosition == null) {
            if (resource.nextPositionInd != null) {
                resource.nextPosition = calculateDevourerNodePosition(resource.nextPositionInd!!)
            } else {
                return null
            }
        }
        result.x =
            resource.startPosition!!.x + (resource.nextPosition!!.x - resource.startPosition!!.x) * resource.distance
        result.y =
            resource.startPosition!!.y + (resource.nextPosition!!.y - resource.startPosition!!.y) * resource.distance
        return result
    }

    fun getMineralCount(mineralType: MineralType?): Int {
        return resourcesStorage.getMineralCount(mineralType)
    }

    companion object {
        private val EATING_PHASES = intArrayOf(
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22
        )
        private val NORMAL_PHASES = intArrayOf(
            0, 0, 0, 0, 1, 1, 1, 2, 2, 2, 22, 22, 22, 22, 22, 2, 2, 2, 1, 1, 1
        )
        private val MINERAL_SPEED : Float = 0.1f
    }
}