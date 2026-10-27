#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D MaskSampler;
uniform float Boost;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 bloom = texture(DiffuseSampler, texCoord).rgb;
    float mask = clamp(texture(MaskSampler, texCoord).a, 0.0, 1.0);
    // RGB already contains silhouette coverage. Add it once, without squaring the faint halo's alpha.
    // The full-resolution mask keeps the original item texture crisp.
    fragColor = vec4(bloom * Boost * (1.0 - mask), 0.0);
}
