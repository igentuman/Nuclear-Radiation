#version 150

uniform float Intensity;      // area exposure rate -> white-noise dots
uniform float DoseIntensity;  // accumulated dose -> sickness (bloom/tint/vignette)
uniform float RandSeed;
uniform float Time;
uniform vec2 ScreenSize;

out vec4 fragColor;

// integer hash, magnitude-independent (no float-precision tiling at 4K)
uint uhash(uvec2 p, uint seed) {
    uint h = seed;
    h ^= p.x * 374761393u;
    h ^= p.y * 668265263u;
    h = (h ^ (h >> 13u)) * 1274126177u;
    h ^= h >> 16u;
    return h;
}

void main() {
    if (Intensity <= 0.0 && DoseIntensity <= 0.0) {
        fragColor = vec4(0.0);
        return;
    }

    vec2 uv = gl_FragCoord.xy / ScreenSize;
    vec2 centered = (uv - 0.5) * 2.0;

    // ---- Effect 1: sickness (DoseIntensity) ----
    float dose = clamp(DoseIntensity, 0.0, 1.0);

    // green tint, ramps with dose
    vec3 tintColor = mix(vec3(0.45, 0.85, 0.28), vec3(0.18, 0.62, 0.10), dose);
    float tintAlpha = dose * 0.22;

    // vignette, dark green closing in at high dose
    float vignette = smoothstep(0.40, 1.30, length(centered));
    vec3 vigColor = vec3(0.02, 0.10, 0.0);
    float vigAlpha = vignette * dose * 0.60;

    // faux bloom + highlight lift: bright green-white wash, gentle pulse
    float pulse = 0.75 + 0.25 * sin(Time * 2.1);
    float radial = 1.0 - smoothstep(0.0, 1.2, length(centered));
    vec3 bloomColor = vec3(0.65, 1.0, 0.55);
    float bloomAlpha = dose * 0.20 * pulse * (0.55 + 0.45 * radial);

    float sickAlpha = clamp(tintAlpha + vigAlpha + bloomAlpha, 0.0, 0.9);
    vec3 sickColor = vec3(0.0);
    if (sickAlpha > 0.001) {
        sickColor = (tintColor * tintAlpha + vigColor * vigAlpha + bloomColor * bloomAlpha) / sickAlpha;
    }

    vec4 outColor = vec4(sickColor, sickAlpha);

    // ---- Effect 2: white-noise dots (Intensity) ----
    // opaque white dots, count scales with Intensity; reseeded each frame -> flicker
    const float DOT_SIZE = 4.0; // dot edge in pixels (tunable 2-4)
    float density = clamp(Intensity, 0.0, 1.0) * 0.002;
    uvec2 cell = uvec2(floor(gl_FragCoord.xy / DOT_SIZE));
    uint seed = uint(RandSeed * 4294967296.0);
    float n = float(uhash(cell, seed)) * (1.0 / 4294967296.0);
    if (n > 1.0 - density) {
        outColor = vec4(1.0, 1.0, 1.0, 0.9);
    }

    fragColor = outColor;
}
