package com.example.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.concurrent.locks.ReentrantLock

class RenderTileObjects(private val context: Context) {
    private val lockTile = ReentrantLock()
    private var shaderTile: Shader? = null
    private var vertexDataBuffer: FloatBuffer? = null
    private var tilesDataBuffer: ShortBuffer? = null
    private var textureBasis: Texture? = null
    private var textureMineral: Texture? = null
    private var textureDevourerBase: Texture? = null
    private var textureDevourerPipe: Texture? = null
    private var textureFog: Texture? = null
    private var vertexArrayObjectTile = 0
    private var vertexBufferObjectInstancedTile = 0
    private var mProjectionMatrix = FloatArray(16)
    private var mModelMatrix = FloatArray(16)

    init {
        shaderTile = try {
            Shader(context, "shaders/shader_tile.vert", "shaders/shader_tile.frag")
        } catch (e: IOException) {
            null
        }
        textureBasis = Texture.create(context, R.drawable.base_texture, 3)
        textureMineral = Texture.create(context, R.drawable.mineral_texture, 3)
        textureDevourerBase = Texture.create(context, R.drawable.dev_texture, 64)
        textureDevourerPipe = Texture.create(context, R.drawable.dev_pipe_texture, 64)
        textureFog = Texture.create(context, R.drawable.fog_texture, 29)
    }

    fun draw(projectionMatrix: FloatArray, modelMatrix: FloatArray) {
        if (tilesDataBuffer != null && shaderTile != null) {
            mProjectionMatrix = projectionMatrix
            mModelMatrix = modelMatrix
            if (!lockTile.isLocked) {
                lockTile.lock()
                drawTiles()
                lockTile.unlock()
            }
        }
    }

    private fun drawTiles() {
        shaderTile!!.use()
        GLES30.glBindVertexArray(vertexArrayObjectTile)
        shaderTile!!.setMatrix4("projection", mProjectionMatrix)
        shaderTile!!.setMatrix4("model", mModelMatrix)
        val tileCounts = FloatArray(5)
        tileCounts[0] = textureBasis!!.tilesCount.toFloat()
        tileCounts[1] = textureMineral!!.tilesCount.toFloat()
        tileCounts[2] = textureDevourerBase!!.tilesCount.toFloat()
        tileCounts[3] = textureFog!!.tilesCount.toFloat()
        tileCounts[4] = textureDevourerPipe!!.tilesCount.toFloat()
        shaderTile!!.setFloatArray("tileCountTexture[0]", tileCounts)
        val textures = intArrayOf(0, 1, 2, 3, 4)
        shaderTile!!.setIntArray("u_texture[0]", textures)
        textureBasis!!.use(GLES20.GL_TEXTURE0)
        textureMineral!!.use(GLES20.GL_TEXTURE0 + 1)
        textureDevourerBase!!.use(GLES20.GL_TEXTURE0 + 2)
        textureFog!!.use(GLES20.GL_TEXTURE0 + 3)
        textureDevourerPipe!!.use(GLES20.GL_TEXTURE0 + 4)
        GLES30.glDrawArraysInstanced(GLES20.GL_TRIANGLES, 0, 6, tilesDataBuffer!!.capacity() / 4)
        GLES30.glBindVertexArray(0)
    }

    private fun prepareVerticesData() {
        val vertices = floatArrayOf( // x y u v
            0f, 0f, 0f, 0f,
            SPRITE_WIDTH.toFloat(), 0f, 1f, 0f,
            0f, SPRITE_HEIGHT.toFloat(), 0f, 1f,
            SPRITE_WIDTH.toFloat(), SPRITE_HEIGHT.toFloat(), 1f, 1f,
            0f, SPRITE_HEIGHT.toFloat(), 0f, 1f,
            SPRITE_WIDTH.toFloat(), 0f, 1f, 0f
        )
        vertexDataBuffer =
            FloatBuffer.allocate(vertices.size).put(vertices).position(0) as FloatBuffer
    }

