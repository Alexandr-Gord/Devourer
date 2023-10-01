package com.example.opengl

import android.content.Context
import android.content.res.Resources.NotFoundException
import android.opengl.GLES20
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.IntBuffer

class Shader(context: Context, vertexShaderFilename: String, fragmentShaderFilename: String) {
    private val handle: Int
    private val uniformLocations: MutableMap<String, Int> = HashMap()
    private val attribLocations: MutableMap<String, Int> = HashMap()

    init {
        var shaderSource = readTextFromAssets(context, vertexShaderFilename)
        if (shaderSource == "") throw IOException("Shader file $vertexShaderFilename not valid")
        val vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(vertexShader, shaderSource)
        compileShader(context, vertexShader)
        shaderSource = readTextFromAssets(context, fragmentShaderFilename)
        if (shaderSource == "") throw IOException("Shader file $fragmentShaderFilename not valid")
        val fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(fragmentShader, shaderSource)
        compileShader(context, fragmentShader)
        handle = GLES20.glCreateProgram()
        GLES20.glAttachShader(handle, vertexShader)
        GLES20.glAttachShader(handle, fragmentShader)
        linkProgram(context, handle)
        GLES20.glDetachShader(handle, vertexShader)
        GLES20.glDetachShader(handle, fragmentShader)
        GLES20.glDeleteShader(fragmentShader)
        GLES20.glDeleteShader(vertexShader)
        val params = IntBuffer.allocate(1)
        val type = IntBuffer.allocate(1)
        GLES20.glGetProgramiv(handle, GLES20.GL_ACTIVE_UNIFORMS, params)
        val numberOfUniforms = params[0]
        for (i in 0 until numberOfUniforms) {
            val key = GLES20.glGetActiveUniform(handle, i, params, type)
            val location = GLES20.glGetUniformLocation(handle, key)
            uniformLocations[key] = location
        }
        GLES20.glGetProgramiv(handle, GLES20.GL_ACTIVE_ATTRIBUTES, params)
        val numberOfAttributes = params[0]
        for (i in 0 until numberOfAttributes) {
            val key = GLES20.glGetActiveAttrib(handle, i, params, type)
            val location = GLES20.glGetAttribLocation(handle, key)
            attribLocations[key] = location
        }
    }

    fun use() {
        GLES20.glUseProgram(handle)
    }

    fun getAttribLocation(attribName: String): Int {
        //return GL.GetAttribLocation(Handle, attribName);
        val result = attribLocations[attribName]
        return result ?: 0
    }

    fun getUniformLocation(attribName: String): Int {
        val result = uniformLocations[attribName]
        return result ?: 0
    }

    // Uniform setters
    fun setInt(name: String, data: Int) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform1i(getUniformLocation(name), data)
    }

    fun setIntArray(name: String, data: IntArray) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform1iv(getUniformLocation(name), data.size, data, 0)
    }

    fun setFloat(name: String, data: Float) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform1f(getUniformLocation(name), data)
    }

    fun setFloatArray(name: String, data: FloatArray) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform1fv(getUniformLocation(name), data.size, data, 0)
    }

    fun setMatrix4(name: String, data: FloatArray?) {
        GLES20.glUseProgram(handle)
        GLES20.glUniformMatrix4fv(
            getUniformLocation(name),
            1,
            false,
            data,
            0
        ) // false - not transpose matrix
    }

    fun setVector3(name: String, data: FloatArray?) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform3fv(getUniformLocation(name), 1, data, 0)
    }

    fun setVector3(name: String, x: Float, y: Float, z: Float) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform3f(getUniformLocation(name), x, y, z)
    }

    fun setVector2(name: String, data: FloatArray?) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform2fv(getUniformLocation(name), 1, data, 0)
    }

    fun setVector2(name: String, x: Float, y: Float) {
        GLES20.glUseProgram(handle)
        GLES20.glUniform2f(getUniformLocation(name), x, y)
    }

    companion object {
        private fun compileShader(context: Context, shader: Int) {
            GLES20.glCompileShader(shader)

            // Check for compilation errors
            val params = IntBuffer.allocate(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, params)
            if (params[0] != GLES20.GL_TRUE) {
                val infoLog = GLES20.glGetShaderInfoLog(shader)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Error occurred whilst compiling Shader($shader).\n\n$infoLog",
                        Toast.LENGTH_LONG
                    ).show()
                }
                //throw new RuntimeException("Error occurred whilst compiling Shader(" + shader + ").\n\n" + infoLog);
            }
        }

        private fun linkProgram(context: Context, program: Int) {
            GLES20.glLinkProgram(program)

            // Check for linking errors
            val params = IntBuffer.allocate(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, params)
            if (params[0] != GLES20.GL_TRUE) {
                val infoLog = GLES20.glGetProgramInfoLog(program)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context,
                        "Error occurred whilst linking Program($program).\n\n$infoLog",
                        Toast.LENGTH_LONG
                    ).show()
                }
                //throw new RuntimeException("Error occurred whilst linking Program(" + program + ").\n\n" + infoLog);
            }
        }

        fun readTextFromRaw(context: Context, resourceId: Int): String {
            val stringBuilder = StringBuilder()
            try {
                var bufferedReader: BufferedReader? = null
                try {
                    val inputStream = context.resources.openRawResource(resourceId)
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\r\n")
                    }
                } finally {
                    bufferedReader?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: NotFoundException) {
                e.printStackTrace()
            }
            return stringBuilder.toString()
        }

        fun readTextFromAssets(context: Context, filepath: String): String {
            val stringBuilder = StringBuilder()
            try {
                var bufferedReader: BufferedReader? = null
                try {
                    val inputStream = context.assets.open(filepath)
                    bufferedReader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (bufferedReader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                        stringBuilder.append("\r\n")
                    }
                } finally {
                    bufferedReader?.close()
                }
            } catch (e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        context, """
     Error occurred whilst loading Shader($filepath).    
     ${e.message}
     """.trimIndent(), Toast.LENGTH_LONG
                    ).show()
                }
            }
            return stringBuilder.toString()
        }
    }
}