package com.example.opengl;

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_ONE;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.GL_ZERO;
import static android.opengl.GLES20.glBlendFuncSeparate;
import static android.opengl.GLES20.glBufferSubData;
import static android.opengl.GLES30.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES30.GL_TRIANGLES;
import static android.opengl.GLES30.GL_TEXTURE0;
import static android.opengl.GLES30.GL_FLOAT;
import static android.opengl.GLES30.glDrawArraysInstanced;
import static android.opengl.GLES30.glVertexAttribDivisor;
import static android.opengl.GLES30.glVertexAttribPointer;
import static android.opengl.GLES30.glEnableVertexAttribArray;
import static android.opengl.GLES30.GL_STATIC_DRAW;
import static android.opengl.GLES30.glBufferData;
import static android.opengl.GLES30.GL_ARRAY_BUFFER;
import static android.opengl.GLES30.glBindBuffer;
import static android.opengl.GLES30.glGenBuffers;
import static android.opengl.GLES30.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES30.glBindVertexArray;
import static android.opengl.GLES30.glClear;
import static android.opengl.GLES30.glClearColor;
import static android.opengl.GLES30.glEnable;
import static android.opengl.GLES30.glGenVertexArrays;
import static android.opengl.GLES30.glViewport;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenGLRenderer implements GLSurfaceView.Renderer {
    private final ReentrantLock lockTile = new ReentrantLock();
    private final ReentrantLock lockMoving = new ReentrantLock();
    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int SHORT_SIZE_BYTES = 2;
    private final Context context;
    private Shader shaderTile;
    private Shader shaderMoving;
    private FloatBuffer vertexDataBuffer;
    private ShortBuffer tilesDataBuffer;
    private FloatBuffer movingDataBuffer;
    private Texture textureBasis;
    private Texture textureMineral;
    private Texture textureEntity;
    private Texture textureFog;
    private Texture textureMoving;
    private int vertexArrayObjectTile;
    private int vertexArrayObjectMoving;
    private int vertexBufferObjectInstancedTile;
    private int vertexBufferObjectInstancedMoving;
    private long startTime = System.nanoTime();
    private int frames = 0;
    private int fps = 0;
    public static final int SPRITE_WIDTH = 149;
    public static final int SPRITE_HEIGHT = 129;
    private float currentScale = 1.0f;
    private float translateX = 0;
    private float translateY = 0;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];
    private final float[] scaleMatrix = new float[16];
    private final float[] transMatrix = new float[16];

    public OpenGLRenderer(Context context) {
        this.context = context;
    }

    public void setCurrentScale(float scale) {
        this.currentScale = scale;
    }

    public void setTranslate(float transX, float transY) {
        this.translateX = transX;
        this.translateY = transY;
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        glClearColor(0.2f, 0.2f, 0.2f, 0f);
        glEnable(GL_BLEND);
        //glEnable(GL_DEPTH_TEST);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        try {
            this.shaderTile = new Shader(context, "shaders/shader_tile.vert", "shaders/shader_tile.frag");
        } catch (IOException e) {
            this.shaderTile = null;
        }
        try {
            this.shaderMoving = new Shader(context, "shaders/shader_moving.vert", "shaders/shader_moving.frag");
        } catch (IOException e) {
            this.shaderMoving = null;
        }
        this.textureBasis = Texture.Create(context, R.drawable.base_texture, 2);
        this.textureMineral = Texture.Create(context, R.drawable.mineral_texture, 3);
        this.textureEntity = Texture.Create(context, R.drawable.dev_texture, 64);
        this.textureFog = Texture.Create(context, R.drawable.fog_texture, 29);
        this.textureMoving = Texture.Create(context, R.drawable.moving_texture, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, 0, height, -1.0f, 1.0f);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.setIdentityM(transMatrix, 0);
        Matrix.scaleM(scaleMatrix, 0, currentScale, currentScale, 0);
        Matrix.translateM(transMatrix, 0, translateX, translateY, 0);
        Matrix.multiplyMM(mModelMatrix, 0, transMatrix, 0, scaleMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if ((tilesDataBuffer != null) && (shaderTile != null)) {
            if (!lockTile.isLocked()) {
                lockTile.lock();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                drawTiles();
                lockTile.unlock();
            }
        }
        if ((movingDataBuffer != null) && (shaderMoving != null)) {
            if (!lockMoving.isLocked()) {
                lockMoving.lock();
                drawMoving();
                lockMoving.unlock();
            }
        }
        logFrame();
    }

    private void drawTiles() {
        shaderTile.use();
        /*
        GL.Disable(EnableCap.CullFace);
        GL.PolygonMode(MaterialFace.Front, PolygonMode.Fill);
        GL.PolygonMode(MaterialFace.Back, PolygonMode.Fill);
          Important !!!!!
        GL.Enable(EnableCap.Blend);
        GL.BlendFunc(BlendingFactor.SrcAlpha, BlendingFactor.OneMinusSrcAlpha);
        GL.ActiveTexture(TextureUnit.Texture0);
        */

        Game.getInstance().showMessage2("transX:" + translateX + " transY:" + translateY + " scale:" + currentScale);

        glBindVertexArray(vertexArrayObjectTile);

        Matrix.setIdentityM(transMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.translateM(transMatrix, 0, translateX, translateY, 0);
        Matrix.scaleM(scaleMatrix, 0, currentScale, currentScale, 0);
        Matrix.multiplyMM(mModelMatrix, 0, transMatrix, 0, scaleMatrix, 0);

        shaderTile.setMatrix4("projection", mProjectionMatrix);
        shaderTile.setMatrix4("model", mModelMatrix);
        shaderTile.setFloat("tileCountTexture0", textureBasis.tilesCount);
        shaderTile.setFloat("tileCountTexture1", textureMineral.tilesCount);
        shaderTile.setFloat("tileCountTexture2", textureEntity.tilesCount);
        shaderTile.setFloat("tileCountTexture3", textureFog.tilesCount);
        shaderTile.setInt("u_texture0", 0);
        shaderTile.setInt("u_texture1", 1);
        shaderTile.setInt("u_texture2", 2);
        shaderTile.setInt("u_texture3", 3);
        textureBasis.Use(GL_TEXTURE0);
        textureMineral.Use(GL_TEXTURE0 + 1);
        textureEntity.Use(GL_TEXTURE0 + 2);
        textureFog.Use(GL_TEXTURE0 + 3);
        //glDrawArrays(GL_TRIANGLES, 0, 6);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, tilesDataBuffer.capacity() / 4);
        glBindVertexArray(0);
    }

    private void drawMoving() {
        shaderMoving.use();
        glBindVertexArray(vertexArrayObjectMoving);

        Matrix.setIdentityM(transMatrix, 0);
        Matrix.setIdentityM(scaleMatrix, 0);
        Matrix.translateM(transMatrix, 0, translateX, translateY, 0);
        Matrix.scaleM(scaleMatrix, 0, currentScale, currentScale, 0);
        Matrix.multiplyMM(mModelMatrix, 0, transMatrix, 0, scaleMatrix, 0);

        shaderMoving.setMatrix4("projection", mProjectionMatrix);
        shaderMoving.setMatrix4("model", mModelMatrix);
        shaderMoving.setFloat("tileCountTexture", textureMoving.tilesCount);
        shaderMoving.setInt("u_texture", 4);
        textureMoving.Use(GL_TEXTURE0 + 4);
        glDrawArraysInstanced(GL_TRIANGLES, 0, 6, tilesDataBuffer.capacity() / 3);
        glBindVertexArray(0);
    }

    public void logFrame() {
        frames++;
        if (System.nanoTime() - startTime >= 1000000000) {
            fps = frames;
            //Game.getInstance().showMessage("fps:" + fps);
            frames = 0;
            startTime = System.nanoTime();
        }
    }

    private void prepareVerticesData() {
        /*
        float[] vertices = {
                // x y u v
                0, 0, 0, 1,
                SPRITE_WIDTH, 0, 1, 1,
                0, SPRITE_HEIGHT, 0, 0,
                SPRITE_WIDTH, SPRITE_HEIGHT, 1, 0,
                0, SPRITE_HEIGHT, 0, 0,
                SPRITE_WIDTH, 0, 1, 1
        };
        */
        float[] vertices = {
                // x y u v
                0, 0, 0, 0,
                SPRITE_WIDTH, 0, 1, 0,
                0, SPRITE_HEIGHT, 0, 1,
                SPRITE_WIDTH, SPRITE_HEIGHT, 1, 1,
                0, SPRITE_HEIGHT, 0, 1,
                SPRITE_WIDTH, 0, 1, 0
        };

        vertexDataBuffer = FloatBuffer.allocate(vertices.length);
        vertexDataBuffer.put(vertices);
        vertexDataBuffer.position(0);
    }

    public void prepareTileData(ShortBuffer buffer) {
        if (shaderTile == null) return;
        //this.mapWidth = widthMap;
        //this.mapHeight = heightMap;

        if (vertexDataBuffer == null) {
            prepareVerticesData();
        }

        if (tilesDataBuffer == null) {
            tilesDataBuffer = buffer;
            setupTileData();
            return;
        }

        lockTile.lock();
        if (tilesDataBuffer.capacity() != buffer.capacity()) {
            tilesDataBuffer = buffer;
            bindTileData(true);
        } else {
            tilesDataBuffer.position(0);
            tilesDataBuffer = buffer;
            bindTileData(false);
        }
        lockTile.unlock();
    }

    private void setupTileData() {
        int[] ids = new int[1];
        glGenBuffers(1, ids, 0);
        vertexBufferObjectInstancedTile = ids[0];
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile);
        tilesDataBuffer.position(0);
        glBufferData(GL_ARRAY_BUFFER, tilesDataBuffer.capacity() * SHORT_SIZE_BYTES, tilesDataBuffer, GL_STATIC_DRAW);

        glGenVertexArrays(1, ids, 0);
        vertexArrayObjectTile = ids[0];
        glBindVertexArray(vertexArrayObjectTile);
        glGenBuffers(1, ids, 0);
        int vertexBufferObjectTile = ids[0];
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectTile);
        vertexDataBuffer.position(0);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * FLOAT_SIZE_BYTES, vertexDataBuffer, GL_STATIC_DRAW);

        int vertexLocation = shaderTile.getAttribLocation("aPos");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 4 * FLOAT_SIZE_BYTES, 0);

        int texCoords = shaderTile.getAttribLocation("aTexCoords");
        glEnableVertexAttribArray(texCoords);
        glVertexAttribPointer(texCoords, 2, GL_FLOAT, false, 4 * FLOAT_SIZE_BYTES, 2 * FLOAT_SIZE_BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferObjectInstancedTile);
        int tilePositionX = shaderTile.getAttribLocation("aTileX");
        glEnableVertexAttribArray(tilePositionX);
        glVertexAttribPointer(tilePositionX, 1, GL_UNSIGNED_SHORT, false, 4 * SHORT_SIZE_BYTES, 0);

        int tilePositionY = shaderTile.getAttribLocation("aTileY");
        glEnableVertexAttribArray(tilePositionY);
        glVertexAttribPointer(tilePositionY, 1, GL_UNSIGNED_SHORT, false, 4 * SHORT_SIZE_BYTES, SHORT_SIZE_BYTES);

        int tileNumber = shaderTile.getAttribLocation("aTileNumber");
        glEnableVertexAttribArray(tileNumber);
        glVertexAttribPointer(tileNumber, 1, GL_UNSIGNED_SHORT, false, 4 * SHORT_SIZE_BYTES, 2 * SHORT_SIZE_BYTES);

        int textureNumber = shaderTile.getAttribLocation("aTextureNumber");
        glEnableVertexAttribArray(textureNumber);
        glVertexAttribPointer(textureNumber, 1, GL_UNSIGNED_SHORT, false, 4 * SHORT_SIZE_BYTES, 3 * SHORT_SIZE_BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glVertexAttribDivisor(tilePositionX, 1);
        glVertexAttribDivisor(tilePositionY, 1);
        glVertexAttribDivisor(tileNumber, 1);
        glVertexAttribDivisor(textureNumber, 1);

        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }


    private void bindTileData(boolean isCreateNewBuffers) {
        if (isCreateNewBuffers) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile);
            tilesDataBuffer.position(0);
            glBufferData(GL_ARRAY_BUFFER, tilesDataBuffer.capacity() * SHORT_SIZE_BYTES, tilesDataBuffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedTile);
            tilesDataBuffer.position(0);
            glBufferSubData(GL_ARRAY_BUFFER, 0, tilesDataBuffer.capacity() * SHORT_SIZE_BYTES, tilesDataBuffer);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }
    }

    public void prepareMovingData(FloatBuffer buffer) {
        if (shaderMoving == null) return;
        if (buffer == null) return;

        if (vertexDataBuffer == null) {
            prepareVerticesData();
        }
        if (movingDataBuffer == null) {
            movingDataBuffer = buffer;
            setupMovingData();
            return;
        }

        lockMoving.lock();
        if (movingDataBuffer.capacity() != buffer.capacity()) {
            movingDataBuffer = buffer;
            bindMovingData(true);
        } else {
            movingDataBuffer.position(0);
            movingDataBuffer = buffer;
            bindMovingData(false);
        }
        lockMoving.unlock();
    }

    private void setupMovingData() {
        int[] ids = new int[1];
        glGenBuffers(1, ids, 0);
        vertexBufferObjectInstancedMoving = ids[0];
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving);
        movingDataBuffer.position(0);
        glBufferData(GL_ARRAY_BUFFER, movingDataBuffer.capacity() * FLOAT_SIZE_BYTES, movingDataBuffer, GL_STATIC_DRAW);

        glGenVertexArrays(1, ids, 0);
        vertexArrayObjectMoving = ids[0];
        glBindVertexArray(vertexArrayObjectMoving);
        glGenBuffers(1, ids, 0);
        int vertexBufferObjectMoving = ids[0];
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectMoving);
        vertexDataBuffer.position(0);
        glBufferData(GL_ARRAY_BUFFER, vertexDataBuffer.capacity() * FLOAT_SIZE_BYTES, vertexDataBuffer, GL_STATIC_DRAW);

        int vertexLocation = shaderMoving.getAttribLocation("aPos");
        glEnableVertexAttribArray(vertexLocation);
        glVertexAttribPointer(vertexLocation, 2, GL_FLOAT, false, 4 * FLOAT_SIZE_BYTES, 0);

        int texCoords = shaderMoving.getAttribLocation("aTexCoords");
        glEnableVertexAttribArray(texCoords);
        glVertexAttribPointer(texCoords, 2, GL_FLOAT, false, 4 * FLOAT_SIZE_BYTES, 2 * FLOAT_SIZE_BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, this.vertexBufferObjectInstancedMoving);
        int tilePositionX = shaderMoving.getAttribLocation("aTileX");
        glEnableVertexAttribArray(tilePositionX);
        glVertexAttribPointer(tilePositionX, 1, GL_FLOAT, false, 3 * FLOAT_SIZE_BYTES, 0);

        int tilePositionY = shaderMoving.getAttribLocation("aTileY");
        glEnableVertexAttribArray(tilePositionY);
        glVertexAttribPointer(tilePositionY, 1, GL_FLOAT, false, 3 * FLOAT_SIZE_BYTES, FLOAT_SIZE_BYTES);

        int tileNumber = shaderMoving.getAttribLocation("aTileNumber");
        glEnableVertexAttribArray(tileNumber);
        glVertexAttribPointer(tileNumber, 1, GL_FLOAT, false, 3 * FLOAT_SIZE_BYTES, 2 * FLOAT_SIZE_BYTES);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glVertexAttribDivisor(tilePositionX, 1);
        glVertexAttribDivisor(tilePositionY, 1);
        glVertexAttribDivisor(tileNumber, 1);

        glEnableVertexAttribArray(0);
        glBindVertexArray(0);
    }


    private void bindMovingData(boolean isCreateNewBuffers) {
        if (isCreateNewBuffers) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving);
            movingDataBuffer.position(0);
            glBufferData(GL_ARRAY_BUFFER, movingDataBuffer.capacity() * FLOAT_SIZE_BYTES, movingDataBuffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectInstancedMoving);
            movingDataBuffer.position(0);
            glBufferSubData(GL_ARRAY_BUFFER, 0, movingDataBuffer.capacity() * FLOAT_SIZE_BYTES, movingDataBuffer);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
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

// Суперсовременный OpenGL https://habr.com/ru/articles/456932/