#version 300 es
#define SIZE 5

uniform vec2 surface;
uniform float pulseStates[SIZE];
uniform float balanceStates[SIZE];
uniform float parameters[3];
uniform float time;

void main(){
    int vertexId = int(gl_VertexID);
    switch(vertexId){
        case 0:
        gl_Position = vec4(-1.0, 1.0, 0.0, 1.0);
        break;
        case 1:
        gl_Position = vec4(1.0, 1.0, 0.0, 1.0);
        break;
        case 2:
        gl_Position = vec4(1.0, -1.0, 0.0, 1.0);
        break;
        case 3:
        gl_Position = vec4(-1.0, -1.0, 0.0, 1.0);
        break;
    }
}