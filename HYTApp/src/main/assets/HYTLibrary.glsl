#version 300 es

precision highp float;

uniform vec2 surface;
uniform float pulseStates[6];
uniform float time;

out vec4 fragmentColor;

float thick = 0.01;

vec4 palette(float hue, float saturation){
    float red = 1.0 - smoothstep(0.25, 0.5, hue) + smoothstep(0.5, 0.75, hue);
    float green = smoothstep(0.0, 0.25, hue) - smoothstep(0.5, 0.75, hue);
    float blue = smoothstep(0.25, 0.5, hue) - smoothstep(0.75, 1.0, hue);
    return vec4(vec3(red, green, blue) * saturation + 1.0 - saturation, 1.0);
}

vec2 getState(float xx, float s, float states[6]){
    float actual = 0.0;
    float average = 0.0;
    float mapped = xx * 5.0;
    for (int index = 0; index < 6; index++){
        float retrieved = states[index];
        float floatIndex = float(index);
        actual += retrieved * (
        smoothstep((floatIndex - s), floatIndex, mapped) - smoothstep(floatIndex, (floatIndex + s), mapped)
        );
        average = (average + abs(states[index])) * 0.5;
    }
    return vec2(actual, average);
}

void main(){
    vec2 point = (gl_FragCoord.xy - 0.5 * surface.xy) / surface.x * 2.0;
    fragmentColor = vec4(0.0);
    vec2 state = getState((-point.x + 1.0) / 2.0, 1.0, pulseStates);
    float modifier = state.x * (-point.y + 0.8) * 0.4;
    if (mod(abs(point.y - point.x * 0.1 - state.x * (-point.x + 1.0) / 2.0), 0.2 + (-point.x + 1.0) / 2.0) < thick * 0.8 && modifier > 0.0) {
        fragmentColor += vec4(0.337, 0.341, 0.251, 0.5) * modifier;
    }
}