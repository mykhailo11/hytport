#version 300 es
#define SIZE 6
#define SIZE_FLOAT 6.0

precision highp float;

uniform vec2 surface;
uniform float pulseStates[SIZE];
uniform float balanceStates[SIZE];
uniform float parameters[3];
uniform float time;

out vec4 fragmentColor;

float thick = 0.01;

vec3 source = vec3(10.0, 4.0, 1.0);

struct State {
    float value;
    float average;
};

vec4 palette(float hue, float saturation){
    float range = mod(hue, 1.0) * 0.6;
    float red = clamp(2.0 * smoothstep(0.1, 0.3, abs(range - 0.3)), 0.0, 1.0);
    float green = clamp(2.0 * smoothstep(0.2, 0.0, abs(range - 0.2)), 0.0, 1.0);
    float blue = clamp(2.0 * smoothstep(0.2, 0.0, abs(range - 0.4)), 0.0, 1.0);
    return vec4(vec3(red, green, blue) * saturation + 1.0 - saturation, 1.0);
}

State getState(float xx, float s, float states[SIZE]){
    float mapped = mod(xx, 1.0) * SIZE_FLOAT;
    State result = State(0.0, 0.0);
    int range = int(ceil(s)) + 1;
    for (int index = -range; index <= SIZE + range; index++) {
        float floatIndex = float(index);
        int correctIndex = int(mod(floatIndex, SIZE_FLOAT));
        if (correctIndex < 0) {
            correctIndex = SIZE + index;
        }
        float retrieved = states[correctIndex];
        result.value += retrieved * smoothstep(s, 0.0, abs(mapped - floatIndex));
        result.average = (result.average + retrieved) * 0.5;
    }
    return result;
}

void main(){

    vec2 point = gl_FragCoord.xy - 0.5 * surface.xy;
    point /=  surface.x < surface.y ? surface.x : surface.y;
    point += vec2(sin(time * 0.1) * 0.2, cos(time * 0.1) * 0.2);
    float base = surface.x > surface.y ? point.x : point.y;
    vec2 polar = vec2(length(point), atan(point.y, point.x) / 6.28 + 0.5);
    float tunnel = (0.06) / polar.x;
    State state = getState(polar.y - tunnel * 0.1 + time * 0.01, 1.0, balanceStates);
    State additional = getState(polar.y - tunnel * 0.1 + time * 0.01, 1.0, pulseStates);
    float initial = state.value;
    state.value *= (1.0 - clamp(tunnel, 0.0, 1.0));
    float average = state.average;
    vec4 color = palette(parameters[2] + 0.1 * sin(time * 0.1) + clamp(tunnel, 0.0, 2.0) * 0.1 + time * 0.01, 1.0);
    fragmentColor = mix(palette(time * 0.01, 1.0), vec4(0.0), 1.0 - clamp((0.04 + initial * average * 0.04) / polar.x, 0.0, 1.0)) * (0.2 + 0.8 * average);
    fragmentColor += mix(color * (state.value * 0.8 + 0.2), fragmentColor, clamp(tunnel, 0.0, 1.0)) * 0.5;
    fragmentColor +=  color * clamp(((state.value * 0.8 + 0.2 * (1.0 - clamp(tunnel, 0.0, 1.0))) * thick) / (abs(mod(tunnel * 2.0 + time * 0.1 - state.value * 0.5, 0.1) - 0.05) + 0.01), 0.0, 1.0 + average) * 0.8;
    fragmentColor +=  palette(parameters[2] - 0.2 + 0.1 * sin(time * 0.1) + clamp(tunnel, 0.0, 1.0) * 0.1 + time * 0.01, 1.0) * clamp((0.03 * (1.0 - tunnel)) / (abs(mod(tunnel * 0.5 + polar.y * 0.2 + time * 0.05 + additional.value * 0.2, 0.2) - 0.1) + 0.01), 0.0, 1.0) * state.value;
}