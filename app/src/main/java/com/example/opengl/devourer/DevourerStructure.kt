package com.example.opengl.devourer

import com.example.opengl.Position
import com.example.opengl.PositionInd
import com.example.opengl.tiles.TileMap
import java.util.ArrayDeque
import java.util.Collections
import java.util.Queue

class DevourerStructure {
    private val map = Collections.synchronizedMap(HashMap<Int, DevourerNode?>())
    var main: DevourerNode? = null
    fun getNumberOfAllNodes(): Int {
        return map.size
    }

    private fun getIndex(x: Int, y: Int): Int {
        return y * INDEX_WIDTH + x
    }

    fun getDevourerNode(x: Int, y: Int): DevourerNode? {
        return map[getIndex(x, y)]
    }

    fun getDevourerNodeDist(x: Int, y: Int): Int {
        val devourerNode = map[getIndex(x, y)]
        return devourerNode?.dist ?: 0
    }

    fun createStructure(tileMap: TileMap) {
        synchronized(map) {
            main = DevourerNode(tileMap.mainDevourerTileIndX, tileMap.mainDevourerTileIndY, 0)
            map.clear()
            map[getIndex(main!!.x, main!!.y)] = main
            var x: Int
            var y: Int
            var index: Int
            var neighbor: DevourerNode?
            val q: Queue<DevourerNode> = ArrayDeque()
            q.add(main)
            //while (!q.isEmpty()) {
            while (true) {
                val node = q.poll() ?: break
                val newDist = node.dist + 1
                for (k in 0..5) {
                    x = node.x + NEIGHBOR_DX[k]
                    y = if (node.x % 2 == 0) {
                        node.y + NEIGHBOR_EVEN_X_DY[k]
                    } else {
                        node.y + NEIGHBOR_ODD_X_DY[k]
                    }
                    if (isValid(tileMap, x, y)) {
                        index = getIndex(x, y)
                        neighbor = map[index]
                        if (neighbor == null) {
                            val newNeighbor = DevourerNode(x, y, newDist)
                            node.addNeighbor(newNeighbor)
                            q.add(newNeighbor)
                            map[index] = newNeighbor
                        } else {
                            /*
                        if (neighbor.dist > newDist) { // ?????
                            neighbor.dist = newDist;
                        }
                         */
                            node.addNeighbor(neighbor)
                        }
                    }
                }
            }
        }
    }

    fun getPathToMainEntity(
        x: Int,
        y: Int,
        tileMapHeight: Int,
        betweenTileCentersX: Float,
        betweenTileCentersY: Float
    ): ArrayList<Position> {
        val path = ArrayList<Position>()
        var node = getDevourerNode(x, y) ?: return path
        var dist = node.dist
        val startPosition = Position()
        startPosition.x = x * betweenTileCentersX
        if (x % 2 == 0) {
            startPosition.y = (tileMapHeight - y) * betweenTileCentersY
        } else {
            startPosition.y = (tileMapHeight - y - 0.5f) * betweenTileCentersY
        }
        path.add(startPosition)
        dist--
        while (dist > -1) {
            for (neighbor in node.getNeighbors()) {
                if (neighbor.dist == dist) {
                    node = neighbor
                    val position = Position()
                    position.x = node.x * betweenTileCentersX
                    if (node.x % 2 == 0) {
                        position.y = (tileMapHeight - node.y) * betweenTileCentersY
                    } else {
                        position.y = (tileMapHeight - node.y - 0.5f) * betweenTileCentersY
                    }
                    path.add(position)
                    break
                }
            }
            dist--
        }
        return path
    }

    fun getNextPositionInd(x: Int, y: Int): PositionInd? {
        val node = getDevourerNode(x, y) ?: return null
        var dist = node.dist
        var position: PositionInd? = PositionInd()
        if (node.dist == 0) {
            position!!.x = x
            position.y = y
        } else if (node.dist > 0) {
            dist--
            for (neighbor in node.getNeighbors()) {
                if (neighbor.dist == dist) {
                    position!!.x = neighbor.x
                    position.y = neighbor.y
                    break
                }
            }
        } else {
            position = null
        }
        return position
    }

    fun addDevourerNode(x: Int, y: Int, tileMap: TileMap) {
        val near = ArrayList<DevourerNode>()
        var indX: Int
        var indY: Int
        var maxDist = 0
        var minDist = Int.MAX_VALUE
        for (k in 0..5) {
            indX = x + NEIGHBOR_DX[k]
            indY = if (x % 2 == 0) {
                y + NEIGHBOR_EVEN_X_DY[k]
            } else {
                y + NEIGHBOR_ODD_X_DY[k]
            }
            val node = getDevourerNode(indX, indY)
            if (tileMap.getTile(indX, indY) != null && tileMap.getTile(
                    indX,
                    indY
                )!!.entity != null && node == null
            ) { // if connect with a separate devourer area
                createStructure(tileMap)
                return
            }
            if (node != null) {
                near.add(node)
                if (node.dist > maxDist) {
                    maxDist = node.dist
                }
                if (node.dist < minDist) {
                    minDist = node.dist
                }
            }
        }
        if (near.size > 0) {
            if (maxDist - minDist < 2) {
                val newDist = if (maxDist == minDist) minDist + 1 else maxDist
                val newNode = DevourerNode(x, y, newDist)
                map[getIndex(x, y)] = newNode
                for (neighbor in near) {
                    newNode.addNeighbor(neighbor)
                    neighbor.addNeighbor(newNode)
                }
            } else {
                createStructure(tileMap)
            }
        }
    }

    fun removeDevourerNode(x: Int, y: Int, tileMap: TileMap) {
        val node : DevourerNode = getDevourerNode(x, y) ?: return
        val near = ArrayList<DevourerNode>()
        var indX: Int
        var indY: Int
        var maxDist = 0
        var minDist = Int.MAX_VALUE
        var neighborNode: DevourerNode?
        for (k in 0..5) {
            indX = x + NEIGHBOR_DX[k]
            indY = if (x % 2 == 0) {
                y + NEIGHBOR_EVEN_X_DY[k]
            } else {
                y + NEIGHBOR_ODD_X_DY[k]
            }
            neighborNode = getDevourerNode(indX, indY)
            if (neighborNode != null) {
                near.add(neighborNode)
                if (neighborNode.dist > maxDist) {
                    maxDist = neighborNode.dist
                }
                if (neighborNode.dist < minDist) {
                    minDist = neighborNode.dist
                }
            }
        }
        map.remove(getIndex(x, y), node)
        if (maxDist == minDist && maxDist <= node.dist) {
            for (neighbor in near) {
                neighbor.removeNeighbor(node)
            }
        } else {
            createStructure(tileMap)
        }
    }

    companion object {
        private val NEIGHBOR_DX = intArrayOf(0, 1, 1, 0, -1, -1)
        private val NEIGHBOR_EVEN_X_DY = intArrayOf(-1, -1, 0, 1, 0, -1)
        private val NEIGHBOR_ODD_X_DY = intArrayOf(-1, 0, 1, 1, 1, 0)
        private const val INDEX_WIDTH = 10000 // for square map max 46340
        private fun isValid(tileMap: TileMap, x: Int, y: Int): Boolean {
            return tileMap.isTileExist(x, y) && tileMap.isEntity(x, y)
        }
    }
}