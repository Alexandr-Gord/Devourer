uniform sampler2D u_texture;

varying vec2 TexCoords;

void main()
{
    gl_FragColor = texture2D(u_texture, TexCoords);
}