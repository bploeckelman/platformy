#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec4 v_color;
varying vec2 v_texCoord;

void main()
{
    vec4 tex_colors = texture2D(u_texture, v_texCoord);

    float border = tex_colors.r;
    vec4 color = vec4(0);
    float barAmount = step(v_color.a, tex_colors.g);
    color.rgb = mix(v_color.rgb, color.rgb, barAmount);
    color = mix(color, vec4(0), tex_colors.r);

    gl_FragColor = vec4(color.rgb, tex_colors.a);

}