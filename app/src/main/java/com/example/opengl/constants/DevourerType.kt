package com.example.opengl.constants

import com.example.opengl.tiles.Entity
import com.example.opengl.tiles.EntityDevourerBase
import com.example.opengl.tiles.EntityDevourerGlow
import com.example.opengl.tiles.EntityDevourerPipe

enum class DevourerType {
    BASE, PIPE, GLOW;

    companion object {
        fun getDevourerTypeByEntity(entity: Entity?): DevourerType? {
            return when (entity) {
                is EntityDevourerBase -> BASE
                is EntityDevourerPipe -> PIPE
                is EntityDevourerGlow -> GLOW
                else -> null
            }
        }
    }
}