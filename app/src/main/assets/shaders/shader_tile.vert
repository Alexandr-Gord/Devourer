layout (location = 0) attribute vec2 aPos;
layout (location = 1) attribute vec2 aTexCoords;
layout (location = 2) attribute float aTileX;
layout (location = 3) attribute float aTileY;
layout (location = 4) attribute float aTileNumber;
layout (location = 5) attribute float aTextureNumber;

uniform mat4 model;
uniform mat4 projection;
//uniform float mapHeight;
uniform float tileCountTexture[4];

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
    if (number == 0) { // base
                       //TexCoords = CorrectTexCoords(aTexCoords, 0.98) * vec2(1.0, 1.0 / tileCountTexture[0]) + vec2(0.0, aTileNumber / tileCountTexture[0]);
                       //TexCoords = CorrectTexCoords(aTexCoords, 0.98);
                       TexCoords = aTexCoords;
                       TexCoords.y = (TexCoords.y + aTileNumber) / tileCountTexture[0];
    } else if (number == 1) { // minerals
                              //TexCoords = a TexCoords * vec2(1.0, 1.0 / tileCountTexture[1]) + vec2(0.0, aTileNumber / tileCountTexture[1]);
                              TexCoords = aTexCoords;
                              TexCoords.y = (1.0 - TexCoords.y + aTileNumber) / tileCountTexture[1];
    } else if (number == 2) { // devourer
                              //TexCoords = aTexCoords * vec2(1.0, 1.0 / tileCountTexture[2]) + vec2(0.0, aTileNumber / tileCountTexture[2]);
                              TexCoords = aTexCoords;
                              TexCoords.y = (TexCoords.y + aTileNumber) / tileCountTexture[2];
    } else if (number == 3) { // fog
                              //TexCoords = CorrectTexCoords(aTexCoords, 0.99) * vec2(1.0, 1.0 / tileCountTexture[3]) + vec2(0.0, aTileNumber / tileCountTexture[3]);
                              TexCoords = CorrectTexCoords(aTexCoords, 0.993);
                              TexCoords.y = (TexCoords.y + aTileNumber) / tileCountTexture[3];
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
