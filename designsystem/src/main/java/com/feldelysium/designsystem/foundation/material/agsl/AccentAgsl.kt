package com.feldelysium.designsystem.foundation.material.agsl

internal const val ACCENT_AGSL = """
uniform float seed;
uniform float invScale;
uniform float invScale2;
uniform float invGrain;
uniform float invGrain3;
uniform float fade;
uniform float glow;
uniform float blotch;
uniform float grain;
layout(color) uniform half4 baseColor;
${NOISE_LIB_FAST}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0;
    float3 c = float3(baseColor.rgb);

    float n = fbm(sp * invScale);
    float lo = (1.0 - smoothstep(0.30, 0.50, n)) * fade;
    if (lo > 0.0) {
        float gray = dot(c, float3(0.299, 0.587, 0.114));
        c = mix(c, (c * 0.55 + gray * 0.45) * 0.88, lo);
    }
    if (glow > 0.0) {
        float hi = smoothstep(0.52, 0.72, fbm(sp * invScale2 + 9.0)) * glow;
        if (hi > 0.0) c = mix(c, min(c * 1.2 + 0.05, float3(1.0)), hi);
    }
    if (blotch != 0.0) c += (n - 0.45) * blotch;
    c += (noise(sp * invGrain) - 0.5) * grain;

    float2 sc = sp * invGrain3;
    float2 gi = floor(sc);
    if (hash(gi) >= 0.975) {
        float rad = 0.15 + 0.2 * hash(gi + 3.1);
        float spk = 1.0 - smoothstep(rad * 0.6, rad, length(fract(sc) - 0.5));
        c *= 1.0 - spk * 0.35;
    }

    float a = float(baseColor.a);
    return half4(half3(clamp(c, 0.0, 1.0) * a), half(a));
}
"""
