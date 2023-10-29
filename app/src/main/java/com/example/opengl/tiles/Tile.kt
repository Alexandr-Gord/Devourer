package com.example.opengl.tiles

import net.jcip.annotations.GuardedBy
import net.jcip.annotations.ThreadSafe

@ThreadSafe
class Tile {
    @get:Synchronized
    @set:Synchronized
    @GuardedBy("this")
    var basis: Basis? = null

    @get:Synchronized
    @set:Synchronized
    @GuardedBy("this")
    var mineral: Mineral? = null

    @get:Synchronized
    @set:Synchronized
    @GuardedBy("this")
    var entity: Entity? = null

    @get:Synchronized
    @set:Synchronized
    @GuardedBy("this")
    var fogTileId = 0

    @get:Synchronized
    @set:Synchronized
    @GuardedBy("this")
    var highlightingCode = 0
}