package com.example.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_ALPHA
import android.opengl.GLES20.GL_ONE
import android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA
import android.opengl.GLES20.GL_SRC_ALPHA
import android.opengl.GLES20.glBlendFunc
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class OpenGLRenderer(private val context: Context) : GLSurfaceView.Renderer {
    var renderTileObjects : RenderTileObjects? = null
    var renderFreeObjects : RenderFreeObjects? = null
    var renderText : RenderText? = null
    private var startTime = System.nanoTime()
    private var frames = 0
    private var fps = 0
    private var currentScale = 1.0f
    private var translateX = 0f
    private var translateY = 0f
    private val mProjectionMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val scaleMatrix = FloatArray(16)
    private val transMatrix = FloatArray(16)
    fun setCurrentScale(scale: Float) {
        currentScale = scale
    }

    fun setTranslate(transX: Float, transY: Float) {
        translateX = transX
        translateY = transY
    }

    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        GLES20.glClearColor(0.2f, 0.2f, 0.2f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        //glEnable(GL_DEPTH_TEST);
        /*
        GLES20.glBlendFuncSeparate(
            GLES20.GL_SRC_ALPHA,
            GLES20.GL_ONE_MINUS_SRC_ALPHA,
            GLES20.GL_ONE,
            GLES20.GL_ZERO
        )
        */
        //glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        //glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        renderTileObjects = RenderTileObjects(context)
        renderFreeObjects = RenderFreeObjects(context)
        renderText = RenderText(context)
    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        Matrix.orthoM(mProjectionMatrix, 0, 0f, width.toFloat(), 0f, height.toFloat(), -1.0f, 1.0f)
        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.setIdentityM(transMatrix, 0)
        Matrix.scaleM(scaleMatrix, 0, currentScale, currentScale, 0f)
        Matrix.translateM(transMatrix, 0, translateX, translateY, 0f)
        Matrix.multiplyMM(mModelMatrix, 0, transMatrix, 0, scaleMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        Matrix.setIdentityM(transMatrix, 0)
        Matrix.setIdentityM(scaleMatrix, 0)
        Matrix.translateM(transMatrix, 0, translateX, translateY, 0f)
        Matrix.scaleM(scaleMatrix, 0, currentScale, currentScale, 0f)
        Matrix.multiplyMM(mModelMatrix, 0, transMatrix, 0, scaleMatrix, 0)

        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA)
        renderTileObjects!!.draw(mProjectionMatrix, mModelMatrix)
        renderFreeObjects!!.draw(mProjectionMatrix, mModelMatrix)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        renderText!!.draw(mProjectionMatrix, mModelMatrix)

        logFrame()
    }

    private fun logFrame() {
        frames++
        if (System.nanoTime() - startTime >= 1000000000) {
            fps = frames
            //Game.getInstance().showMessage("fps:" + fps);
            frames = 0
            startTime = System.nanoTime()
        }
    }
/*
    private void bindData() {
        int[] vaoIds = new int[1];
        glGenVertexArrays(1, vaoIds, 0);

        vao = vaoIds[0];
        glBindVertexArray(vao);
        int[] vboIds = new int[1];
        glGenBuffers(1, vboIds, 0);
        vbo = vboIds[0];
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        vertexDataBuffer.position(0);
        glBufferData(GL_ARRAY_BUFFER, 4 * 6 * 4, vertexDataBuffer, GL_STATIC_DRAW);

        int position = shader.GetAttribLocation("aPos");
        glEnableVertexAttribArray(position);
        glVertexAttribPointer(position, 2, GL_FLOAT, false, 4 * 4, 0);

        int texCoords = shader.GetAttribLocation("aTexCoords");
        glEnableVertexAttribArray(texCoords);
        glVertexAttribPointer(texCoords, 2, GL_FLOAT, false, 4 * 4, 2 * 4);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void bindVertexData() {
        mapHeight = 3;
        mapWidth = 3;
        final int numVerticesPerStrip = (mapHeight + 1) * 2;
        final int numDegenerateVertices = (mapWidth - 1) * 2;
        float[] vertexData = new float[(mapWidth * numVerticesPerStrip + numDegenerateVertices) * 2];

        float minX, maxX, maxY;
        int pos = 0;

        for (int x = 0; x < mapWidth; x++) {
            if (x % 2 == 0) {
                maxY = mapHeight * SPRITE_HEIGHT;
            } else {
                maxY = (mapHeight + 0.5f) * SPRITE_HEIGHT;
            }
            //minY = maxY - SPRITE_HEIGHT;
            minX = x * betweenTileCentersX;
            maxX = minX + SPRITE_WIDTH;

            vertexData[pos++] = maxX; // position X vertex 1
            vertexData[pos++] = maxY; // position Y vertex 1

            // Degenerate begin: repeat first vertex
            if (x > 0) {
                vertexData[pos++] = maxX;
                vertexData[pos++] = maxY;
            }

            vertexData[pos++] = minX; // position X vertex 2
            vertexData[pos++] = maxY; // position Y vertex 2

            for (int y = 0; y < mapHeight; y++) {
                maxY -= SPRITE_HEIGHT;
                vertexData[pos++] = maxX; // position X vertex 1
                vertexData[pos++] = maxY; // position Y vertex 1
                vertexData[pos++] = minX; // position X vertex 2
                vertexData[pos++] = maxY; // position Y vertex 2
            }
            // Degenerate end: repeat last vertex
            if (x < mapWidth - 1) {
                vertexData[pos++] = minX;
                vertexData[pos++] = maxY;
            }
        }
        vertexDataBuffer1 = FloatBuffer.allocate(vertexData.length);
        vertexDataBuffer1.put(vertexData);
    }

    private void bindIndexData() {
        final int numStripsRequired = mapHeight - 1;
        final int numDegensRequired = 2 * (numStripsRequired - 1);
        final int verticesPerStrip = 2 * mapWidth;

        short[] heightMapIndexData = new short[(verticesPerStrip * numStripsRequired) + numDegensRequired];

        int offset = 0;

        for (int y = 0; y < mapHeight - 1; y++) {
            if (y > 0) {
                // Degenerate begin: repeat first vertex
                heightMapIndexData[offset++] = (short) (y * mapHeight);
            }

            for (int x = 0; x < mapWidth; x++) {
                // One part of the strip
                heightMapIndexData[offset++] = (short) ((y * mapHeight) + x);
                heightMapIndexData[offset++] = (short) (((y + 1) * mapHeight) + x);
            }

            if (y < mapHeight - 2) {
                // Degenerate end: repeat last vertex
                heightMapIndexData[offset++] = (short) (((y + 1) * mapHeight) + (mapWidth - 1));
            }
        }

    }
*/
}
// text renderer
// https://arm-software.github.io/opengl-es-sdk-for-android/high_quality_text.html
// http://fractiousg.blogspot.com/2012/04/rendering-text-in-opengl-on-android.html


// OpenGL ES SDK for Android !!!!!!
// https://arm-software.github.io/opengl-es-sdk-for-android/pages.html

// https://en.wikipedia.org/wiki/Strip_packing_problem
// https://www.david-colson.com/2020/03/10/exploring-rect-packing.html