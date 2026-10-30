package com.feldelysium.designsystem.foundation.material.agsl

internal const val SEGMENT_PAINT_AGSL = """
uniform float2 size;
uniform float seed;
uniform float u;
uniform float smear;
layout(color) uniform half4 baseColor;
layout(color) uniform half4 deepColor;
layout(color) uniform half4 lightColor;
layout(color) uniform half4 holeColor;
${NOISE_LIB}

half4 main(float2 p) {
    float2 sp = p + seed * 41.0;
    float vy = p.y / size.y;
    float inset = 1.5 * u;

    float brL = noise(float2(p.y / (0.8 * u), seed * 3.1)) * 0.55
              + noise(float2(p.y / (2.4 * u), seed * 1.7 + 5.0)) * 0.45;
    float brR = noise(float2(p.y / (0.8 * u), seed * 2.3 + 17.0)) * 0.55
              + noise(float2(p.y / (2.4 * u), seed * 0.9 + 31.0)) * 0.45;

    float frayL = (1.4 + 9.0 * max(smear, 0.0)) * u;
    float frayR = (1.4 + 9.0 * max(-smear, 0.0)) * u;

    float x0 = inset + frayL * brL;
    float x1 = size.x - inset + 2.0 * u - 4.0 * u * vy - frayR * brR;

    float top = inset + (noise(float2(p.x / (18.0 * u), seed + 2.0)) - 0.5) * 1.0 * u;
    float bot = size.y - inset - (noise(float2(p.x / (18.0 * u), seed + 8.0)) - 0.5) * 1.0 * u;

    float dx = min(p.x - x0, x1 - p.x);
    float dy = min(p.y - top, bot - p.y);
    float dRaw = min(dx, dy);

    float rad = 3.0 * u;
    float cx = max(rad - dx, 0.0);
    float cy = max(rad - dy, 0.0);
    float d = (cx > 0.0 && cy > 0.0) ? rad - length(float2(cx, cy)) : dRaw;

    d += (noise(sp / (1.3 * u)) - 0.5) * 1.4 * u * (1.0 - smoothstep(0.0, 4.0 * u, dRaw));

    float a = smoothstep(0.0, 1.0 * u, d);

    float endZone = 1.0 - smoothstep(0.0, 18.0 * u, dx);
    float gap = smoothstep(0.62, 0.86, noise(float2(sp.x / (10.0 * u), sp.y / (0.7 * u))));
    a *= 1.0 - gap * endZone * (0.55 + 0.4 * abs(smear));
    if (a <= 0.002) return half4(0.0);

    float big = fbm(sp / (22.0 * u));
    float mid = fbm(sp / (7.0 * u) + 3.0);
    float streakA = noise(float2(sp.x / (28.0 * u), sp.y / (0.9 * u)));
    float streakB = noise(float2(sp.x / (60.0 * u), sp.y / (0.6 * u) + 9.0));
    float grain = noise(sp / (0.55 * u));

    float3 c = mix(float3(deepColor.rgb), float3(baseColor.rgb), smoothstep(0.25, 0.65, big));
    c = mix(c, float3(lightColor.rgb), smoothstep(0.55, 0.80, mid) * 0.45);
    c *= 1.0 - 0.10 * smoothstep(0.55, 0.90, streakA);
    c += (streakB - 0.5) * 0.06;
    c += (grain - 0.5) * 0.07;
    c *= 1.0 - 0.16 * (1.0 - smoothstep(0.0, 3.0 * u, dRaw));
    c += 0.03 * (0.5 - vy);

    float cl = smoothstep(0.45, 0.72, fbm(sp / (26.0 * u) + 7.0));

    float2 s1 = sp / (2.2 * u);
    float2 g1 = floor(s1);
    float2 c1 = float2(0.3 + 0.4 * hash(g1 + 1.7), 0.3 + 0.4 * hash(g1 + 9.2));
    float r1 = 0.10 + 0.18 * hash(g1 + 3.1);
    float pit1 = step(1.0 - (0.04 + 0.30 * cl), hash(g1))
               * (1.0 - smoothstep(r1 * 0.5, r1, length(fract(s1) - c1)));

    float2 s2 = sp / (5.5 * u);
    float2 g2 = floor(s2);
    float2 c2 = float2(0.3 + 0.4 * hash(g2 + 4.1), 0.3 + 0.4 * hash(g2 + 6.6));
    float r2 = 0.12 + 0.20 * hash(g2 + 7.7);
    float pit2 = step(1.0 - (0.02 + 0.12 * cl), hash(g2 + 11.0))
               * (1.0 - smoothstep(r2 * 0.5, r2, length(fract(s2) - c2)));

    float hole = clamp(pit1 * 0.9 + pit2, 0.0, 1.0);
    c = mix(c, float3(holeColor.rgb), hole * 0.92);

    return half4(half3(clamp(c, 0.0, 1.0) * a), half(a));
}
"""
