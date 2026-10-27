#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DetailSampler;
uniform vec2 InSize;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 step = 0.5 / InSize;
    vec4 sum = texture(DiffuseSampler, texCoord + vec2(-2.0 * step.x, 0.0));
    sum += texture(DiffuseSampler, texCoord + vec2(-step.x,  step.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0,  2.0 * step.y));
    sum += texture(DiffuseSampler, texCoord + vec2( step.x,  step.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(2.0 * step.x, 0.0));
    sum += texture(DiffuseSampler, texCoord + vec2( step.x, -step.y)) * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0, -2.0 * step.y));
    sum += texture(DiffuseSampler, texCoord + vec2(-step.x, -step.y)) * 2.0;
    // Retain a soft inner halo as well as the broad outer aura. Weights preserve energy and hue.
    fragColor = mix(texture(DetailSampler, texCoord), sum / 12.0, 0.75);
}
