#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 sampleStep;

uniform float Radius;
uniform float Boost;

out vec4 fragColor;

// Wide separable blur. Relies on GL_LINEAR (use_linear_filter) + step 2 to halve sample count.
// Triangular weights for a softer-than-box falloff. Boost brightens the final pass into a visible aura.
void main() {
    vec4 sum = vec4(0.0);
    float total = 0.0;
    for (float a = -Radius + 0.5; a <= Radius; a += 2.0) {
        float w = 1.0 - abs(a) / (Radius + 1.0);
        sum += texture(DiffuseSampler, texCoord + sampleStep * a) * w;
        total += w;
    }
    fragColor = (sum / total) * Boost;
}
