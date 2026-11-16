#version 330

#moj_import <nuclear_radiation:ionization_bloom.glsl>

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
    vec2 DetailSize;
};

uniform sampler2D InSampler;
uniform sampler2D DetailSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 stepSize = 0.5 / InSize;
    vec3 sum = sampleBloom(InSampler, texCoord + vec2(-2.0 * stepSize.x, 0.0));
    sum += sampleBloom(InSampler, texCoord + vec2(-stepSize.x,  stepSize.y)) * 2.0;
    sum += sampleBloom(InSampler, texCoord + vec2(0.0,  2.0 * stepSize.y));
    sum += sampleBloom(InSampler, texCoord + vec2( stepSize.x,  stepSize.y)) * 2.0;
    sum += sampleBloom(InSampler, texCoord + vec2(2.0 * stepSize.x, 0.0));
    sum += sampleBloom(InSampler, texCoord + vec2( stepSize.x, -stepSize.y)) * 2.0;
    sum += sampleBloom(InSampler, texCoord + vec2(0.0, -2.0 * stepSize.y));
    sum += sampleBloom(InSampler, texCoord + vec2(-stepSize.x, -stepSize.y)) * 2.0;
    fragColor = encodeBloom(mix(sampleBloom(DetailSampler, texCoord), sum / 12.0, 0.75));
}
