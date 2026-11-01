package com.feldelysium.designsystem.foundation.material.agsl

internal const val WORN_AGSL = """
uniform float seed;
uniform float invScale;
uniform float invMid1;
uniform float invMid2;
uniform float invFine;
uniform float wear;
layout(color) uniform half4 baseColor;
${NOISE_LIB_FAST}

half4 main(float2 p) {
    float2 sp = p + seed * 53.0;
    float big = fbm(sp * invScale);
    float t = 0.80 - 0.34 * wear;

    float hole = smoothstep(t, t + 0.03, big);
    if (hole >= 1.0) return half4(0.0);

    float2 q = sp * invMid1;
    float2 w = float2(fbm(q), fbm(q + 4.0)) - 0.5;
    float mid = fbm(sp * invMid2 + w * 1.5 + 7.0);

    hole += smoothstep(t + 0.10, t + 0.13, mid) * smoothstep(0.45, 0.6, big);
    hole += smoothstep(0.90 - 0.1 * wear, 0.93, noise(sp * invFine)) * 0.9;
    float keep = 1.0 - clamp(hole, 0.0, 1.0);

    float3 c = float3(baseColor.rgb) * (0.9 + 0.2 * mid);
    float a = float(baseColor.a) * keep;
    return half4(half3(c * a), half(a));
}
"""
