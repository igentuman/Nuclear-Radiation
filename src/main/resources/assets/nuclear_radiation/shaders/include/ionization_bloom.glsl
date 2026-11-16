// RGBE keeps faint pyramid levels in the native API's RGBA8 render targets.
vec4 encodeBloom(vec3 color) {
    float peak = max(color.r, max(color.g, color.b));
    if (peak < exp2(-120.0)) return vec4(0.0);
    float exponent = ceil(log2(peak));
    return vec4(color * exp2(-exponent), (exponent + 128.0) / 255.0);
}

vec3 decodeBloom(vec4 encoded) {
    return encoded.rgb * exp2(round(encoded.a * 255.0) - 128.0);
}

// Packed exponents cannot be interpolated. Decode each texel before bilinear filtering.
vec3 sampleBloom(sampler2D image, vec2 uv) {
    ivec2 size = textureSize(image, 0);
    vec2 pixel = uv * vec2(size) - 0.5;
    ivec2 base = ivec2(floor(pixel));
    vec2 weight = fract(pixel);
    ivec2 limit = size - 1;
    vec3 a = decodeBloom(texelFetch(image, clamp(base, ivec2(0), limit), 0));
    vec3 b = decodeBloom(texelFetch(image, clamp(base + ivec2(1, 0), ivec2(0), limit), 0));
    vec3 c = decodeBloom(texelFetch(image, clamp(base + ivec2(0, 1), ivec2(0), limit), 0));
    vec3 d = decodeBloom(texelFetch(image, clamp(base + ivec2(1, 1), ivec2(0), limit), 0));
    return mix(mix(a, b, weight.x), mix(c, d, weight.x), weight.y);
}

bool isIonizationSource(vec4 source) {
    return abs(source.g - 1.0 / 255.0) < 0.5 / 255.0
        && abs(source.b - 254.0 / 255.0) < 0.5 / 255.0;
}
