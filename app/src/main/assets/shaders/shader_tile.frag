uniform sampler2D u_texture0;
uniform sampler2D u_texture1;
uniform sampler2D u_texture2;
uniform sampler2D u_texture3;

varying vec2 TexCoords;
varying float TextureNumber;

void main()
{
    int number = int(TextureNumber);
    if (number == 0) {
        gl_FragColor = texture2D(u_texture0, TexCoords);
    } else if (number == 1) {
        gl_FragColor = texture2D(u_texture1, TexCoords);
    } else if (number == 2) {
        gl_FragColor = texture2D(u_texture2, TexCoords);
    } else if (number == 3) {
        gl_FragColor = texture2D(u_texture3, TexCoords);
    }
}
