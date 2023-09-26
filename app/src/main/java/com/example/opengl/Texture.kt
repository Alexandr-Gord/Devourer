package com.example.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils

class Texture(val handle: Int, val tilesCount: Int) {
    // Activate texture
    // Multiple textures can be bound, if your shader needs more than just one.
    // If you want to do that, use GL.ActiveTexture to set which slot GL.BindTexture binds to.
    // The OpenGL standard requires that there be at least 16, but there can be more depending on your graphics card.
    fun Use(unit: Int) {
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
        fun Create(context: Context, resourceId: Int, tilesCount: Int): Texture? {
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
            return Texture(handle, tilesCount)
        }
    }
}