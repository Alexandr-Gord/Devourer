package com.example.opengl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import java.io.IOException
import java.nio.FloatBuffer
import java.util.concurrent.locks.ReentrantLock

class RenderText(private val context: Context) {
    private val lockText = ReentrantLock()
    private var shaderText: Shader? = null
    private var vertexUnitDataBuffer: FloatBuffer? = null
    private var textDataBuffer: FloatBuffer? = null
    private var textureFont: Texture? = null
    private var textureImages: Texture? = null
    private var vertexArrayObjectText = 0
    private var vertexBufferObjectInstancedText = 0
    private var mProjectionMatrix = FloatArray(16)
    private var mModelMatrix = FloatArray(16)
    var isReady: Boolean = false

    init {
        shaderText = try {
            Shader(context, "shaders/shader_text.vert", "shaders/shader_text.frag")
        } catch (e: IOException) {
            null
        }
    }

    fun initFontsTexture(bitmap: Bitmap) {
        textureFont = Texture.createFontsTexture(context, bitmap)
        isReady = true
    }

    fun initImagesTexture(bitmap: Bitmap) {
        textureImages = Texture.create(context, bitmap)
        // TODO isReady ?????
    }

    fun draw(projectionMatrix: FloatArray, modelMatrix: FloatArray) {
        //if (!isReady) return
        if (textureFont != null && textureImages != null) {
            if (textDataBuffer != null && shaderText != null) {
                mProjectionMatrix = projectionMatrix
                mModelMatrix = modelMatrix
                if (!lockText.isLocked) {
                    lockText.lock()
                    drawText()
                    lockText.unlock()
                }
            }
        }
    }

    private fun drawText() {
        shaderText!!.use()
        GLES30.glBindVertexArray(vertexArrayObjectText)
        shaderText!!.setMatrix4("projection", mProjectionMatrix)
        shaderText!!.setVector2("sizeFontsTexture", textureFont!!.width.toFloat(), textureFont!!.height.toFloat())
        shaderText!!.setVector2("sizeImagesTexture", textureImages!!.width.toFloat(), textureImages!!.height.toFloat())
        shaderText!!.setInt("u_textureFonts", 6)
        textureFont!!.use(GLES20.GL_TEXTURE6)
        shaderText!!.setInt("u_textureImages", 7)
        textureImages!!.use(GLES20.GL_TEXTURE7)
        GLES30.glDrawArraysInstanced(GLES20.GL_TRIANGLES, 0, 6, textDataBuffer!!.capacity() / 14)
        GLES30.glBindVertexArray(0)
    }

    private fun prepareVerticesData() {

        val verticesUnit = floatArrayOf( // x y u v
            0f, 0f, 0f, 1f,
            1f, 0f, 1f, 1f,
            0f, 1f, 0f, 0f,
            1f, 1f, 1f, 0f,
            0f, 1f, 0f, 0f,
            1f, 0f, 1f, 1f
        )

        vertexUnitDataBuffer = FloatBuffer.allocate(verticesUnit.size)
            .put(verticesUnit)
            .position(0) as FloatBuffer
    }

    fun prepareTextData(buffer: FloatBuffer?) {
        if (shaderText == null) return
        if (buffer == null) return
        if (vertexUnitDataBuffer == null) {
            prepareVerticesData()
        }
        if (textDataBuffer == null) {
            textDataBuffer = buffer
            setupTextData()
            return
        }
        lockText.lock()
        if (textDataBuffer!!.capacity() != buffer.capacity()) {
            textDataBuffer = buffer
            bindTextData(true)
        } else {
            textDataBuffer!!.position(0)
            textDataBuffer = buffer
            bindTextData(false)
        }
        lockText.unlock()
    }

