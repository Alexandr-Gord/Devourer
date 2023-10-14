layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute vec2 aCharPos;
layout (location = 3) attribute vec2 aCharSize;
layout (location = 4) attribute vec2 aCharPosInTexture;
layout (location = 5) attribute vec3 aTextColor;
layout (location = 6) attribute vec2 aTextStartPos;
layout (location = 7) attribute vec2 aScale;
layout (location = 8) attribute float aType;

uniform mat4 projection;
uniform vec2 sizeFontsTexture;
uniform vec2 sizeImagesTexture;

varying vec2 TexCoords;
varying float Type;
varying vec3 TextColor;

void main()
{
    int type = int(aType);
    Type = aType;
    if (type == 1) { // letters
                     TexCoords = aCharPosInTexture + aTexCoords * aCharSize / sizeFontsTexture;
                     TextColor = aTextColor;
                     gl_Position = projection * vec4((aPos * aCharSize + aCharPos) * aScale + aTextStartPos, 0.0, 1.0);
    } else if (type == 2) { // label background images
                            TexCoords = aCharPosInTexture + aTexCoords * aCharSize / sizeImagesTexture;
                            TextColor = aTextColor;
                            gl_Position = projection * vec4(aPos * aCharSize * aScale + aCharPos, 0.0, 1.0);
    }
}

