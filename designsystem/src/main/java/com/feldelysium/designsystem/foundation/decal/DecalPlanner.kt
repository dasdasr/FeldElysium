package com.feldelysium.designsystem.foundation.decal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.floor
import kotlin.random.Random

internal class DecalPlan(val items: List<Placement>, val leak: Leak?)

internal fun DrawScope.drawDecals(plan: DecalPlan) {
    plan.leak?.let { with(it) { draw() } }
    for (p in plan.items) {
        val c = p.bounds.center
        val e = p.decal.extent
        withTransform({
            rotate(p.rotation, c)
            if (p.mirror) scale(-1f, 1f, c)
            translate(c.x - e.width / 2f, c.y - e.height / 2f)
        }) { with(p.decal) { draw(p.color, p.alpha) } }
    }
}

private val Mirrorable = setOf(DecalKind.Text, DecalKind.DataBlock, DecalKind.Counter)
private val UseLimit = mapOf(
    DecalKind.DataBlock to 1,
    DecalKind.Ruler to 1,
    DecalKind.Perforation to 1,
    DecalKind.Corner to 1,
    DecalKind.Tape to 1,
    DecalKind.Stitch to 1,
)

internal fun planDecals(
    seed: Int,
    size: Size,
    ambient: Float,
    spec: DecalSpec,
    ctx: DecalContext,
    density: Density,
    measurer: TextMeasurer,
    leftInsetPx: Float,
): DecalPlan {
    val a = ambient.coerceIn(0f, 1f)
    if (a <= 0f || size.width < 40f || size.height < 20f) return DecalPlan(emptyList(), null)

    val d = density.density
    val rng = Random(seed * 7919 + 0xDEC41)

    val leak = if (size.width > 60f * d && rng.nextFloat() < spec.leakChance * a) {
        Leak(
            size = size,
            rightSide = leftInsetPx > 0f || rng.nextBoolean(),
            color = spec.leakColor,
            strength = 0.08f + 0.10f * a,
        )
    } else {
        null
    }

    val areaDp = size.width * size.height / (d * d)
    val x = a + areaDp / 20000f * a * a * 3f
    val budget = floor(x + rng.nextFloat()).toInt().coerceAtMost(spec.maxDecals)
    if (budget == 0) return DecalPlan(emptyList(), leak)

    val safe = Rect(leftInsetPx, 0f, size.width, size.height)
    val used = HashMap<DecalKind, Int>()
    val taken = ArrayList<Rect>()
    val out = ArrayList<Placement>()

    var attempts = 0
    while (out.size < budget && attempts++ < budget * 5) {
        val kind = pickKind(rng, spec, a, used, size, d) ?: break
        val decal = buildDecal(kind, rng, ctx, density, LayoutDirection.Ltr, measurer, size) ?: continue
        val spot = findSpot(rng, spec, decal, safe, taken, d, a) ?: continue
        used[kind] = (used[kind] ?: 0) + 1
        taken += spot.bounds
        val cap = spec.alphaCap[kind] ?: 0.1f
        out += Placement(
            decal,
            spot.bounds,
            spot.rotation,
            mirror = kind in Mirrorable && rng.nextFloat() < 0.2f,
            color = spec.tones[rng.nextInt(spec.tones.size)],
            alpha = cap * (0.45f + 0.55f * rng.nextFloat()) * (0.5f + 0.5f * a),
        )
    }
    return DecalPlan(out, leak)
}

private fun pickKind(
    rng: Random,
    spec: DecalSpec,
    a: Float,
    used: Map<DecalKind, Int>,
    size: Size,
    d: Float,
): DecalKind? {
    val wDp = size.width / d
    val hDp = size.height / d

    fun weightOf(k: DecalKind, base: Float): Float {
        if ((used[k] ?: 0) >= (UseLimit[k] ?: Int.MAX_VALUE)) return 0f
        return when (k) {
            DecalKind.Perforation -> if (hDp < 30f) 0f else base * a
            DecalKind.DataBlock -> if (wDp < 200f || hDp < 80f) 0f else base * a
            DecalKind.Tally -> if (wDp < 80f) 0f else base
            else -> base
        }
    }

    var total = 0f
    for ((k, b) in spec.weights) total += weightOf(k, b)
    if (total <= 0f) return null

    var t = rng.nextFloat() * total
    var last: DecalKind? = null
    for ((k, b) in spec.weights) {
        val v = weightOf(k, b)
        if (v <= 0f) continue
        last = k
        t -= v
        if (t <= 0f) return k
    }
    return last
}

