#version 300 es
#define SIZE 6
#define SIZE_FLOAT 6.0

precision highp float;

uniform vec2 surface;
uniform float balanceStates[SIZE];
uniform float time;

out vec4 fragmentColor;

struct State {
    float state;
    float average;
};

float thick = 0.005;

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
        result.state += retrieved * smoothstep(s, 0.0, abs(mapped - floatIndex));
        result.average = (result.average + retrieved) * 0.5;
    }
    return result;
}

void main(){
    vec2 point = (gl_FragCoord.xy - 0.5 * surface.xy) / surface.x * 2.0;
    vec2 polar = vec2(length(point), atan(point.y, point.x) / 3.14 * 0.5 + 0.5);
    point.y = 0.2 / polar.x;
    fragmentColor = vec4(0.0);
    State state = getState(polar.y - time * 0.02, 1.0, balanceStates);
    if (mod(abs(point.y - state.state * 0.4 + time * 0.1), 0.1) < thick * 0.8) {
        fragmentColor += mix(vec4(0.0), vec4(0.337, 0.341, 0.251, 0.5), (1.0 - clamp(point.y, 0.0, 1.0)) * 0.6 + 0.4 * state.state) * state.state;
    }
}