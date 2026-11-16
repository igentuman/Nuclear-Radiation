#version 330

#moj_import <nuclear_radiation:ionization_bloom.glsl>

layout(std140) uniform GlowConfig {
    float Boost;
};

uniform sampler2D GlowSampler;
uniform sampler2D MaskSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 bloom = sampleBloom(GlowSampler, texCoord) * Boost;
    float mask = texture(MaskSampler, texCoord).a > 0.0 ? 1.0 : 0.0;
    // Coverage is already in RGB. Add it once; leave the original item texture crisp.
    fragColor = vec4(bloom * (1.0 - mask), 0.0);
}
