package com.example.feldelysium.designsystem.material

private const val NOISE_LIB = """
float hash(float2 p) {
    float3 p3 = fract(float3(p.x, p.y, p.x) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}
float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), f.x),
               mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), f.x), f.y);
}
float fbm(float2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 3; i++) {
        v += a * noise(p);
        p = p * 2.03 + 17.0;
        a *= 0.5;
    }
    return v;
}
"""

internal const val PAPER_AGSL = """
uniform float2 size;
uniform float seed;
uniform float wear;
uniform float edgeWear;
uniform float time;
uniform float press;
uniform float rim;
uniform float band;
layout(color) uniform half4 baseColor;
layout(color) uniform half4 scuffColor;
$NOISE_LIB
half4 main(float2 p) {
    float d = min(min(p.x, size.x - p.x), min(p.y, size.y - p.y)) - rim;
    float edge = 1.0 - smoothstep(0.0, band, d);

    float2 sp = p + seed * 37.0;
    float mottle = fbm(sp * 0.012);
    float blotch = fbm(sp * 0.09);
    float grain  = noise(sp * 0.9 + time * 31.7);

    float chip = smoothstep(0.42 - press * 0.05, 0.70, blotch * 1.14 + (grain - 0.5) * 0.15);
    float loss = clamp(pow(edge, 1.5) * edgeWear * wear * chip * 1.5, 0.0, 0.8);
    float inner = wear * 0.12 * smoothstep(0.62, 0.85, blotch);
    float scuff = clamp(inner + loss * 0.8, 0.0, 1.0);

    float3 rgb = float3(baseColor.rgb);
    rgb *= 1.0 + (mottle - 0.4) * 0.22 - 0.08 * edge;
    rgb = mix(rgb, float3(scuffColor.rgb), scuff * 0.5);
    rgb += (grain - 0.5) * 0.05;
    rgb *= 1.0 - 0.12 * press;

    float a = float(baseColor.a) * (1.0 - loss);
    return half4(half3(rgb * a), half(a));
}
"""

internal const val INK_AGSL = """
uniform float seed;
uniform float time;
layout(color) uniform half4 inkColor;
$NOISE_LIB
half4 main(float2 p) {
    float n = noise(p * 0.55 + seed * 11.0);
    float m = noise(p * 0.07 + seed * 5.0);
    float a = mix(0.28, 1.0, smoothstep(0.15, 0.85, n * 0.6 + m * 0.4)) * float(inkColor.a);
    return half4(half3(float3(inkColor.rgb) * a), half(a));
}
"""
