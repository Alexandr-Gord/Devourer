package com.example.opengl;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGenerateMipmap;
import static android.opengl.GLES20.glTexParameteri;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Texture {
    public final int handle;
    public final int tilesCount;

    public Texture(int handle, int tilesCount) {
        this.handle = handle;
        this.tilesCount = tilesCount;
    }

    public static Texture Create(Context context, int resourceId, int tilesCount) {
        // Generate handle
        final int[] textureIds = new int[1];
        glGenTextures(1, textureIds, 0);
        int handle = textureIds[0];
        if (textureIds[0] == 0) {
            return null;
        }

        // Bind the handle
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, handle);

        // create Bitmap
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        final Bitmap bitmap = BitmapFactory.decodeResource(
                context.getResources(), resourceId, options);

        if (bitmap == null) {
            glDeleteTextures(1, textureIds, 0);
            return null;
        }

        //GL.TexImage2D(TextureTarget.Texture2D, 0, PixelInternalFormat.Rgba, palette.Count, 1, 0, PixelFormat.Rgba, PixelType.UnsignedByte, palette.ToArray());
        //GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);
        
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmap, 0);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        // Generate mipmaps.
        glGenerateMipmap(GL_TEXTURE_2D);

        bitmap.recycle();

        // reset target
        glBindTexture(GL_TEXTURE_2D, 0);

        return new Texture(handle, tilesCount);
    }

    // Activate texture
    // Multiple textures can be bound, if your shader needs more than just one.
    // If you want to do that, use GL.ActiveTexture to set which slot GL.BindTexture binds to.
    // The OpenGL standard requires that there be at least 16, but there can be more depending on your graphics card.
    public void Use(int unit) {
        glActiveTexture(unit);
        glBindTexture(GL_TEXTURE_2D, handle);
    }

    protected void finalize() {
        if (handle > 0) {
            final int[] textureIds = new int[1];
            textureIds[0] = handle;
            glDeleteTextures(1, textureIds, 0);
        }
    }
}
