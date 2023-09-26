package com.example.opengl

import com.example.opengl.tiles.MineralType
import java.util.Collections

class ResourcesStorage {
    private val resourcesStorage = Collections.synchronizedMap(HashMap<MineralType, Int>())

    init {
        for (mineralType in MineralType.values()) {
            resourcesStorage[mineralType] = 0
        }
    }

    fun getMineralCount(mineralType: MineralType?): Int {
        val count = resourcesStorage[mineralType]
        return count ?: 0
    }

    fun addMineral(mineralType: MineralType, count: Int) {
        //val currentCount = resourcesStorage[mineralType] ?: 0
        //val newCount = if (currentCount == null) count else count + currentCount
        val newCount = count + (resourcesStorage[mineralType] ?: 0)
        resourcesStorage[mineralType] = newCount
    }
}