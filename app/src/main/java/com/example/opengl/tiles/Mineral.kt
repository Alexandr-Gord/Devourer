package com.example.opengl.tiles

import com.example.opengl.ResourceDepository

class Mineral : Layer() {
    @JvmField
    var type: MineralType? = null
    @JvmField
    var resourceDepository: ResourceDepository? = null
}