    private fun setupTextData() {
        val ids = IntArray(1)
        GLES20.glGenBuffers(1, ids, 0)
        vertexBufferObjectInstancedText = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedText)
        textDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            textDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
            textDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES30.glGenVertexArrays(1, ids, 0)
        vertexArrayObjectText = ids[0]
        GLES30.glBindVertexArray(vertexArrayObjectText)
        GLES20.glGenBuffers(1, ids, 0)
        val vertexBufferObjectText = ids[0]
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectText)
        vertexUnitDataBuffer!!.position(0)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexUnitDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
            vertexUnitDataBuffer,
            GLES20.GL_STATIC_DRAW
        )
        val vertexLocation = shaderText!!.getAttribLocation("aPos")
        GLES20.glEnableVertexAttribArray(vertexLocation)
        GLES20.glVertexAttribPointer(
            vertexLocation,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            0
        )
        val texCoords = shaderText!!.getAttribLocation("aTexCoords")
        GLES20.glEnableVertexAttribArray(texCoords)
        GLES20.glVertexAttribPointer(
            texCoords,
            2,
            GLES20.GL_FLOAT,
            false,
            4 * FLOAT_SIZE_BYTES,
            2 * FLOAT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedText)
        val charPos = shaderText!!.getAttribLocation("aCharPos")
        GLES20.glEnableVertexAttribArray(charPos)
        GLES20.glVertexAttribPointer(
            charPos,
            2,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            0
        )
        val charSize = shaderText!!.getAttribLocation("aCharSize")
        GLES20.glEnableVertexAttribArray(charSize)
        GLES20.glVertexAttribPointer(
            charSize,
            2,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            2 * FLOAT_SIZE_BYTES
        )
        val charPosInTexture = shaderText!!.getAttribLocation("aCharPosInTexture")
        GLES20.glEnableVertexAttribArray(charPosInTexture)
        GLES20.glVertexAttribPointer(
            charPosInTexture,
            2,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            4 * FLOAT_SIZE_BYTES
        )
        val textColor = shaderText!!.getAttribLocation("aTextColor")
        GLES20.glEnableVertexAttribArray(textColor)
        GLES20.glVertexAttribPointer(
            textColor,
            3,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            6 * FLOAT_SIZE_BYTES
        )
        val textStartPos = shaderText!!.getAttribLocation("aTextStartPos")
        GLES20.glEnableVertexAttribArray(textStartPos)
        GLES20.glVertexAttribPointer(
            textStartPos,
            2,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            9 * FLOAT_SIZE_BYTES
        )
        val scale = shaderText!!.getAttribLocation("aScale")
        GLES20.glEnableVertexAttribArray(scale)
        GLES20.glVertexAttribPointer(
            scale,
            2,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            11 * FLOAT_SIZE_BYTES
        )
        val type = shaderText!!.getAttribLocation("aType")
        GLES20.glEnableVertexAttribArray(type)
        GLES20.glVertexAttribPointer(
            type,
            1,
            GLES20.GL_FLOAT,
            false,
            14 * FLOAT_SIZE_BYTES,
            13 * FLOAT_SIZE_BYTES
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES30.glVertexAttribDivisor(charPos, 1)
        GLES30.glVertexAttribDivisor(charSize, 1)
        GLES30.glVertexAttribDivisor(charPosInTexture, 1)
        GLES30.glVertexAttribDivisor(textColor, 1)
        GLES30.glVertexAttribDivisor(textStartPos, 1)
        GLES30.glVertexAttribDivisor(scale, 1)
        GLES30.glVertexAttribDivisor(type, 1)
        GLES20.glEnableVertexAttribArray(0)
        GLES30.glBindVertexArray(0)
    }

    private fun bindTextData(isCreateNewBuffers: Boolean) {
        if (isCreateNewBuffers) {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedText)
            textDataBuffer!!.position(0)
            GLES20.glBufferData(
                GLES20.GL_ARRAY_BUFFER,
                textDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
                textDataBuffer,
                GLES20.GL_STATIC_DRAW
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        } else {
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferObjectInstancedText)
            textDataBuffer!!.position(0)
            GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                textDataBuffer!!.capacity() * FLOAT_SIZE_BYTES,
                textDataBuffer
            )
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    companion object {
        private const val FLOAT_SIZE_BYTES = 4
    }
}