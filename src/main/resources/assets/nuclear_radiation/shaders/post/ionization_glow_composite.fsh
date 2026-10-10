#version 330

uniform sampler2D GlowSampler;
uniform sampler2D MaskSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 glow = texture(GlowSampler, texCoord);
    float mask = texture(MaskSampler, texCoord).a;
    float alpha = clamp(glow.a * 7.0 * (1.0 - mask), 0.0, 0.55);
    fragColor = vec4(0.24, 0.82, 0.55, alpha);
}
