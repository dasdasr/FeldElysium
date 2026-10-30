package com.feldelysium.designsystem.foundation.material.agsl

internal const val NOISE_LIB = """
float hash(float2 p) {
    float3 p3 = fract(float3(p.x, p.y, p.x) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);

    f = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(
            hash(i),
            hash(i + float2(1.0, 0.0)),
            f.x
        ),
        mix(
            hash(i + float2(0.0, 1.0)),
            hash(i + float2(1.0, 1.0)),
            f.x
        ),
        f.y
    );
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

internal const val NOISE_LIB_FAST = """
float hash(float2 p) {
    float3 p3 = fract(p.xyx * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float noise(float2 p) {
    float2 i = floor(p);
    float2 f = fract(p);
    float2 u = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + float2(1.0, 0.0));
    float c = hash(i + float2(0.0, 1.0));
    float d = hash(i + float2(1.0, 1.0));
    return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
}

float fbm(float2 p) {
    float v = 0.5 * noise(p);
    p = p * 2.03 + 17.0;
    v += 0.25 * noise(p);
    p = p * 2.03 + 17.0;
    return v + 0.125 * noise(p);
}
"""
