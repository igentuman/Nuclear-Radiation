#version 330

uniform sampler2D InSampler;
uniform sampler2D ItemDepthSampler;
uniform sampler2D SceneDepthSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 source = texture(InSampler, texCoord);
    bool radioactive = source.r > 0.95 && source.g < 0.1 && source.b > 0.95;
    float itemDepth = texture(ItemDepthSampler, texCoord).r;
    float sceneDepth = texture(SceneDepthSampler, texCoord).r;
    float visible = radioactive && itemDepth <= sceneDepth + 0.0001 ? source.a : 0.0;
    fragColor = vec4(visible);
}