    fun prepareTileData(buffer: ShortBuffer) {
        if (shaderTile == null) return

        if (vertexDataBuffer == null) {
            prepareVerticesData()
        }
        if (tilesDataBuffer == null) {
            tilesDataBuffer = buffer
            setupTileData()
            return
        }
        lockTile.lock()
        if (tilesDataBuffer!!.capacity() != buffer.capacity()) {
            tilesDataBuffer = buffer
            bindTileData(true)
        } else {
            tilesDataBuffer!!.position(0)
            tilesDataBuffer = buffer
            bindTileData(false)
        }
        lockTile.unlock()
    }

    private fun setupTileData() {
        val ids = IntArray(1)
        GLES20.glGenBuffers(1, ids, 0)
        vertexBufferObjectInstancedTile = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile)
        tilesDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            tilesDataBuffer!!.capacity() * SHORT_SIZE_BYTES,
            tilesDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES30.glGenVertexArrays(1, ids, 0)
        vertexArrayObjectTile = ids[0]
        GLES30.glBindVertexArray(vertexArrayObjectTile)
        GLES20.glGenBuffers(1, ids, 0)
        val vertexBufferObjectTile = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectTile)
        vertexDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
            vertexDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        val vertexLocation = shaderTile!!.getAttribLocation("aPos")
        GLES20.glEnableVertexAttribArray(vertexLocation)
        GLES20.glVertexAttribPointer(
            vertexLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            0
        )
        val texCoords = shaderTile!!.getAttribLocation("aTexCoords")
        GLES20.glEnableVertexAttribArray(texCoords)
        GLES20.glVertexAttribPointer(
            texCoords,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            2 * FLOAT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile)
        val tilePositionX = shaderTile!!.getAttribLocation("aTileX")
        GLES20.glEnableVertexAttribArray(tilePositionX)
        GLES20.glVertexAttribPointer(
            tilePositionX,
            1,
            GLES20.GL_UNSIGNED_SHORT,
            false,
            4 * SHORT_SIZE_BYTES,
            0
        )
        val tilePositionY = shaderTile!!.getAttribLocation("aTileY")
        GLES20.glEnableVertexAttribArray(tilePositionY)
        GLES20.glVertexAttribPointer(
            tilePositionY,
            1,
            GLES20.GL_UNSIGNED_SHORT,
            false,
            4 * SHORT_SIZE_BYTES,
            SHORT_SIZE_BYTES
        )
        val tileNumber = shaderTile!!.getAttribLocation("aTileNumber")
        GLES20.glEnableVertexAttribArray(tileNumber)
        GLES20.glVertexAttribPointer(
            tileNumber,
            1,
            GLES20.GL_UNSIGNED_SHORT,
            false,
            4 * SHORT_SIZE_BYTES,
            2 * SHORT_SIZE_BYTES
        )
        val textureNumber = shaderTile!!.getAttribLocation("aTextureNumber")
        GLES20.glEnableVertexAttribArray(textureNumber)
        GLES20.glVertexAttribPointer(
            textureNumber,
            1,
            GLES20.GL_UNSIGNED_SHORT,
            false,
            4 * SHORT_SIZE_BYTES,
            3 * SHORT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES30.glVertexAttribDivisor(tilePositionX, 1)
        GLES30.glVertexAttribDivisor(tilePositionY, 1)
        GLES30.glVertexAttribDivisor(tileNumber, 1)
        GLES30.glVertexAttribDivisor(textureNumber, 1)
        GLES20.glEnableVertexAttribArray(0)
        GLES30.glBindVertexArray(0)
    }

    private fun bindTileData(isCreateNewBuffers: Boolean) {
        if (isCreateNewBuffers) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile)
            tilesDataBuffer!!.position(0)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                tilesDataBuffer!!.capacity() * SHORT_SIZE_BYTES,
                tilesDataBuffer,
                GLES20.GL_STATIC_DRAW
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile)
            tilesDataBuffer!!.position(0)
            GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                tilesDataBuffer!!.capacity() * SHORT_SIZE_BYTES,
                tilesDataBuffer
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    companion object {
        private const val FLOAT_SIZE_BYTES = 4
        private const val SHORT_SIZE_BYTES = 2
        private const val SPRITE_WIDTH = 149
        private const val SPRITE_HEIGHT = 129
    }
}