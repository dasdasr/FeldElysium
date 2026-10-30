package com.feldelysium.designsystem.foundation.material.agsl

internal const val DRY_PAPER_AGSL = """
uniform float2 size;
uniform float seed;
uniform float press;
uniform float rim;

uniform float tearDepth;
uniform float tearScale;
uniform float tearCoverage;
uniform float bow;

uniform float crackScale;
uniform float crackDepth;
uniform float crackWidth;
uniform float crackStrength;
uniform float crackCoverage;

uniform float grainScale;
uniform float grainStrength;
uniform float fiberStrength;
uniform float mottleScale;
uniform float mottleStrength;
uniform float pressShade;

layout(color) uniform half4 baseColor;
layout(color) uniform half4 crackColor;
${NOISE_LIB}

float2 hash2(float2 p) { return float2(hash(p), hash(p + 19.19)); }

float voronoiEdge(float2 x) {
    float2 n = floor(x);
    float2 f = fract(x);
    float f1 = 8.0;
    float f2 = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            float2 g = float2(float(i), float(j));
            float2 r = g + hash2(n + g) - f;
            float d = dot(r, r);
            if (d < f1) { f2 = f1; f1 = d; }
            else if (d < f2) { f2 = d; }
        }
    }
    return sqrt(f2) - sqrt(f1);
}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0;

    float dl = p.x;
    float dr = size.x - p.x;
    float dt = p.y;
    float db = size.y - p.y;
    float d = min(min(dl, dr), min(dt, db));
    bool horiz = min(dt, db) < min(dl, dr);
    float along = horiz ? p.x : p.y;
    float alongLen = horiz ? size.x : size.y;
    float depth = d - rim;

        
    float cut = bow * sin(3.14159 * clamp(along / alongLen, 0.0, 1.0));
    float crack = 0.0;
    float edgeAlpha = 1.0;
    
    float limit = max(bow + tearDepth + 4.0, crackDepth * 1.4) + 1.0;
    
    if (depth < limit) {
        float2 w = float2(
            fbm(sp / (tearScale * 3.0)),
            fbm(sp / (tearScale * 3.0) + 7.7)
        ) - 0.5;
    
        float tn = fbm(sp / tearScale + w * 2.0);
        float zoneN = fbm(sp / (tearScale * 6.0) + 3.0);
        float zoneT = 0.62 - 0.40 * tearCoverage;
        float zone = smoothstep(zoneT, zoneT + 0.06, zoneN);
        float bite = smoothstep(0.42, 0.60, tn)
                   + (noise(sp / 2.5) - 0.5) * 0.3;
    
        cut += tearDepth * zone * clamp(bite, 0.0, 1.0);
    
        float2 lc = float2(along, max(depth, 0.0));
        float2 cq = float2(
            lc.x / crackScale,
            lc.y / (crackScale * 3.5)
        );
    
        cq += (
            float2(fbm(sp / 14.0), fbm(sp / 14.0 + 5.0)) - 0.5
        ) * 0.7;
        cq += seed * 3.1;
    
        float ce = voronoiEdge(cq);
    
        float cmT = 0.80 - 0.45 * crackCoverage;
        float cm = smoothstep(
            cmT, cmT + 0.08,
            noise(float2(lc.x / (crackScale * 1.7) + seed * 3.0, 1.0))
        );
    
        float len = crackDepth * (
            0.5 + 0.9 * noise(
                float2(lc.x / (crackScale * 0.8), 3.0 + seed)
            )
        );
    
        float fall = 1.0 - smoothstep(0.0, len, depth);
        float wpx = crackWidth * (0.4 + 0.6 * fall);
    
        crack = (1.0 - smoothstep(0.0, wpx / crackScale, ce))
              * cm * fall;
    
        cut += crack * (1.0 - smoothstep(0.0, 4.0, depth));
    
        edgeAlpha = smoothstep(cut - 0.5, cut + 0.5, depth);
    }


    float mottle = fbm(sp / mottleScale);
    float grain = noise(sp / grainScale);
    float fiber = noise(float2(sp.x / (grainScale * 6.0), sp.y / (grainScale * 0.5)));
    float scuff = smoothstep(0.42, 0.58, fbm(sp / (mottleScale * 0.6) + 11.0));

    float3 rgb = float3(baseColor.rgb);
    rgb += (mottle - 0.44) * mottleStrength;
    rgb += (grain - 0.5) * grainStrength;
    rgb += (fiber - 0.5) * fiberStrength;
    rgb = mix(rgb, float3(crackColor.rgb), scuff * 0.08);
    rgb *= 1.05 - 0.12 * (p.y / size.y);
    rgb *= 1.0 - 0.10 * (1.0 - smoothstep(1.0, 8.0, depth - cut));
    rgb += 0.05 * (1.0 - smoothstep(0.0, 1.6, depth - cut));
    rgb = mix(rgb, float3(crackColor.rgb), crack * crackStrength);
    float speck = step(0.994, noise(sp / 1.4));
    rgb = mix(rgb, float3(crackColor.rgb), speck * 0.45);   
    rgb *= 1.0 - pressShade * press;

    float a = float(baseColor.a) * edgeAlpha;
    return half4(half3(rgb * a), half(a));
}
"""
