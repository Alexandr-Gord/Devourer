package com.example.opengl

import com.example.opengl.devourer.DevourerStructure
import com.example.opengl.tiles.MineralType

class Resource(var mineralType: MineralType) {
    var devourerStructure: DevourerStructure? = null
    var startPosition: Position? = null
    var nextPosition: Position? = null
    var startPositionInd: PositionInd? = null
    var nextPositionInd: PositionInd? = null
    var distance // 0...1
            = 0f
    var isMoving = false
    var isDelivered = false

    fun startResource(
        startPositionInd: PositionInd,
        startPosition: Position?,
        devourerStructure: DevourerStructure?
    ) {
        this.devourerStructure = devourerStructure
        this.startPosition = startPosition
        nextPosition = null
        this.startPositionInd = startPositionInd
        nextPositionInd = calculateNextPositionInd(startPositionInd)
        distance = 0f
        isMoving = true
        isDelivered = false
    }

    fun move(delta: Float) {
        val startDevourerNode = if (startPositionInd != null) devourerStructure!!.getDevourerNode(
            startPositionInd!!.x, startPositionInd!!.y
        ) else null
        val nextDevourerNode = if (nextPositionInd != null) devourerStructure!!.getDevourerNode(
            nextPositionInd!!.x, nextPositionInd!!.y
        ) else null
        if (startDevourerNode == null && distance + delta <= 0.5f) { // in deleted start tile
            // stopped in startPosition
            isMoving = false
            return
        }
        if (nextDevourerNode == null) {
            if (distance + delta > 0.5f) {
                isMoving = false
                // stopped in nextPosition
                return
            } else {
                // stopped in startPosition;
                isMoving = false
                return
            }
        }
        val distanceOverflow = delta + distance - 1f
        if (distanceOverflow > 0) { // move to the next tile
            if (nextDevourerNode.dist == 0) { // Delivered to main devourer
                distance = 1f
                isMoving = false
                isDelivered = true
                return
            }
            startPositionInd = nextPositionInd
            nextPositionInd = calculateNextPositionInd(startPositionInd!!)
            distance = distanceOverflow
            startPosition = nextPosition
            nextPosition = null
        } else {
            distance += delta
        }
    }

    fun tryMove(delta: Float) {
        if (startPositionInd  != null) {
            nextPositionInd = calculateNextPositionInd(startPositionInd!!)
            if (nextPositionInd != null) {
                move(delta)
            }
        }
    }

    private fun calculateNextPositionInd(positionInd: PositionInd): PositionInd? {
        var result: PositionInd? = null
        //val devourerNode = devourerStructure!!.getDevourerNode(positionInd.x, positionInd.y)
        val devourerNode = devourerStructure?.getDevourerNode(positionInd.x, positionInd.y)
        if (devourerNode != null) {
            val dist = devourerNode.dist - 1
            for (neighbor in devourerNode.getNeighbors()) {
                if (neighbor.dist == dist) {
                    result = PositionInd(neighbor.x, neighbor.y)
                    break
                }
            }
        }
        return result
    }
}