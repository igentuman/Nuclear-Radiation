#version 150

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// Flat silhouette shaped by the item sprite's alpha. Texture rgb is discarded — only the glow
// color (vertex color, forced per item) is written, so the blur produces a uniform aura. Depth
// testing is handled by the render type (LEQUAL against the world depth copied into the target),
// so items occluded by blocks never reach this shader.
void main() {
    float a = texture(Sampler0, texCoord0).a;
    if (a < 0.1) discard;
    fragColor = vec4(vertexColor.rgb, 1.0);
}
