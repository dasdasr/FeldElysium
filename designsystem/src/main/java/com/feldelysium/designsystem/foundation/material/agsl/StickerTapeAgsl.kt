package com.feldelysium.designsystem.foundation.material.agsl

internal const val STICKER_TAPE_AGSL = """
uniform float2 size;
uniform float seed;

uniform float tearAmp;
uniform float tearScale;
uniform float slantL;
uniform float slantR;
uniform float fray;
uniform float rimWidth;
uniform float cutAmp;

uniform float cornerRx;
uniform float cornerRy;
uniform float layerZone;
uniform float layerAmp;
uniform float layerScale;
uniform float layerStrength;

uniform float stainScale;
uniform float stainStrength;
uniform float mottleScale;
uniform float mottleStrength;
uniform float fiberStrength;
uniform float grainStrength;
uniform float speckStrength;
uniform float ageStrength;

layout(color) uniform half4 baseColor;
layout(color) uniform half4 stainColor;
layout(color) uniform half4 rimColor;
${NOISE_LIB}

float ridge(float2 p) { return 1.0 - abs(noise(p) * 2.0 - 1.0); }

float tearProfile(float t, float s) {
    float big = fbm(float2(t / tearScale, s));
    float mid = ridge(float2(t / (tearScale * 0.35), s + 3.0));
    float small = noise(float2(t / 2.2, s + 7.0));
    float v = 0.55 * smoothstep(0.25, 0.70, big) + 0.30 * mid * mid + 0.15 * small;
    return clamp(v, 0.0, 1.0);
}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0;
    float s1 = seed * 1.7;
    float vy = p.y / size.y;

    float slL = slantL >= 0.0 ? slantL * vy : -slantL * (1.0 - vy);
    float slR = slantR >= 0.0 ? slantR * vy : -slantR * (1.0 - vy);
    float offL = 1.0 + slL + tearAmp * tearProfile(p.y, s1);
    float offR = 1.0 + slR + tearAmp * tearProfile(p.y, s1 + 40.0);

    float dLeft = p.x - offL;
    float dRight = (size.x - offR) - p.x;
    float dTorn = min(dLeft, dRight);

    float topOff = 0.6 + cutAmp * noise(float2(p.x / 26.0, s1 + 11.0));
    float botOff = 0.6 + cutAmp * noise(float2(p.x / 26.0, s1 + 23.0));
    float dStraight = min(p.y - topOff, (size.y - botOff) - p.y);

    float frayN = noise(sp / 1.4) * 0.65 + noise(sp / 0.7) * 0.35;
    float d = min(dTorn + (frayN - 0.5) * fray, dStraight);

    float2 cu = float2((p.x - (size.x - cornerRx - 1.0)) / cornerRx,
                       (p.y - (size.y - cornerRy - 0.6)) / cornerRy);
    float cd = (1.0 - length(max(cu, 0.0))) * min(cornerRx, cornerRy);
    d = min(d, cd + (frayN - 0.5) * fray);

    float2 cv = float2((size.x - p.x) / layerZone, (size.y - p.y) / (layerZone * 0.8));
    float cw = 1.0 - smoothstep(0.0, 1.0, length(cv));
    float2 lw = float2(fbm(sp / (layerScale * 2.5)), fbm(sp / (layerScale * 2.5) + 6.0)) - 0.5;
    float h = fbm(sp / layerScale + lw * 2.0) + cw * 0.35 + (ridge(sp / 3.0) - 0.5) * 0.05;
    float stepped = floor(h * 4.0) / 4.0;
    float levelT = clamp((mix(h, stepped, 0.6) - 0.45) / 0.5, 0.0, 1.0);
    d -= layerAmp * levelT * cw;

    float a = smoothstep(-0.3, 0.9, d);

    float rimMask = max(1.0 - smoothstep(0.0, rimWidth, dTorn),
                        (1.0 - smoothstep(0.0, rimWidth, d)) * cw);
    float rimFib = noise(float2(sp.x / 4.0, sp.y / 1.1));

    float mottle = fbm(sp / mottleScale);
    float stain = smoothstep(0.42, 0.72, fbm(sp / stainScale + 5.0));
    float fibA = noise(float2(sp.x / 18.0, sp.y / 0.9));
    float fibB = noise(float2((sp.x + sp.y * 0.35) / 11.0, (sp.y - sp.x * 0.35) / 0.8));
    float grain = noise(sp / 0.9);
    float blob = noise(sp / 3.0);
    float speck = smoothstep(0.80, 0.86, blob) * smoothstep(0.50, 0.60, noise(sp / 40.0 + 9.0));
    float age = (1.0 - smoothstep(0.0, 16.0, d)) * (0.5 + noise(sp / 10.0));

    float lvlI = floor(h * 5.0) / 5.0;
    float fr = fract(h * 5.0);
    float line = (1.0 - smoothstep(0.0, 0.08, min(fr, 1.0 - fr))) * smoothstep(0.05, 0.3, cw);

    float3 rgb = float3(baseColor.rgb);
    rgb += (mottle - 0.45) * mottleStrength;
    rgb += ((fibA - 0.5) * 0.6 + (fibB - 0.5) * 0.4) * fiberStrength;
    rgb += (grain - 0.5) * grainStrength;
    rgb = mix(rgb, float3(stainColor.rgb), stain * stainStrength);
    rgb = mix(rgb, float3(stainColor.rgb), speck * speckStrength);
    rgb = mix(rgb, float3(stainColor.rgb), age * ageStrength);
    rgb = mix(rgb, float3(rimColor.rgb), cw * smoothstep(0.35, 0.9, lvlI) * 0.5 * layerStrength);
    rgb = mix(rgb, float3(stainColor.rgb), line * 0.45 * layerStrength);
    rgb = mix(rgb, float3(rimColor.rgb), rimMask * (0.3 + 0.7 * rimFib) * 0.8);

    float al = float(baseColor.a) * a;
    return half4(half3(rgb * al), half(al));
}
"""
