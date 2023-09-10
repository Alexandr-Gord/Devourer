layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute float aTileX;
layout (location = 3) attribute float aTileY;
layout (location = 4) attribute float aTileNumber;
layout (location = 5) attribute float aTextureNumber;

uniform mat4 model;
uniform mat4 projection;
uniform float tileCountTexture[2];

varying vec2 TexCoords;
varying float TextureNumber;

void main()
{
    int number = int(aTextureNumber);
    vec2 spriteSize = vec2(0.0, 0.0);
    vec2 offset = vec2(0.0, 0.0);

    if (number == 0) {
        spriteSize.x = 149.0;
        spriteSize.y = 129.0;
        offset.x = aTileX;
        offset.y = aTileY;
    } else if (number == 1) {
        spriteSize.x = 335.0;
        spriteSize.y = 387.0;
        offset.x = aTileX - 93.0; // (335.0 - 149.0) * 0.5f;
        offset.y = aTileY - 129.0; // (387.0 - 129.0) * 0.5f;
    }
    TexCoords = aTexCoords;
    TexCoords.y = (TexCoords.y + aTileNumber) / tileCountTexture[number];
    //TexCoords.y = (1.0 - TexCoords.y - aTileNumber) / tileCountTexture;
    //vec2 offset = vec2(aTileX, aTileY);
    gl_Position = projection * model * vec4(aPos * spriteSize + offset, 0.0, 1.0);
    TextureNumber = aTextureNumber;
}
