/**
 Star Nest - https://www.shadertoy.com/view/XlfGRj

    Created by Kali in 2013-06-17

    3D kaliset fractal - volumetric rendering and some tricks.
    I put the params on top to play with. Mouse enabled to explore different regions.
     It should work fine on full screen with a decent gpu
 **/
#ifdef GL_ES
// TODO: mediump isn't enough resolution on mobile, add settings for fanciness of background?
//precision mediump float;
precision highp float;
#endif

#define iterations 17
#define formuparam 0.53

#define volsteps 20
#define stepsize 0.1

#define zoom   0.800
#define tile   0.850
#define speed  0.010

#define brightness 0.0015
#define darkmatter 0.300
#define distfading 0.730
#define saturation 0.850

uniform float u_time;
uniform vec2 u_resolution;

varying vec4 v_color;
varying vec3 v_position;
varying vec2 v_texCoord;

void main() {
    vec2 res = u_resolution;
    vec2 fragCoord = vec2(v_texCoord.x, 1.0 - v_texCoord.y) * res.xy;
    vec2 uv = 2.0 * (fragCoord.xy - res.xy / 2.0) / min(res.x, res.y);
    vec3 dir = vec3(uv * zoom, 1.0);
    float time = u_time * speed + 0.25;

//    float a1 = 0.5 + mouse.x / resolution.x * 2.0;
//    float a2 = 0.8 + mouse.y / resolution.y * 2.0;
//    mat2 rot1 = mat2(cos(a1), sin(a1), -sin(a1), cos(a1));
//    mat2 rot2 = mat2(cos(a2), sin(a2), -sin(a2), cos(a2));
//    dir.xz *= rot1;
//    dir.xy *= rot2;
    vec3 from = vec3(1.0, 1.5, 0.5);
    from += vec3(0.0, time * 1.5, -2.0);
//    from.xz *= rot1;
//    from.xy *= rot2;

    float s = 0.1;
    float fade = 1.0;
    vec3 v = vec3(0.0);

    for (int r = 0; r < volsteps; r++) {
        vec3 p = from + s * dir * 0.5;
        p = abs(vec3(tile) - mod(p, vec3(tile * 2.0))); // tiling fold

        float pa = 0.0;
        float a = 0.0;
        for (int i = 0; i < iterations; i++) {
            p = abs(p) / dot(p, p) - formuparam; // the magic formula
            a += abs(length(p) - pa); // absolute sum of average change
            pa = length(p);
        }

        float dm = max(0.0, darkmatter - a * a * 0.001); //dark matter
        a *= a * a; // add contrast
        if (r > 6) fade *= 1.0 - dm; // dark matter, don't render near
//        v += vec3(dm, dm * 0.5, 0.0);
        v += fade;
        v += vec3(s, s * s, s * s * s * s) * a * brightness * fade; // coloring based on distance
        fade *= distfading;  // distance fading
        s += stepsize;
    }

    v = mix(vec3(length(v)), v, saturation); // color adjust

    gl_FragColor = vec4(v * 0.01, 1.0) * v_color;
}