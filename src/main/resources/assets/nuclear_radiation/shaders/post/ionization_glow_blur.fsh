#version 330

#moj_import <nuclear_radiation:ionization_bloom.glsl>

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

// Five fixed taps on successively halved color targets replace the wide full-screen loops.
void main() {
    vec2 stepSize = 1.0 / InSize;
    vec3 sum = sampleBloom(InSampler, texCoord) * 4.0;
    sum += sampleBloom(InSampler, texCoord + vec2(-stepSize.x, -stepSize.y));
    sum += sampleBloom(InSampler, texCoord + vec2( stepSize.x, -stepSize.y));
    sum += sampleBloom(InSampler, texCoord + vec2(-stepSize.x,  stepSize.y));
    sum += sampleBloom(InSampler, texCoord + vec2( stepSize.x,  stepSize.y));
    fragColor = encodeBloom(sum * 0.125);
}
