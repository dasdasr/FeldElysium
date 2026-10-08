package com.example.feldelysium.designsystem.material.agsl

internal const val PAPER_AGSL = """
uniform float2 size;
uniform float seed;
uniform float time;
uniform float press;
uniform float wear;
uniform float rim;

uniform float edgeWear;
uniform float band;
uniform float chipScale;
uniform float chipCoverage;
uniform float innerWear;
uniform float maxLoss;
uniform float scuffTint;

uniform float mottleScale;
uniform float mottleStrength;
uniform float grainScale;
uniform float grainStrength;
uniform float colorGrain;
uniform float toothStrength;
uniform float speckleStrength;
uniform float edgeShade;
uniform float pressShade;

uniform float sinkBand;
uniform float sinkShade;

layout(color) uniform half4 baseColor;
layout(color) uniform half4 scuffColor;
${NOISE_LIB}
half4 main(float2 p) {
    float d = min(min(p.x, size.x - p.x), min(p.y, size.y - p.y)) - rim;
    float edge = 1.0 - smoothstep(0.0, band, d);

    float2 sp = p + seed * 37.0;
    float mottle = fbm(sp / mottleScale);
    float blotch = fbm(sp / chipScale);
    float2 gp = sp / grainScale;
    float grain = noise(gp + time * 31.7);
    float3 cg = float3(grain, noise(gp + 41.0), noise(gp + 83.0)) - 0.5;
    float tooth = fbm(sp / (grainScale * 5.0));
    float speck = step(0.992, hash(floor(sp / (grainScale * 3.0)) + seed));

    float lo = 0.70 - 0.40 * chipCoverage - press * 0.05;
    float chip = smoothstep(lo, lo + 0.28, blotch * 1.14 + (grain - 0.5) * 0.15);
    float loss = clamp(pow(edge, 1.5) * edgeWear * wear * chip * 1.5, 0.0, maxLoss);

    float inner = wear * innerWear * smoothstep(0.62, 0.85, blotch);
    float scuff = clamp(inner + loss * 0.8, 0.0, 1.0);

    float3 rgb = float3(baseColor.rgb);
    rgb *= 1.0 - edgeShade * edge;
        float sink = 1.0 - smoothstep(0.0, sinkBand, d);
    float vy = p.y / size.y;
    rgb *= 1.0 - sinkShade * (1.0 + press * 0.8) * sink * (1.25 - vy); 
    rgb += sinkShade * 0.35 * sink * smoothstep(0.55, 1.0, vy);
    rgb += (mottle - 0.4) * mottleStrength * 0.2;
    rgb = mix(rgb, float3(scuffColor.rgb), scuff * scuffTint);
    rgb += (grain - 0.5) * grainStrength;
    rgb += cg * colorGrain;
    rgb += (tooth - 0.5) * toothStrength;
    rgb += speck * speckleStrength;
    rgb *= 1.0 - pressShade * press;

    float a = float(baseColor.a) * (1.0 - loss);
    return half4(half3(rgb * a), half(a));
}
"""

internal const val INK_AGSL = """
uniform float seed;
uniform float grainScale;
uniform float minAlpha;
uniform float contrast;
layout(color) uniform half4 inkColor;
${NOISE_LIB}
half4 main(float2 p) {
    float2 sp = p + seed * 53.0;
    float n = noise(sp / grainScale);
    n = clamp((n - 0.5) * contrast + 0.5, 0.0, 1.0);
    float a = mix(minAlpha, 1.0, n) * float(inkColor.a);
    return half4(half3(float3(inkColor.rgb) * a), half(a));
}
"""
