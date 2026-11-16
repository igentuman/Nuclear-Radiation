#version 330

#moj_import <nuclear_radiation:ionization_bloom.glsl>

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 source = texture(InSampler, texCoord);
    // Remove markers before edge detection, preserving ordinary (including purple) outlines.
    fragColor = isIonizationSource(source) ? vec4(0.0) : source;
}
