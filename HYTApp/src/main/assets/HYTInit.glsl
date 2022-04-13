#version 300 es

precision highp float;

uniform float time;
uniform vec2 surface;

out vec4 fragmentColor;

float thick = 0.01;

vec4 palette(float hue, float saturation){
    float red = 1.0 - smoothstep(0.25, 0.5, hue) + smoothstep(0.5, 0.75, hue);
    float green = smoothstep(0.0, 0.25, hue) - smoothstep(0.5, 0.75, hue);
    float blue = smoothstep(0.25, 0.5, hue) - smoothstep(0.75, 1.0, hue);
    return vec4(vec3(red, green, blue) * saturation + 1.0 - saturation, 0.0);
}

void main(){
    fragmentColor = vec4(0.0);
    vec2 point = (gl_FragCoord.xy - 0.5 * surface.xy) * 2.0;
    point /=  surface.x > surface.y ? surface.x : surface.y;
    vec4 color = palette(0.25 + 0.2 * (point.x + 1.0) * 0.5, 1.0) * (point.y + 1.5) * 0.5;
    float deformation = (sin(point.y) - cos(point.y + point.x + time) + 4.0) / 3.0 * (point.x * 2.0 + 2.0) * 0.5;
    vec2 pattern = vec2(mod(point.x, 0.2 * deformation), mod(point.y, 0.2 * deformation));
    if (length(pattern - vec2(0.1)) < 0.005 * deformation) {
        fragmentColor += color * 0.2;
    }
    fragmentColor += palette(0.5 + 0.2 * (1.0 + point.x) * 0.5, 1.0) * 0.05 * length(point - 0.5);
}