private const val SPOT_TRIES = 10
private const val CENTER_ALPHA_MIN = 0.7f
private const val CENTER_TILT = 6f
private const val STRIP_MARGIN_DP = 1.5f

private class Spot(val bounds: Rect, val rotation: Float)

private enum class Side { Top, Bottom, Left, Right }

private fun findSpot(
    rng: Random,
    spec: DecalSpec,
    decal: Decal,
    safe: Rect,
    taken: List<Rect>,
    d: Float,
    a: Float,
): Spot? {
    val gap = spec.gap.value * d
    val corner = decal is CornerFoldDecal
    repeat(SPOT_TRIES) {
        val spot = if (corner) tryCorner(rng, decal, safe) else tryEdge(rng, spec, decal, safe, d, a)
        if (spot != null && isFree(spot.bounds, taken, gap)) return spot
    }
    return null
}

private fun isFree(rect: Rect, taken: List<Rect>, gap: Float): Boolean = taken.none { it.inflate(gap).overlaps(rect) }

private fun tryCorner(rng: Random, decal: Decal, safe: Rect): Spot {
    val w = decal.extent.width
    val h = decal.extent.height
    val right = rng.nextBoolean()
    val bottom = rng.nextBoolean()
    val x = if (right) safe.right - w else safe.left
    val y = if (bottom) safe.bottom - h else safe.top
    val rotation = when {
        !right && !bottom -> 0f
        right && !bottom -> 90f
        right -> 180f
        else -> 270f
    }
    return Spot(Rect(x, y, x + w, y + h), rotation)
}

private fun tryEdge(rng: Random, spec: DecalSpec, decal: Decal, safe: Rect, d: Float, a: Float): Spot? {
    val strip = decal as? StripDecal
    val isStrip = strip != null
    val center = !isStrip && spec.allowCenter && a > CENTER_ALPHA_MIN &&
        rng.nextFloat() < (a - CENTER_ALPHA_MIN) * 2f
    val side = pickSide(rng, strip)
    val vertical = !center && (side == Side.Left || side == Side.Right)

    val bw = if (vertical) decal.extent.height else decal.extent.width
    val bh = if (vertical) decal.extent.width else decal.extent.height
    if (bw > safe.width || bh > safe.height) return null

    val margin = edgeMargin(rng, spec, isStrip, d)
    val rx = snap(rng, isStrip)
    val ry = snap(rng, isStrip)

    val origin = if (center) {
        Offset(
            safe.left + (safe.width - bw) * rng.nextFloat(),
            (safe.height - bh) * rng.nextFloat(),
        )
    } else {
        sideOrigin(side, safe, bw, bh, margin, rx, ry)
    }
    val x = origin.x.coerceIn(safe.left, safe.right - bw)
    val y = origin.y.coerceIn(safe.top, safe.bottom - bh)
    return Spot(Rect(x, y, x + bw, y + bh), rotationFor(rng, vertical, center))
}

private fun sideOrigin(side: Side, safe: Rect, bw: Float, bh: Float, margin: Float, rx: Float, ry: Float): Offset {
    val freeX = safe.left + (safe.width - bw) * rx
    val freeY = safe.top + (safe.height - bh) * ry
    return when (side) {
        Side.Top -> Offset(freeX, safe.top + margin)
        Side.Bottom -> Offset(freeX, safe.bottom - bh - margin)
        Side.Left -> Offset(safe.left + margin, freeY)
        Side.Right -> Offset(safe.right - bw - margin, freeY)
    }
}

private fun pickSide(rng: Random, strip: StripDecal?): Side = when {
    strip == null -> Side.entries[rng.nextInt(4)]
    strip.vertical -> if (rng.nextBoolean()) Side.Left else Side.Right
    else -> if (rng.nextBoolean()) Side.Top else Side.Bottom
}

private fun edgeMargin(rng: Random, spec: DecalSpec, isStrip: Boolean, d: Float): Float {
    if (isStrip) return STRIP_MARGIN_DP * d
    val r = spec.edgeMarginDp
    return (r.start + rng.nextFloat() * (r.endInclusive - r.start)) * d
}

private fun snap(rng: Random, isStrip: Boolean): Float {
    val r = rng.nextFloat()
    if (isStrip || rng.nextFloat() >= 0.5f) return r
    return if (r < 0.5f) 0f else 1f
}

private fun rotationFor(rng: Random, vertical: Boolean, center: Boolean): Float = when {
    vertical -> if (rng.nextBoolean()) 90f else 270f
    center -> (rng.nextFloat() - 0.5f) * CENTER_TILT
    else -> 0f
}
