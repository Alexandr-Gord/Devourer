uniform sampler2D u_texture[2];

varying vec2 TexCoords;
varying float TextureNumber;

void main()
{
    int number = int(TextureNumber);
    gl_FragColor = texture2D(u_texture[number], TexCoords);
}