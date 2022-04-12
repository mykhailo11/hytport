#version 300 es

precision highp float;

uniform vec2 surface;
uniform float pulseStates[6];
uniform float balanceStates[6];
uniform float parameters;
uniform float time;

out vec4 fragmentColor;

float thick = 0.01;
float smoothness = 0.8;

mat2 getRotation(float angle) {
    float sinAngle = sin(angle);
    float cosAngle = cos(angle);

    return mat2(
    cosAngle, -sinAngle,
    sinAngle, cosAngle
    );
}


vec4 getPalette(float saturation){
    float red = 1.0 - smoothstep(0.25, 0.5, saturation) + smoothstep(0.5, 0.75, saturation);
    float green = smoothstep(0.0, 0.25, saturation) - smoothstep(0.5, 0.75, saturation);
    float blue = smoothstep(0.25, 0.5, saturation) - smoothstep(0.75, 1.0, saturation);
    return vec4(red,green, blue, 1.0);
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
    vec2 point = (gl_FragCoord.xy - 0.5 * surface.xy) * 2.0;
    point /=  surface.x < surface.y ? surface.x : surface.y;
    vec2 wave = getRotation(time * 0.1) * point;
    vec2 computed = getState(((wave.x + 1.0) * 0.5 * sin(time - wave.x) + point.y * cos(time + wave.x) + 2.0) / 4.0, smoothness, balanceStates);
    float average = computed.y;
    computed = getState(((wave.x - computed.x) * sin(-time + average * 5.0 + wave.y * sin(time * 0.5)) + wave.y * cos(-time + average + wave.y + 0.5) * sin(time * 0.5) + 2.0) / 4.0, smoothness + 0.1 * computed.x, pulseStates) - computed * 0.8;
    float cummulativeState = computed.x;
    vec2 fraction = vec2(mod(time + point.x + point.y, 16.0) / 16.0, mod(-time + 8.0 * computed.x * length(vec2(cummulativeState, point.x)), 40.0) / 40.0);
    vec4 color = getPalette(fraction.y);
    fragmentColor = getPalette(0.6 + 0.2 * (point.x * point.y + 1.0) * 0.5) * 0.2 * (-(point.y) + 1.0) * (0.4 + average * 0.6) * (1.0 - parameters);
    fragmentColor += color * (abs(cummulativeState) * 0.4 + (average + computed.y) * 0.3) * parameters * (point.x + point.y + 4.0) / 2.0;
    color = getPalette(fraction.x) * (0.2 + 0.8 * abs(cummulativeState));
    if (mod(sin(sin(point.x + point.y + time * 0.1) * 3.14 * 2.0) * 0.1 / (0.5 + average) + point.y * 0.4 - cummulativeState, 0.4) < (cummulativeState + 2.0)  * thick){
        fragmentColor += (color * (0.8 + 0.2 * cummulativeState));
    }
    if (mod(point.x + point.y * sin(-average) - 0.2 + point.x * 0.2 - cummulativeState, 0.2 * average + 0.2) < (cummulativeState + 2.0) * thick){
        fragmentColor += vec4(0.1) * parameters;
    }
}