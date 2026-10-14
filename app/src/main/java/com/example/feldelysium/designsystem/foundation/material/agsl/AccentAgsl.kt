package com.example.feldelysium.designsystem.foundation.material.agsl

internal const val ACCENT_AGSL = """
uniform float seed;
uniform float scale;
uniform float grainScale;
uniform float fade;
uniform float glow;
uniform float blotch;
uniform float grain;
layout(color) uniform half4 baseColor;
${NOISE_LIB}

half4 main(float2 p) {
    float2 sp = p + seed * 37.0;
    float n = fbm(sp / scale);
    float n2 = fbm(sp / (scale * 0.4) + 9.0);
    float g = noise(sp / grainScale);

    float3 c = float3(baseColor.rgb);
    float gray = dot(c, float3(0.299, 0.587, 0.114));

    float lo = (1.0 - smoothstep(0.30, 0.50, n)) * fade;
    float hi = smoothstep(0.52, 0.72, n2) * glow;
    float3 faded = mix(float3(gray), c, 0.55) * 0.88;
    c = mix(c, faded, lo);
    c = mix(c, min(c * 1.2 + 0.05, float3(1.0)), hi);

    c += (n - 0.45) * blotch;
    c += (g - 0.5) * grain;

    float2 sc = sp / (grainScale * 3.0);
    float2 gi = floor(sc);
    float rad = 0.15 + 0.2 * hash(gi + 3.1);
    float spk = step(0.975, hash(gi)) * (1.0 - smoothstep(rad * 0.6, rad, length(fract(sc) - 0.5)));
    c *= 1.0 - spk * 0.35;

    float a = float(baseColor.a);
    return half4(half3(clamp(c, 0.0, 1.0) * a), half(a));
}
"""
