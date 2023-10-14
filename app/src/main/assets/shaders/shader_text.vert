layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute vec2 aCharPos;
layout (location = 3) attribute vec2 aCharSize;
layout (location = 4) attribute vec2 aCharPosInTexture;
layout (location = 5) attribute vec3 aTextColor;
layout (location = 6) attribute vec2 aTextStartPos;
layout (location = 7) attribute float aScale;

//uniform mat4 model;
uniform mat4 projection;
uniform vec2 sizeTexture;

varying vec2 TexCoords;
varying vec3 TextColor;

void main()
{
    TexCoords = aCharPosInTexture + aTexCoords * aCharSize / sizeTexture;
    TextColor = aTextColor;
    //gl_Position = projection * model * vec4((aPos * aCharSize + aCharPos) * aScale + aTextStartPos, 0.0, 1.0);
    gl_Position = projection * vec4((aPos * aCharSize + aCharPos) * aScale + aTextStartPos, 0.0, 1.0);
}
