#version 300 es
#define SIZE 5
#define SIZE_FLOAT 5.0

precision highp float;

uniform vec2 surface;
uniform float pulseStates[SIZE];
uniform float balanceStates[SIZE];
uniform float parameters[3];
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
    vec2 point = gl_FragCoord.xy - 0.5 * surface.xy;
    point /=  surface.x < surface.y ? surface.x : surface.y;
    vec3 source = vec3(0.0, 0.0, 3.0);
    point *= 1.5;
    source += vec3(sin(time * 0.1), cos(time * 0.1), 0.0) * source.z;
    float base = surface.x > surface.y ? point.x : point.y;
    State alpha = getState((point.x - point.y + 2.0) / 4.0 - time * 0.1, 1.2, pulseStates);
    State beta = getState((point.x + cos(point.y) + 2.0) / 4.0 - time * 0.2, 1.2, balanceStates);
    float verticalDisplacementAlpha = sin(beta.state + point.x * 3.0 + time * 0.3);
    verticalDisplacementAlpha *= cos(point.y + beta.state * 2.0 - time * 0.4);
    float horizontalDisplacementAlpha = cos(point.y * 3.14 - beta.state + time * 0.5);
    horizontalDisplacementAlpha *= sin(beta.state + point.y * 3.14 + time * 0.2);
    float verticalDisplacementBeta = sin(point.x * 3.0 + alpha.state - time * 0.4);
    verticalDisplacementBeta *= cos(-point.y * 2.0 + time * 0.3);
    float horizontalDisplacementBeta = sin(point.y * 3.0 + time * 0.2);
    horizontalDisplacementBeta *= sin(alpha.state + point.x * 2.0 - time * 0.4);
    horizontalDisplacementAlpha *= sin(time * 0.1);
    horizontalDisplacementBeta *= cos(-time * 0.2);
    alpha.state *= (1.0 + point.x * 0.2 + (verticalDisplacementAlpha + horizontalDisplacementAlpha));
    alpha.state *= 0.5;
    beta.state *= (1.0 + point.y * 0.2 + (verticalDisplacementBeta + horizontalDisplacementBeta));
    beta.state *= 0.5;
    float colorAlphaFraction = (parameters[2] * 0.8 + point.y * 0.1 + 0.1);
    float colorBetaFraction = (-point.x * 0.5 + 0.5) * 0.2 + time * 0.02;
    float mixer = 0.5 + 0.25 * (alpha.state - beta.state);
    vec4 color = mix(
        palette(colorAlphaFraction, 1.0),
        palette(colorBetaFraction, 1.0),
        mixer
    );
    alpha.state = max(alpha.state * 1.5, beta.state) + alpha.average * 0.4;
    vec3 normal = normalize(vec3(point.xy, alpha.state));
    vec3 ray = source - vec3(point.xy, 0.0);
    float light = pow(clamp(dot(normalize(ray), normal), 0.1 * alpha.average, 1.0), 0.2);
    fragmentColor = color * light / (1.0 - clamp(beta.state - alpha.state, 0.0, 0.5)) * parameters[0];
    fragmentColor *= alpha.state * 0.8 + alpha.average * 0.2 * horizontalDisplacementAlpha * verticalDisplacementBeta;
    float strips = (thick * clamp(alpha.state, 0.0, 0.5));
    strips /= abs(mod(point.x - point.y - alpha.state - time * 0.1, 0.8) - 0.4);
    strips = clamp(strips, 0.0, 1.0);
    fragmentColor += color * strips * pow(clamp(light, 0.2, 1.0), 1.0 - 0.5 * alpha.average) * alpha.state;
}