#version 330

uniform sampler2D OutlineSampler;
uniform sampler2D GlowSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 outline = texture(OutlineSampler, texCoord);
    vec4 glow = texture(GlowSampler, texCoord);
    float radioactiveOutline = step(outline.g * 2.0, outline.r) * step(outline.g * 2.0, outline.b);
    outline.a *= 1.0 - radioactiveOutline;
    float alpha = max(outline.a, glow.a);
    vec3 color = (outline.rgb * outline.a + glow.rgb * glow.a) / max(alpha, 0.0001);
    fragColor = vec4(color, alpha);
}
