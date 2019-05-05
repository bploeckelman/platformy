/**
 Star layers - simple parallax
 based on : https://www.shadertoy.com/view/MltXz7 by gigatron
*/
#ifdef GL_ES
precision highp float;
#endif

#define dir 1    // define direction 0,1,2,3  up/down/left/right
#define speed 0.4

uniform float u_time;
uniform vec2 u_resolution;

varying vec4 v_color;
varying vec3 v_position;
varying vec2 v_texCoord;

vec2 mod289(vec2 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 mod289(vec3 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec4 mod289(vec4 x) {
    return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
    return mod289(((x*34.0)+1.0)*x);
}

vec4 permute(vec4 x) {
  return mod((34.0 * x + 1.0) * x, 289.0);
}

vec4 taylorInvSqrt(vec4 r) {
    return 1.79284291400159 - 0.85373472095314 * r;
}

float snoise(vec2 v)
{
    const vec4 C = vec4(0.211324865405187,0.366025403784439,-0.577350269189626,0.024390243902439);
    vec2 i  = floor(v + dot(v, C.yy) );
    vec2 x0 = v -   i + dot(i, C.xx);

    vec2 i1;
    i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    i = mod289(i); // Avoid truncation effects in permutation
    vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
        + i.x + vec3(0.0, i1.x, 1.0 ));

    vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
    m = m*m ;
    m = m*m ;

    vec3 x = 2.0 * fract(p * C.www) - 1.0;
    vec3 h = abs(x) - 0.5;
    vec3 ox = floor(x + 0.5);
    vec3 a0 = x - ox;

    m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

    vec3 g;
    g.x  = a0.x  * x0.x  + h.x  * x0.y;
    g.yz = a0.yz * x12.xz + h.yz * x12.yw;

    return 130.0 * dot(m, g);
}

float cellular2x2(vec2 P)
{
    #define K 0.142857142857 // 1/7
    #define K2 0.0714285714285 // K/2
    #define jitter 0.8 // jitter 1.0 makes F1 wrong more often

    vec2 Pi = mod(floor(P), 289.0);
    vec2 Pf = fract(P);
    vec4 Pfx = Pf.x + vec4(-0.5, -1.5, -0.5, -1.5);
    vec4 Pfy = Pf.y + vec4(-0.5, -0.5, -1.5, -1.5);
    vec4 p = permute(Pi.x + vec4(0.0, 1.0, 0.0, 1.0));
    p = permute(p + Pi.y + vec4(0.0, 0.0, 1.0, 1.0));
    vec4 ox = mod(p, 7.0)*K+K2;
    vec4 oy = mod(floor(p*K),7.0)*K+K2;
    vec4 dx = Pfx + jitter*ox;
    vec4 dy = Pfy + jitter*oy;
    vec4 d = dx * dx + dy * dy; // d11, d12, d21 and d22, squared
    // Sort out the two smallest distances

    // Cheat and pick only F1
    d.xy = min(d.xy, d.zw);
    d.x = min(d.x, d.y);
    return d.x; // F1 duplicated, F2 not computed
}


void main() {
    vec2 uv = v_texCoord;
    uv.x *= (u_resolution.x / u_resolution.y);

    vec2 GA = vec2(0);

#if dir == 0
    GA.y += u_time*speed;
#elif dir == 1
    GA.y -= u_time*speed;
#elif dir == 2
    GA.x += u_time*speed;
#elif dir == 3
    GA.x -= u_time*speed;
#endif

    float F1,F2,F3,F4,F5,F6,F7,F8;
    float N1,N2,N3,N4,N5,N6,N7,N8;
    float A,A1,A2,A3,A4,A5,A6,A7,A8;
    F1=F2=F3=F4=F5=F6=F7=F8=0.0;
    N1=N2=N3=N4=N5=N6=N7=N8=0.0;
    A=A1=A2=A3=A4=A5=A6=A7=A8=0.0;

	// Snow layers, somewhat like an fbm with worley layers.
	F1 = 1.0-cellular2x2((uv+(GA*0.1))*8.0);
	A1 = 1.0-(A*0.8);
	N1 = smoothstep(0.9998,1.0,F1)*0.2*A1;

	F2 = 1.0-cellular2x2((uv+(GA*0.2))*7.0);
	A2 = 1.0-(A*0.8);
	N2 = smoothstep(0.9998,1.0,F2)*0.3*A2;

	F3 = 1.0-cellular2x2((uv+(GA*0.3))*6.0);
	A3 = 1.0-(A*0.8);
	N3 = smoothstep(0.9998,1.0,F3)*0.4*A3;

    F4 = 1.0-cellular2x2((uv+(GA*0.4))*5.0);
	A4 = 1.0-(A*0.8);
	N4 = smoothstep(0.9998,1.0,F4)*0.5*A4;

    F5 = 1.0-cellular2x2((uv+(GA*0.5))*4.0);
	A5 = 1.0-(A*0.8);
	N5 = smoothstep(0.9998,1.0,F5)*0.6*A5;

    F6 = 1.0-cellular2x2((uv+(GA*0.8))*3.0);
	A6 = 1.0-(A*0.8);
	N6 = smoothstep(0.9999,1.0,F6)*0.59*A6;

    F7 = 1.0-cellular2x2((uv+(GA*1.2))*2.9);
	A7 = 1.0-(A*0.8);
	N7 = smoothstep(0.9999,1.0,F7)*0.58*A7;

    F8 = 1.0-cellular2x2((uv+(GA*1.8))*2.8);
	A8 = 1.0-(A*0.8);
	N8 = smoothstep(0.9999,1.0,F8)*0.57*A8;

	float cl= N8+N7+N6+N5+N4+N3+N2+N1;

	//cl = N1+N2+N3+N4+N5;
//	fragColor = vec4(cl, cl, cl, 1.0)*2.;
    gl_FragColor = vec4(cl, cl, cl, 1.0) * 2.0 * v_color;
}
