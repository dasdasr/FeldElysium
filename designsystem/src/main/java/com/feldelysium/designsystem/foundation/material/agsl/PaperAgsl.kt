package com.feldelysium.designsystem.foundation.material.agsl

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
    float d = min(
        min(p.x, size.x - p.x),
        min(p.y, size.y - p.y)
    ) - rim;

    float edge = 1.0 - smoothstep(0.0, band, d);
    float vy = p.y / size.y;
    float2 sp = p + seed * 37.0;

    float mottle = 0.42;
    if (mottleStrength > 0.0) mottle = fbm(sp / mottleScale);

    float2 gp = sp / grainScale;
    float g1 = noise(gp + time * 31.7);
    float g2 = noise(gp * 0.47 + 17.0);
    float grain = g1 * 0.6 + g2 * 0.4;

    float fiber = 0.5;
    float tooth = 0.5;
    if (toothStrength > 0.0) {
        fiber =
            noise(float2(sp.x / (grainScale * 9.0), sp.y / (grainScale * 0.9))) * 0.5
          + noise(float2((sp.x + sp.y * 0.4) / (grainScale * 7.0),
                         (sp.y - sp.x * 0.4) / (grainScale * 0.8))) * 0.5;
        tooth = noise(sp / (grainScale * 5.0));
    }

    float spk = 0.0;
    if (speckleStrength > 0.0) {
        float2 sc = sp / (grainScale * 3.0);
        float2 gi = floor(sc);
        float srad = 0.12 + 0.2 * hash(gi + 3.1);
        spk = step(0.985, hash(gi + seed))
            * (1.0 - smoothstep(srad * 0.5, srad, length(fract(sc) - 0.5)));
    }

    float blotch = 0.0;
    if (wear * innerWear > 0.0) blotch = fbm(sp / chipScale);

    float loss = 0.0;
    if (edge > 0.0 && edgeWear > 0.0 && wear > 0.0 && maxLoss > 0.0) {
        float2 cw = float2(
            fbm(sp / (chipScale * 2.5) + 3.1),
            fbm(sp / (chipScale * 2.5) + 11.3)
        ) - 0.5;
        float warpedBlotch = fbm(sp / chipScale + cw * 1.4);
        float ridge = 1.0 - abs(noise(sp / (chipScale * 0.55) + cw * 2.0) * 2.0 - 1.0);
        float chipField = warpedBlotch + (ridge - 0.5) * 0.18;
        float lo = 0.70 - 0.40 * chipCoverage - press * 0.05;
        float chip = smoothstep(lo, lo + 0.22, chipField * 1.14 + (g1 - 0.5) * 0.12);
        loss = clamp(pow(edge, 1.5) * edgeWear * wear * chip * 1.5, 0.0, maxLoss);
    }

    float inner = wear * innerWear * smoothstep(0.62, 0.85, blotch);
    float scuff = clamp(inner + loss * 0.8, 0.0, 1.0);

    float3 rgb = float3(baseColor.rgb);
    rgb *= 1.0 - edgeShade * edge;

    float bevel = 1.0 - smoothstep(0.0, 2.5, d);
    rgb += bevel * (0.5 - vy) * edgeShade * 1.2;

    float sink = 1.0 - smoothstep(0.0, sinkBand, d);
    rgb *= 1.0 - sinkShade * (1.0 + press * 0.8) * sink * (1.25 - vy);
    rgb += sinkShade * 0.35 * sink * smoothstep(0.55, 1.0, vy);

    rgb += (mottle - 0.42) * mottleStrength * 0.2;
    rgb = mix(rgb, float3(scuffColor.rgb), scuff * scuffTint);
    rgb += (grain - 0.5) * grainStrength;

    if (colorGrain > 0.0) {
        rgb += float3(g1 - 0.5, noise(gp + 41.0) - 0.5, noise(gp + 83.0) - 0.5) * colorGrain;
    }

    rgb += (fiber - 0.5) * toothStrength * 1.2 + (tooth - 0.5) * toothStrength * 0.6;
    rgb += spk * speckleStrength;
    rgb *= 1.0 - pressShade * press;
    rgb += (hash(p + seed) - 0.5) / 255.0;

    float a = float(baseColor.a) * (1.0 - loss);
    return half4(half3(clamp(rgb, 0.0, 1.0) * a), half(a));
}
"""
