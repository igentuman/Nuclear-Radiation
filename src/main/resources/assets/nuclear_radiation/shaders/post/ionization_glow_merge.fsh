#version 330

uniform sampler2D OutlineSampler;
uniform sampler2D GlowSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 outline = texture(OutlineSampler, texCoord);
    vec3 bloom = texture(GlowSampler, texCoord).rgb;
    // ONE / ONE_MINUS_SRC_ALPHA blits premultiplied ordinary outlines and adds the aura.
    fragColor = vec4(outline.rgb * outline.a + bloom, outline.a);
}
