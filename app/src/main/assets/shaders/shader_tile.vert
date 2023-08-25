layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute float aTileX;
layout (location = 3) attribute float aTileY;
layout (location = 4) attribute float aTileNumber;
layout (location = 5) attribute float aTextureNumber;

uniform mat4 model;
uniform mat4 projection;
//uniform float mapHeight;
uniform float tileCountTexture0;
uniform float tileCountTexture1;
uniform float tileCountTexture2;
uniform float tileCountTexture3;

varying vec2 TexCoords;
varying float TextureNumber;

vec2 CorrectTexCoords(vec2 texCoords, float scale) {
    vec2 center = vec2(0.5);
    //return texCoords * scale + center * (1.0 - scale);
    return (texCoords - center) * scale + center;
}

void main()
{
    int number = int(aTextureNumber);
    if (number == 0) {
        TexCoords = CorrectTexCoords(aTexCoords, 0.98) * vec2(1.0, 1.0 / tileCountTexture0) + vec2(0.0, aTileNumber / tileCountTexture0);
    } else if (number == 1) {
        TexCoords = aTexCoords * vec2(1.0, 1.0 / tileCountTexture1) + vec2(0.0, aTileNumber / tileCountTexture1);
    } else if (number == 2) {
        TexCoords = aTexCoords * vec2(1.0, 1.0 / tileCountTexture2) + vec2(0.0, aTileNumber / tileCountTexture2);
    } else if (number == 3) {
        TexCoords = CorrectTexCoords(aTexCoords, 0.99) * vec2(1.0, 1.0 / tileCountTexture3) + vec2(0.0, aTileNumber / tileCountTexture3);
    }

    float offsetY = 0.0;
    float offsetX = aTileX * 111.75; //149.0 * 0.75;
    if (mod(aTileX, 2.0) < 0.0001) {
        offsetY = aTileY * 129.0;
    } else {
        offsetY = (aTileY + 0.5f) * 129.0;
    }
    vec2 offset = vec2(offsetX, offsetY);
    gl_Position = projection * model * vec4(aPos + offset, 0.0, 1.0);
    TextureNumber = aTextureNumber;
}
