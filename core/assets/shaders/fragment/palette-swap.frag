#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform sampler2D u_palette;

varying vec4 v_color;
varying vec2 v_texCoord;

void main()
{
    vec4 tex_colors = texture2D(u_texture, v_texCoord);
    float palette_sample = v_color.r + .5/256.;
    vec2 palette_vec = vec2(tex_colors.r, palette_sample);

    vec4 color = texture2D(u_palette, palette_vec);

    gl_FragColor = vec4(color.rgb, tex_colors.a);
}