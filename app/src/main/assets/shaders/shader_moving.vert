layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute float aTileX;
layout (location = 3) attribute float aTileY;
layout (location = 4) attribute float aTileNumber;

uniform mat4 model;
uniform mat4 projection;
uniform float tileCountTexture;

varying vec2 TexCoords;

void main()
{
    TexCoords = aTexCoords * vec2(1.0, 1.0 / tileCountTexture) + vec2(0.0, aTileNumber / tileCountTexture);
    vec2 offset = vec2(aTileX, aTileY);
    gl_Position = projection * model * vec4(aPos + offset, 0.0, 1.0);
}
