package com.example.feldelysium.designsystem.foundation.material.agsl

internal const val MARKER_FILM_AGSL = """
uniform float seed;
uniform float streakLen;
uniform float streakWidth;
uniform float dryness;
uniform float gapStrength;
uniform float smudgeScale;
uniform float smudgeStrength;
uniform float poreScale;
uniform float poreDensity;
uniform float grainScale;
uniform float grainStrength;
layout(color) uniform half4 baseColor;
${NOISE_LIB}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0;
    float2 r = float2(sp.x * 0.94 + sp.y * 0.34, sp.y * 0.94 - sp.x * 0.34);

    // полосы от движения маркера + сухие разрывы
    float s1 = fbm(float2(r.x / streakLen, r.y / streakWidth));
    float s2 = noise(float2(r.x / (streakLen * 0.35), r.y / (streakWidth * 0.4) + 13.0));
    float streak = s1 * 0.65 + s2 * 0.35;
    float t = 0.20 + dryness * 0.35;
    float keep = smoothstep(t, t + 0.16, streak);
    float a = mix(1.0, keep, gapStrength);

    // неравномерный нажим / смазанность
    float smudge = fbm(sp / smudgeScale + 5.0);
    a *= 1.0 - smudgeStrength * (1.0 - smoothstep(0.30, 0.65, smudge));

    // поры плёнки (мелкие дырки)
    float2 g = sp / poreScale;
    float2 cell = floor(g);
    float h = hash(cell + seed);
    float pore = step(1.0 - poreDensity, h)
        * (1.0 - smoothstep(0.15, 0.42, length(fract(g) - 0.5)));
    a *= 1.0 - pore * 0.9;

    // мелкое зерно
    float grain = noise(sp / grainScale);
    a *= 1.0 - grainStrength * (1.0 - grain);

    float3 rgb = float3(baseColor.rgb);
    rgb *= 0.92 + 0.14 * noise(sp / (smudgeScale * 0.5));

    float al = clamp(a, 0.0, 1.0) * float(baseColor.a);
    return half4(half3(clamp(rgb, 0.0, 1.0) * al), half(al));
}
"""
