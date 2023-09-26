package com.example.opengl.devourer

class DevourerNode(var x: Int, var y: Int, var dist: Int) {
    private val neighbors: MutableList<DevourerNode> = ArrayList(6) //TODO synchronizedList ???
    fun addNeighbor(devourerNode: DevourerNode) {
        neighbors.add(devourerNode)
    }

    fun removeNeighbor(devourerNode: DevourerNode) {
        neighbors.remove(devourerNode)
    }

    fun getNeighbors(): List<DevourerNode> {
        return neighbors
    }
}