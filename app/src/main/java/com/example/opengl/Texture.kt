package com.example.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

class Texture(private val handle: Int, val tilesCount: Int,  val width: Int, val height: Int) {
    fun use(unit: Int) {
        GLES20.glActiveTexture(unit)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
    }

    protected fun finalize() {
        if (handle > 0) {
            val textureIds = IntArray(1)
            textureIds[0] = handle
            GLES20.glDeleteTextures(1, textureIds, 0)
        }
    }

    companion object {
        fun create(context: Context, resourceId: Int, tilesCount: Int): Texture? {
            // Generate handle
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            val handle = textureIds[0]
            if (textureIds[0] == 0) {
                return null
            }

            // Bind the handle
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)

            // create Bitmap
            val options = BitmapFactory.Options()
            options.inScaled = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            val bitmap = BitmapFactory.decodeResource(
                context.resources, resourceId, options
            )
            if (bitmap == null) {
                GLES20.glDeleteTextures(1, textureIds, 0)
                return null
            }
            val width = bitmap.width
            val height = bitmap.height

            //GL.TexImage2D(TextureTarget.Texture2D, 0, PixelInternalFormat.Rgba, palette.Count, 1, 0, PixelFormat.Rgba, PixelType.UnsignedByte, palette.ToArray());
            //GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, bitmap, 0)
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            // Generate mipmaps.
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)
            bitmap.recycle()

            // reset target
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            return Texture(handle, tilesCount, width, height)
        }

        fun create(context: Context, bitmap: Bitmap): Texture? {
            // Generate handle
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            val handle = textureIds[0]
            if (textureIds[0] == 0) {
                return null
            }

            // Bind the handle
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, handle)
            val width = bitmap.width
            val height = bitmap.height

            // set 1 byte pixel alignment
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 1)
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap,0)
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE
            )

            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
            // set default (4 byte) pixel alignment
            GLES20.glPixelStorei(GLES20.GL_UNPACK_ALIGNMENT, 4)
            return Texture(handle, 0, width, height)
        }
    }
}