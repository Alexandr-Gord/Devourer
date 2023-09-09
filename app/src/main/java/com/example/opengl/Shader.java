package com.example.opengl;

import static android.opengl.GLES20.glUniform1fv;
import static android.opengl.GLES20.glUniform1iv;
import static android.opengl.GLES30.GL_ACTIVE_ATTRIBUTES;
import static android.opengl.GLES30.GL_ACTIVE_UNIFORMS;
import static android.opengl.GLES30.GL_COMPILE_STATUS;
import static android.opengl.GLES30.GL_FRAGMENT_SHADER;
import static android.opengl.GLES30.GL_LINK_STATUS;
import static android.opengl.GLES30.GL_TRUE;
import static android.opengl.GLES30.GL_VERTEX_SHADER;
import static android.opengl.GLES30.glAttachShader;
import static android.opengl.GLES30.glCompileShader;
import static android.opengl.GLES30.glCreateProgram;
import static android.opengl.GLES30.glCreateShader;
import static android.opengl.GLES30.glDeleteShader;
import static android.opengl.GLES30.glDetachShader;
import static android.opengl.GLES30.glGetActiveAttrib;
import static android.opengl.GLES30.glGetActiveUniform;
import static android.opengl.GLES30.glGetAttribLocation;
import static android.opengl.GLES30.glGetProgramInfoLog;
import static android.opengl.GLES30.glGetProgramiv;
import static android.opengl.GLES30.glGetShaderInfoLog;
import static android.opengl.GLES30.glGetShaderiv;
import static android.opengl.GLES30.glGetUniformLocation;
import static android.opengl.GLES30.glLinkProgram;
import static android.opengl.GLES30.glShaderSource;
import static android.opengl.GLES30.glUniform1f;
import static android.opengl.GLES30.glUniform1i;
import static android.opengl.GLES30.glUniform3fv;
import static android.opengl.GLES30.glUniformMatrix4fv;
import static android.opengl.GLES30.glUseProgram;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class Shader {
    public final int Handle;
    private final Map<String, Integer> uniformLocations = new HashMap<>();
    private final Map<String, Integer> attribLocations = new HashMap<>();

    public Shader(Context context, String vertexShaderFilename, String fragmentShaderFilename) throws IOException {
        String shaderSource = readTextFromAssets(context, vertexShaderFilename);
        if (shaderSource.equals(""))
            throw new IOException("Shader file " + vertexShaderFilename + " not valid");

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, shaderSource);
        compileShader(context, vertexShader);

        shaderSource = readTextFromAssets(context, fragmentShaderFilename);
        if (shaderSource.equals(""))
            throw new IOException("Shader file " + fragmentShaderFilename + " not valid");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, shaderSource);
        compileShader(context, fragmentShader);

        Handle = glCreateProgram();

        glAttachShader(Handle, vertexShader);
        glAttachShader(Handle, fragmentShader);

        linkProgram(context, Handle);

        glDetachShader(Handle, vertexShader);
        glDetachShader(Handle, fragmentShader);
        glDeleteShader(fragmentShader);
        glDeleteShader(vertexShader);

        IntBuffer params = IntBuffer.allocate(1);
        IntBuffer type = IntBuffer.allocate(1);
        glGetProgramiv(Handle, GL_ACTIVE_UNIFORMS, params);
        int numberOfUniforms = params.get(0);

        for (int i = 0; i < numberOfUniforms; i++) {
            String key = glGetActiveUniform(Handle, i, params, type);
            int location = glGetUniformLocation(Handle, key);
            uniformLocations.put(key, location);
        }

        glGetProgramiv(Handle, GL_ACTIVE_ATTRIBUTES, params);
        int numberOfAttributes = params.get(0);
        for (int i = 0; i < numberOfAttributes; i++) {
            String key = glGetActiveAttrib(Handle, i, params, type);
            int location = glGetAttribLocation(Handle, key);
            attribLocations.put(key, location);
        }
    }

    private static void compileShader(Context context, int shader) {
        glCompileShader(shader);

        // Check for compilation errors
        IntBuffer params = IntBuffer.allocate(1);
        glGetShaderiv(shader, GL_COMPILE_STATUS, params);
        if (params.get(0) != GL_TRUE) {
            String infoLog = glGetShaderInfoLog(shader);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Error occurred whilst compiling Shader(" + shader + ").\n\n" + infoLog, Toast.LENGTH_LONG).show();
                }
            });
            //throw new RuntimeException("Error occurred whilst compiling Shader(" + shader + ").\n\n" + infoLog);
        }
    }

    private static void linkProgram(Context context, int program) {
        glLinkProgram(program);

        // Check for linking errors
        IntBuffer params = IntBuffer.allocate(1);
        glGetProgramiv(program, GL_LINK_STATUS, params);
        if (params.get(0) != GL_TRUE) {
            String infoLog = glGetProgramInfoLog(program);
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Error occurred whilst linking Program(" + program + ").\n\n" + infoLog, Toast.LENGTH_LONG).show();
                }
            });
            //throw new RuntimeException("Error occurred whilst linking Program(" + program + ").\n\n" + infoLog);
        }
    }

    public void use() {
        glUseProgram(Handle);
    }

    public int getAttribLocation(String attribName) {
        //return GL.GetAttribLocation(Handle, attribName);
        Integer result = attribLocations.get(attribName);
        return result != null ? result : 0;
    }

    public int getUniformLocation(String attribName) {
        Integer result = uniformLocations.get(attribName);
        return result != null ? result : 0;
    }

    // Uniform setters
    public void setInt(String name, int data) {
        glUseProgram(Handle);
        glUniform1i(getUniformLocation(name), data);
    }

    public void setIntArray(String name, int[] data) {
        glUseProgram(Handle);
        glUniform1iv(getUniformLocation(name), data.length, data,0);
    }

    public void setFloat(String name, float data) {
        glUseProgram(Handle);
        glUniform1f(getUniformLocation(name), data);
    }

    public void setFloatArray(String name, float[] data) {
        glUseProgram(Handle);
        glUniform1fv(getUniformLocation(name), data.length, data, 0);
    }

    public void setMatrix4(String name, float[] data) {
        glUseProgram(Handle);
        glUniformMatrix4fv(getUniformLocation(name), 1, false, data, 0); // false - not transpose matrix
    }

    public void setVector3(String name, float[] data) {
        glUseProgram(Handle);
        glUniform3fv(getUniformLocation(name), 1, data, 0);
    }

    public static String readTextFromRaw(Context context, int resourceId) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = context.getResources().openRawResource(resourceId);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\r\n");
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException | Resources.NotFoundException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static String readTextFromAssets(Context context, String filepath) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = context.getAssets().open(filepath);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\r\n");
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Error occurred whilst loading Shader(" + filepath + ").\n\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        return stringBuilder.toString();
    }
}
