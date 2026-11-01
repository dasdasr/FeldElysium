package com.feldelysium.designsystem.foundation.material.agsl

internal const val DISTRESS_AGSL = """
uniform shader content;
uniform float seed;
uniform float strength;

uniform float edgeSoft;
uniform float edgeRough;
uniform float edgeScale;
uniform float edgeErode;
uniform float bleed;

uniform float patchy;
uniform float patchScale;

uniform float dropout;
uniform float dropoutScale;

uniform float scratch;
uniform float scratchLen;
uniform float scratchWid;
uniform float scratchAngle;

uniform float speck;
uniform float speckScale;

uniform float grain;
uniform float grainScale;

uniform float splatter;
uniform float splatterRadius;

uniform float useInk;
layout(color) uniform half4 inkColor;
${NOISE_LIB}

half4 main(float2 p) {
    float R = max(edgeSoft + edgeRough * 1.5, splatterRadius) + 0.5;
    float R2 = R * 0.7071;

    float pre = float(content.eval(p).a)
        + float(content.eval(p + float2( R, 0.0)).a)
        + float(content.eval(p + float2(-R, 0.0)).a)
        + float(content.eval(p + float2(0.0,  R)).a)
        + float(content.eval(p + float2(0.0, -R)).a)
        + float(content.eval(p + float2( R2,  R2)).a)
        + float(content.eval(p + float2(-R2,  R2)).a)
        + float(content.eval(p + float2( R2, -R2)).a)
        + float(content.eval(p + float2(-R2, -R2)).a);

    if (pre < 0.002) return half4(0.0);

    float2 sp = p + seed * 37.0;

    float2 w = float2(fbm(sp / (edgeScale * 3.0)),
                      fbm(sp / (edgeScale * 3.0) + 17.0)) - 0.5;
    float2 q = p + w * edgeRough * 2.0;

    half4 c0 = content.eval(q);
    float a0 = float(c0.a);

    float ring = 0.0;
    for (int i = 0; i < 4; i++) {
        float ang = float(i) * 1.570796 + 0.4;
        ring += float(content.eval(q + float2(cos(ang), sin(ang)) * edgeSoft).a);
    }
    ring /= 4.0;

    float shape = max(mix(a0, ring, 0.5), ring * (0.6 + bleed));
    float en = fbm(sp / edgeScale);
    float keep = smoothstep(0.30, 0.52, shape + (en - 0.5) * edgeErode);
    float edgeness = 1.0 - smoothstep(0.55, 0.98, ring);

    float pn = fbm(sp / patchScale);
    float dens = 1.0 - patchy * smoothstep(0.42, 0.78, pn);

    float dz = smoothstep(0.52, 0.74, fbm(sp / (dropoutScale * 2.2) + 5.0));
    float db = smoothstep(0.50, 0.66, fbm(sp / dropoutScale + w * 1.2 + 9.0));
    float drop = dz * db * dropout * (0.35 + 0.65 * edgeness);

    float ca = cos(scratchAngle);
    float sa = sin(scratchAngle);
    float2 rp = float2(ca * sp.x + sa * sp.y, -sa * sp.x + ca * sp.y);
    float sn = noise(float2(rp.x / scratchLen, rp.y / scratchWid));
    float sz = smoothstep(0.50, 0.72, fbm(sp / (scratchLen * 0.8) + 3.0));
    float line = smoothstep(0.80, 0.86, sn) * sz * scratch;

    float2 sc = sp / speckScale;
    float2 gi = floor(sc);
    float rad = 0.14 + 0.22 * hash(gi + 3.1);
    float spkIn = step(0.90, hash(gi))
                * (1.0 - smoothstep(rad * 0.55, rad, length(fract(sc) - 0.5))) * speck;

    float gn = smoothstep(0.62, 1.0, noise(sp / grainScale)) * grain * (0.35 + 0.65 * edgeness);

    float loss = clamp((drop + line + spkIn + gn) * strength, 0.0, 1.0);
    float inkA = keep * dens * (1.0 - loss);

    float wide = 0.0;
    for (int j = 0; j < 6; j++) {
        float ang = float(j) * 1.047198 + 0.3;
        wide += float(content.eval(p + float2(cos(ang), sin(ang)) * splatterRadius).a);
    }
    wide /= 6.0;
    float spMask = smoothstep(0.50, 0.70, fbm(sp / (patchScale * 0.8) + 41.0));
    float spl = smoothstep(0.86, 0.92, noise(sp / (speckScale * 0.8) + 31.0))
              * spMask * smoothstep(0.02, 0.14, wide) * splatter;

    float A = clamp(inkA + spl * (1.0 - inkA), 0.0, 1.0);
    A *= mix(1.0, float(inkColor.a), useInk);

    float3 base = a0 > 0.05 ? float3(c0.rgb) / a0 : float3(inkColor.rgb);
    float3 rgb = mix(base, float3(inkColor.rgb), useInk);
    rgb *= 0.95 + 0.10 * pn;

    return half4(half3(rgb * A), half(A));
}
"""
