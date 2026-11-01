package com.feldelysium.designsystem.foundation.material.agsl

internal const val MARKER_FILM_AGSL = """
uniform shader content;
uniform float seed;
uniform float coverage;
uniform float unevenness;
uniform float blotchScale;
uniform float poolEdge;
uniform float edgeSoft;
uniform float erode;
uniform float erodeScale;
uniform float wobble;
uniform float streak;
uniform float streakLen;
uniform float streakWid;
uniform float streakAngle;
uniform float skip;
uniform float skipCoverage;
uniform float skipScale;
uniform float pinhole;
uniform float grain;
uniform float grainScale;
uniform float smudge;
uniform float smudgeScale;
uniform float smudgeRadius;
uniform float speck;
uniform float useInk;
layout(color) uniform half4 inkColor;
${NOISE_LIB}

float fbmL(float2 p) { return noise(p) * 0.65 + noise(p * 2.03 + 7.1) * 0.35; }

half4 main(float2 p) {
    half4 c0 = content.eval(p);
    float a0 = float(c0.a);

    float R = max(edgeSoft, smudgeRadius) + wobble * 2.0;
    float near = float(content.eval(p + float2( R, 0.0)).a)
               + float(content.eval(p + float2(-R, 0.0)).a)
               + float(content.eval(p + float2(0.0,  R)).a)
               + float(content.eval(p + float2(0.0, -R)).a);
    if (a0 + near < 0.002) return half4(0.0);
    float wide = near * 0.25;

    float2 sp = p + seed * 37.0;
    float2 w = float2(fbmL(sp / (erodeScale * 3.0)),
                      fbmL(sp / (erodeScale * 3.0) + 17.0)) - 0.5;
    float2 q = p + w * wobble * 2.0;

    float ring = (float(content.eval(q + float2( edgeSoft, 0.0)).a)
                + float(content.eval(q + float2(-edgeSoft, 0.0)).a)
                + float(content.eval(q + float2(0.0,  edgeSoft)).a)
                + float(content.eval(q + float2(0.0, -edgeSoft)).a)) * 0.25;

    float shape = mix(a0, ring, 0.5);
    float edge = clamp((a0 - ring) * 2.0, 0.0, 1.0);

    float en = fbm(sp / erodeScale);
    float keep = smoothstep(0.28, 0.52, shape + (en - 0.5) * erode);

    float blotch = fbm(sp / blotchScale);
    float tone = coverage - unevenness * smoothstep(0.40, 0.72, blotch) + poolEdge * edge;

    float ca = cos(streakAngle);
    float sa = sin(streakAngle);
    float2 rp = float2(ca * sp.x + sa * sp.y, -sa * sp.x + ca * sp.y);
    float st = noise(float2(rp.x / streakLen, rp.y / streakWid)) * 0.65
             + noise(float2(rp.x / (streakLen * 0.4) + 5.0, rp.y / (streakWid * 2.3) + 9.0)) * 0.35;
    tone -= streak * smoothstep(0.35, 0.85, st);
    tone += (0.5 - noise(sp / grainScale)) * grain;

    float lo = 0.80 - 0.16 * skipCoverage;
    float sk = smoothstep(lo, lo + 0.12,
        fbmL(float2(rp.x / (skipScale * 1.6), rp.y / (skipScale * 0.6)) + 3.0));
    float pinMask = smoothstep(0.55, 0.75, fbmL(sp / (blotchScale * 0.7) + 11.0));
    float pin = smoothstep(0.80, 0.88, noise(sp / (grainScale * 2.2))) * pinMask;

    float inkA = clamp(tone, 0.0, 1.0) * keep * (1.0 - sk * skip) * (1.0 - pin * pinhole);

    float smudgeMask = smoothstep(0.60, 0.78, fbmL(sp / smudgeScale + 23.0));
    float halo = wide * smudgeMask * smudge * (1.0 - keep);

    float spkMask = smoothstep(0.50, 0.70, fbmL(sp / (smudgeScale * 0.8) + 41.0));
    float spk = smoothstep(0.86, 0.92, noise(sp / (grainScale * 2.8) + 31.0))
              * spkMask * smoothstep(0.02, 0.12, wide) * speck;

    float A = clamp(inkA + (halo + spk) * (1.0 - inkA), 0.0, 1.0);
    A *= mix(1.0, float(inkColor.a), useInk);

    float3 base = a0 > 0.05 ? float3(c0.rgb) / a0 : float3(inkColor.rgb);
    float3 rgb = mix(base, float3(inkColor.rgb), useInk);
    rgb *= 0.94 + 0.12 * blotch;

    return half4(half3(rgb * A), half(A));
}
"""
