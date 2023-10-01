uniform sampler2D u_texture;

varying vec2 TexCoords;
varying vec3 TextColor;

void main()
{
    gl_FragColor = vec4(TextColor, texture2D(u_texture, TexCoords).w) ;
    //gl_FragColor = vec4(u_textColor, 1.0); // show BlackBox
}
