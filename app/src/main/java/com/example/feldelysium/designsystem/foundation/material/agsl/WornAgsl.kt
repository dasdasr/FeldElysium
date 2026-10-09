package com.example.feldelysium.designsystem.foundation.material.agsl

internal const val WORN_AGSL = """
uniform float seed;
uniform float scale;
uniform float wear;
layout(color) uniform half4 baseColor;
${NOISE_LIB}

half4 main(float2 p) {
    float2 sp = p + seed * 53.0;
    float big = fbm(sp / scale);
    float2 w = float2(fbm(sp / (scale * 0.35)), fbm(sp / (scale * 0.35) + 4.0)) - 0.5;
    float mid = fbm(sp / (scale * 0.18) + w * 1.5 + 7.0);
    float fine = noise(sp / (scale * 0.06));

    float t = 0.80 - 0.34 * wear;
    float hole = smoothstep(t, t + 0.03, big)
               + smoothstep(t + 0.10, t + 0.13, mid) * smoothstep(0.45, 0.6, big);
    hole += smoothstep(0.90 - 0.1 * wear, 0.93, fine) * 0.9;
    float keep = 1.0 - clamp(hole, 0.0, 1.0);

    float3 c = float3(baseColor.rgb);
    c *= 0.9 + 0.2 * mid;
    float a = float(baseColor.a) * keep;
    return half4(half3(c * a), half(a));
}
"""
