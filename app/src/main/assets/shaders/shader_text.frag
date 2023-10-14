uniform sampler2D u_textureFonts;
uniform sampler2D u_textureImages;

varying vec2 TexCoords;
varying vec3 TextColor;
varying float Type;

void main()
{
    int type = int(Type);
    if (type == 1) {
        gl_FragColor = vec4(TextColor, texture2D(u_textureFonts, TexCoords).w);
        //gl_FragColor = vec4(u_textColor, 1.0); // show BlackBox
    } else if (type == 2) {
        gl_FragColor = texture2D(u_textureImages, TexCoords);
    }
}
