package com.example.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.Matrix
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class RenderFreeObjects (private val context: Context) {
    private val lockMoving = ReentrantLock()
    private var shaderMoving: Shader? = null
    private var vertexUnitDataBuffer: FloatBuffer? = null
    private var movingDataBuffer: FloatBuffer? = null
    private var textureMoving: Texture? = null
    private var textureMainDevourer: Texture? = null
    private var vertexArrayObjectMoving = 0
    private var vertexBufferObjectInstancedMoving = 0
    private var mProjectionMatrix = FloatArray(16)
    private var mModelMatrix = FloatArray(16)

    init {
        shaderMoving = try {
            Shader(context, "shaders/shader_moving.vert", "shaders/shader_moving.frag")
        } catch (e: IOException) {
            null
        }
        textureMoving = Texture.Create(context, R.drawable.moving_texture, 1)
        textureMainDevourer = Texture.Create(context, R.drawable.main_texture, 23)
    }


     fun draw(projectionMatrix: FloatArray, modelMatrix: FloatArray) {
        if (movingDataBuffer != null && shaderMoving != null) {
            mProjectionMatrix = projectionMatrix
            mModelMatrix = modelMatrix
            if (!lockMoving.isLocked) {
                lockMoving.lock()
                drawMoving()
                lockMoving.unlock()
            }
        }
    }

    private fun drawMoving() {
        shaderMoving!!.use()
        GLES30.glBindVertexArray(vertexArrayObjectMoving)
        shaderMoving!!.setMatrix4("projection", mProjectionMatrix)
        shaderMoving!!.setMatrix4("model", mModelMatrix)
        val tileCounts = FloatArray(2)
        tileCounts[0] = textureMoving!!.tilesCount.toFloat()
        tileCounts[1] = textureMainDevourer!!.tilesCount.toFloat()
        shaderMoving!!.setFloatArray("tileCountTexture[0]", tileCounts)
        val textures = intArrayOf(4, 5)
        shaderMoving!!.setIntArray("u_texture[0]", textures)
        textureMoving!!.Use(GLES20.GL_TEXTURE0 + 4)
        textureMainDevourer!!.Use(GLES20.GL_TEXTURE0 + 5)
        GLES30.glDrawArraysInstanced(GLES20.GL_TRIANGLES, 0, 6, movingDataBuffer!!.capacity() / 4) // was tilesDataBuffer ???
        GLES30.glBindVertexArray(0)
    }


    private fun prepareVerticesData() {
        val verticesUnit = floatArrayOf( // x y u v
            0f, 0f, 0f, 0f,
            1f, 0f, 1f, 0f,
            0f, 1f, 0f, 1f,
            1f, 1f, 1f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 1f, 0f
        )
        vertexUnitDataBuffer = FloatBuffer.allocate(verticesUnit.size)
            .put(verticesUnit)
            .position(0) as FloatBuffer
    }

    fun prepareMovingData(buffer: FloatBuffer?) {
        if (shaderMoving == null) return
        if (buffer == null) return
        if (vertexUnitDataBuffer == null) {
            prepareVerticesData()
        }
        if (movingDataBuffer == null) {
            movingDataBuffer = buffer
            setupMovingData()
            return
        }
        lockMoving.lock()
        if (movingDataBuffer!!.capacity() != buffer.capacity()) {
            movingDataBuffer = buffer
            bindMovingData(true)
        } else {
            movingDataBuffer!!.position(0)
            movingDataBuffer = buffer
            bindMovingData(false)
        }
        lockMoving.unlock()
    }

    private fun setupMovingData() {
        val ids = IntArray(1)
        GLES20.glGenBuffers(1, ids, 0)
        vertexBufferObjectInstancedMoving = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving)
        movingDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            movingDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
            movingDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES30.glGenVertexArrays(1, ids, 0)
        vertexArrayObjectMoving = ids[0]
        GLES30.glBindVertexArray(vertexArrayObjectMoving)
        GLES20.glGenBuffers(1, ids, 0)
        val vertexBufferObjectMoving = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectMoving)
        vertexUnitDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexUnitDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
            vertexUnitDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        val vertexLocation = shaderMoving!!.getAttribLocation("aPos")
        GLES20.glEnableVertexAttribArray(vertexLocation)
        GLES20.glVertexAttribPointer(
            vertexLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            0
        )
        val texCoords = shaderMoving!!.getAttribLocation("aTexCoords")
        GLES20.glEnableVertexAttribArray(texCoords)
        GLES20.glVertexAttribPointer(
            texCoords,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            2 * FLOAT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving)
        val tilePositionX = shaderMoving!!.getAttribLocation("aTileX")
        GLES20.glEnableVertexAttribArray(tilePositionX)
        GLES20.glVertexAttribPointer(
            tilePositionX,
            1,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            0
        )
        val tilePositionY = shaderMoving!!.getAttribLocation("aTileY")
        GLES20.glEnableVertexAttribArray(tilePositionY)
        GLES20.glVertexAttribPointer(
            tilePositionY,
            1,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            FLOAT_SIZE_BYTES
        )
        val tileNumber = shaderMoving!!.getAttribLocation("aTileNumber")
        GLES20.glEnableVertexAttribArray(tileNumber)
        GLES20.glVertexAttribPointer(
            tileNumber,
            1,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            2 * FLOAT_SIZE_BYTES
        )
        val textureNumber = shaderMoving!!.getAttribLocation("aTextureNumber")
        GLES20.glEnableVertexAttribArray(textureNumber)
        GLES20.glVertexAttribPointer(
            textureNumber,
            1,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            3 * FLOAT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES30.glVertexAttribDivisor(tilePositionX, 1)
        GLES30.glVertexAttribDivisor(tilePositionY, 1)
        GLES30.glVertexAttribDivisor(tileNumber, 1)
        GLES30.glVertexAttribDivisor(textureNumber, 1)
        GLES20.glEnableVertexAttribArray(0)
        GLES30.glBindVertexArray(0)
    }

    private fun bindMovingData(isCreateNewBuffers: Boolean) {
        if (isCreateNewBuffers) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving)
            movingDataBuffer!!.position(0)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                movingDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
                movingDataBuffer,
                GLES20.GL_STATIC_DRAW
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving)
            movingDataBuffer!!.position(0)
            GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                movingDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
                movingDataBuffer
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    companion object {
        private const val FLOAT_SIZE_BYTES = 4
    }
}