#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;

in vec2 texCoord;
in vec2 sampleStep;

uniform float Radius;
uniform float Boost;

out vec4 fragColor;

// Final blur pass + outer-glow masking. Same separable triangular blur as ionization_glow_blur,
// then multiplies by (1 - silhouette) so the item-covered region drops to zero (real item texture
// stays visible) and only the soft outer aura composites back. Multiply, not subtract: the blur
// passes compound Boost so alpha exceeds 1 inside the silhouette and a subtract would not cancel it.
void main() {
    vec4 sum = vec4(0.0);
    float total = 0.0;
    for (float a = -Radius + 0.5; a <= Radius; a += 2.0) {
        float w = 1.0 - abs(a) / (Radius + 1.0);
        sum += texture(DiffuseSampler, texCoord + sampleStep * a) * w;
        total += w;
    }
    vec4 blurred = (sum / total) * Boost;
    float mask = clamp(texture(MaskSampler, texCoord).a, 0.0, 1.0);
    fragColor = blurred * (1.0 - mask);
}
