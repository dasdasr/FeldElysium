package com.feldelysium.designsystem.foundation.material.agsl

internal const val SLIDER_TRACK_AGSL = """
uniform float2 size;
uniform float seed;
uniform float u;
uniform float fillX;
uniform float fadeW;
layout(color) uniform half4 fillColor;
layout(color) uniform half4 offColor;
${NOISE_LIB}

half4 main(float2 p) {
    float r = size.y * 0.5;
    float2 q = abs(p - size * 0.5) - (size * 0.5 - r);
    float dist = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;

    float2 sp = p + seed * 37.0;
    float rough = noise(sp / (2.4 * u)) * 0.7 + noise(sp / (0.9 * u)) * 0.3;
    float shape = 1.0 - smoothstep(-0.6 * u, 0.6 * u, dist + (rough - 0.5) * 1.1 * u);
    if (shape <= 0.001) return half4(0.0);

    float mot = fbm(sp / (size.y * 2.6));
    float mot2 = fbm(sp / (size.y * 0.9) + 5.0);
    float grain = noise(sp / (0.7 * u)) - 0.5;
    float streak = noise(float2(sp.x / (size.y * 1.6), sp.y / (0.8 * u)));
    float clus = smoothstep(0.52, 0.74, fbm(sp / (size.y * 1.8) + 3.0));
    float spk = step(0.90, noise(sp / (1.3 * u))) * clus;
    float vy = p.y / size.y;

    float hd = (fillX - p.x) / (size.y * 5.0);
    float head = exp(-hd * hd) * step(p.x, fillX + 1.0);
    
    float3 f = float3(fillColor.rgb);
    f *= 0.80 + 0.32 * head;
    f += float3(0.035, 0.028, 0.018) * head;
    f *= 0.90 + 0.20 * mot;
    f *= 1.0 - 0.14 * smoothstep(0.55, 0.90, streak);
    f *= 1.0 - spk * 0.38;
    f *= 1.07 - 0.16 * vy;
    f += grain * 0.07;
    float gray = dot(f, float3(0.299, 0.587, 0.114));
    f = mix(float3(gray), f, 1.0 + 0.18 * head);

    float3 o = float3(offColor.rgb);
    o *= 0.88 + 0.24 * mot2;
    o += grain * 0.035 + spk * 0.03;
    o *= 1.04 - 0.10 * vy;

    float jitter = (noise(sp / (0.6 * u)) - 0.5) * fadeW * 0.8;
    float m = 1.0 - smoothstep(fillX - fadeW, fillX, p.x + jitter);

    float3 rgb = mix(o, f, m);
    rgb *= 0.88 + 0.12 * smoothstep(0.0, 2.0 * u, -dist);
    rgb += (hash(p + seed) - 0.5) / 255.0;

    return half4(half3(clamp(rgb, 0.0, 1.0) * shape), half(shape));
}
"""
