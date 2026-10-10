#version 330

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 stepSize = BlurDir / InSize;
    vec4 sum = vec4(0.0);
    float weightSum = 0.0;
    for (float offset = -Radius; offset <= Radius; offset += 2.0) {
        float weight = 1.0 - abs(offset) / (Radius + 1.0);
        sum += texture(InSampler, texCoord + stepSize * offset) * weight;
        weightSum += weight;
    }
    fragColor = sum / weightSum;
}
