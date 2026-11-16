#version 330

#moj_import <nuclear_radiation:ionization_bloom.glsl>

uniform sampler2D InSampler;
uniform sampler2D ItemDepthSampler;
uniform sampler2D SceneDepthSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 source = texture(InSampler, texCoord);
    float itemDepth = texture(ItemDepthSampler, texCoord).r;
    float sceneDepth = texture(SceneDepthSampler, texCoord).r;
    float visible = isIonizationSource(source) && itemDepth <= sceneDepth + 0.0001 ? source.a : 0.0;
    vec3 hue = source.r < 0.5
        ? mix(vec3(0.4, 1.0, 0.2), vec3(0.2, 0.8, 1.0), source.r * 2.0)
        : mix(vec3(0.2, 0.8, 1.0), vec3(1.0), source.r * 2.0 - 1.0);
    fragColor = encodeBloom(hue * visible);
}
