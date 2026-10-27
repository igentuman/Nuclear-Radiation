#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
in vec2 texCoord;
out vec4 fragColor;

// Normalized, symmetric downsample kernel. Bilinear filtering covers the gaps between taps.
// Each pass halves the dimensions, so a wide halo no longer needs a wide per-pixel loop.
void main() {
    vec2 step = 1.0 / InSize;
    vec4 sum = texture(DiffuseSampler, texCoord) * 4.0;
    sum += texture(DiffuseSampler, texCoord + vec2(-step.x, -step.y));
    sum += texture(DiffuseSampler, texCoord + vec2( step.x, -step.y));
    sum += texture(DiffuseSampler, texCoord + vec2(-step.x,  step.y));
    sum += texture(DiffuseSampler, texCoord + vec2( step.x,  step.y));
    fragColor = sum * 0.125;
}
