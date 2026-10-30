package com.feldelysium.designsystem.foundation.material.agsl

internal const val INK_AGSL = """
uniform float seed;
uniform float grainScale;
uniform float minAlpha;
uniform float contrast;
layout(color) uniform half4 inkColor;
${NOISE_LIB}
half4 main(float2 p) {
    float2 sp = p + seed * 53.0;
    float wearAmt = 1.0 - minAlpha;
    float2 w = float2(fbm(sp / (grainScale * 1.6)),
                      fbm(sp / (grainScale * 1.6) + 9.0)) - 0.5;
    float big = fbm(sp / grainScale + w * 1.2);
    float t = 0.72 - 0.30 * wearAmt;
    float edgeW = 0.10 / max(contrast, 0.5);
    float keep = 1.0 - smoothstep(t, t + edgeW, big);

    float3 c = float3(inkColor.rgb) * (0.92 + 0.12 * noise(sp / (grainScale * 0.6)));
    float a = keep * float(inkColor.a);
    return half4(half3(c * a), half(a));
}
"""
