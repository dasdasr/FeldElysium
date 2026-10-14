package com.example.feldelysium.playground

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feldelysium.designsystem.components.button.FeldIconButton
import com.example.feldelysium.designsystem.components.display.FeldIcon
import com.example.feldelysium.designsystem.components.display.FeldIconId
import com.example.feldelysium.designsystem.components.input.FeldChip
import com.example.feldelysium.designsystem.components.input.FeldSlider
import com.example.feldelysium.designsystem.components.input.FeldSliderColors
import com.example.feldelysium.designsystem.components.input.FeldSwitch
import com.example.feldelysium.designsystem.foundation.decal.Leak
import com.example.feldelysium.designsystem.foundation.material.DistressSpec
import com.example.feldelysium.designsystem.foundation.material.DryPaperSpec
import com.example.feldelysium.designsystem.foundation.material.FeldPaper
import com.example.feldelysium.designsystem.foundation.material.FeldTape
import com.example.feldelysium.designsystem.foundation.material.MarkerFilmSpec
import com.example.feldelysium.designsystem.foundation.material.PaperSpec
import com.example.feldelysium.designsystem.foundation.material.StickerTapeSpec
import com.example.feldelysium.designsystem.foundation.material.accentBrush
import com.example.feldelysium.designsystem.foundation.material.agsl.SEGMENT_PAINT_AGSL
import com.example.feldelysium.designsystem.foundation.material.agsl.SLIDER_TRACK_AGSL
import com.example.feldelysium.designsystem.foundation.material.distressMaterial
import com.example.feldelysium.designsystem.foundation.material.dryPaperMaterial
import com.example.feldelysium.designsystem.foundation.material.markerFilmMaterial
import com.example.feldelysium.designsystem.foundation.material.paperMaterial
import com.example.feldelysium.designsystem.foundation.material.stickerTapeMaterial
import com.example.feldelysium.designsystem.foundation.material.wornBrush
import com.example.feldelysium.designsystem.foundation.shape.RoughShape
import com.example.feldelysium.designsystem.foundation.shape.RoughStyle
import com.example.feldelysium.designsystem.foundation.utils.FeldPalette
import com.example.feldelysium.theme.DiscoDialogueTheme

private val Bg = Color(0xFF0B1011)
private val TileBg = Color(0xFF141C1C)
private val Teal = Color(0xFF3FA58E)

private data class Param(
    val key: String,
    val label: String,
    val range: ClosedFloatingPointRange<Float>,
    val default: Float,
)

private data class Swatch(val name: String, val color: Color)

private class Values(private val m: Map<String, Float>) {
    operator fun get(k: String): Float = m[k] ?: 0f
    fun dp(k: String): Dp = get(k).dp
}

private class ShaderDemo(
    val id: String,
    val title: String,
    val fn: String,
    val family: String,
    val note: String,
    val params: List<Param>,
    val swatches: List<Swatch>,
    val fixed: Map<String, Float> = emptyMap(),
    val tileHeight: Dp = 130.dp,
    val content: @Composable (Values, Color, Int, Modifier) -> Unit,
)

private fun p(key: String, range: ClosedFloatingPointRange<Float>, default: Float) = Param(key, key, range, default)

private fun pd(key: String, range: ClosedFloatingPointRange<Float>, default: Dp) =
    Param(key, "$key dp", range, default.value)

private fun paperParams(s: PaperSpec) = listOf(
    p("wear", 0f..1f, s.wear), p("edgeWear", 0f..1.5f, s.edgeWear),
    pd("edgeBand", 0f..40f, s.edgeBand), pd("chipSize", 0.5f..16f, s.chipSize),
    p("chipCoverage", 0f..1f, s.chipCoverage), p("innerWear", 0f..0.5f, s.innerWear),
    p("maxLoss", 0f..1f, s.maxLoss), p("scuffTint", 0f..1f, s.scuffTint),
    pd("mottleSize", 4f..80f, s.mottleSize), p("mottleStrength", 0f..1f, s.mottleStrength),
    pd("grainSize", 0.2f..3f, s.grainSize), p("grainStrength", 0f..0.4f, s.grainStrength),
    p("colorGrain", 0f..0.1f, s.colorGrain), p("toothStrength", 0f..0.2f, s.toothStrength),
    p("speckleStrength", 0f..0.4f, s.speckleStrength), p("edgeShade", 0f..0.4f, s.edgeShade),
    p("pressShade", 0f..0.4f, s.pressShade), pd("sinkBand", 0f..20f, s.sinkBand),
    p("sinkShade", 0f..0.6f, s.sinkShade), pd("inkWidth", 0f..6f, s.inkWidth),
    pd("inkGrainSize", 4f..60f, s.inkGrainSize), p("inkMinAlpha", 0f..1f, s.inkMinAlpha),
    p("inkContrast", 0.5f..4f, s.inkContrast),
)

private fun roughParams(): List<Param> {
    val r = RoughStyle()
    return listOf(
        pd("roughness", 0f..4f, r.roughness),
        p("irregularity", 0f..1f, r.irregularity),
        pd("corner", 0f..30f, r.corner),
        p("tornChance", 0f..1f, r.tornChance),
        p("jerkChance", 0f..1f, r.jerkChance),
        p("press", 0f..1f, 0f),
    )
}

private fun paperSpec(v: Values, base: Color) = PaperSpec(
    base = base,
    scuff = lerp(base, if (base.luminance() < 0.3f) Color.White else Color.Black, 0.14f),
    ink = FeldPalette.Line,
    wear = v["wear"], edgeWear = v["edgeWear"], edgeBand = v.dp("edgeBand"),
    chipSize = v.dp("chipSize"), chipCoverage = v["chipCoverage"], innerWear = v["innerWear"],
    maxLoss = v["maxLoss"], scuffTint = v["scuffTint"], mottleSize = v.dp("mottleSize"),
    mottleStrength = v["mottleStrength"], grainSize = v.dp("grainSize"),
    grainStrength = v["grainStrength"], colorGrain = v["colorGrain"],
    toothStrength = v["toothStrength"], speckleStrength = v["speckleStrength"],
    edgeShade = v["edgeShade"], pressShade = v["pressShade"], sinkBand = v.dp("sinkBand"),
    sinkShade = v["sinkShade"], inkWidth = v.dp("inkWidth"), inkGrainSize = v.dp("inkGrainSize"),
    inkMinAlpha = v["inkMinAlpha"], inkContrast = v["inkContrast"],
)

private fun roughShape(v: Values, seed: Int) = RoughShape(
    seed,
    RoughStyle(
        roughness = v.dp("roughness"),
        irregularity = v["irregularity"],
        corner = v.dp("corner"),
        tornChance = v["tornChance"],
        jerkChance = v["jerkChance"],
    ),
)

private fun dryParams(d: DryPaperSpec) = listOf(
    pd("tearDepth", 0f..8f, d.tearDepth), pd("tearScale", 2f..60f, d.tearScale),
    p("tearCoverage", 0f..1f, d.tearCoverage), pd("bow", 0f..6f, d.bow),
    pd("crackScale", 4f..60f, d.crackScale), pd("crackDepth", 0f..50f, d.crackDepth),
    pd("crackWidth", 0.2f..4f, d.crackWidth), p("crackStrength", 0f..1f, d.crackStrength),
    p("crackCoverage", 0f..1f, d.crackCoverage), pd("grainScale", 0.3f..4f, d.grainScale),
    p("grainStrength", 0f..0.3f, d.grainStrength), p("fiberStrength", 0f..0.3f, d.fiberStrength),
    pd("mottleScale", 6f..100f, d.mottleScale), p("mottleStrength", 0f..0.5f, d.mottleStrength),
    p("pressShade", 0f..0.4f, d.pressShade), p("press", 0f..1f, 0f),
)

private fun tapeParams(t: StickerTapeSpec) = listOf(
    pd("tearAmp", 0f..20f, t.tearAmp), pd("tearScale", 4f..60f, t.tearScale),
    pd("slantLeft", -30f..30f, t.slantLeft), pd("slantRight", -30f..30f, t.slantRight),
    pd("fray", 0f..6f, t.fray), pd("rimWidth", 0f..8f, t.rimWidth), pd("cutAmp", 0f..4f, t.cutAmp),
    pd("stainScale", 10f..100f, t.stainScale), p("stainStrength", 0f..1f, t.stainStrength),
    pd("mottleScale", 6f..80f, t.mottleScale), p("mottleStrength", 0f..0.3f, t.mottleStrength),
    p("fiberStrength", 0f..0.3f, t.fiberStrength), p("grainStrength", 0f..0.2f, t.grainStrength),
    p("speckStrength", 0f..1f, t.speckStrength), p("ageStrength", 0f..1f, t.ageStrength),
    pd("cornerRx", 0f..80f, t.cornerRx), pd("cornerRy", 0f..60f, t.cornerRy),
    pd("layerZone", 0f..120f, t.layerZone), pd("layerAmp", 0f..16f, t.layerAmp),
    pd("layerScale", 4f..40f, t.layerScale), p("layerStrength", 0f..1f, t.layerStrength),
)

private fun markerParams(m: MarkerFilmSpec) = listOf(
    p("coverage", 0f..1f, m.coverage), p("unevenness", 0f..1f, m.unevenness),
    pd("blotchSize", 4f..80f, m.blotchSize), p("poolEdge", 0f..0.5f, m.poolEdge),
    pd("edgeSoftness", 0.2f..4f, m.edgeSoftness), p("erode", 0f..1.5f, m.erode),
    pd("erodeSize", 0.5f..8f, m.erodeSize), pd("wobble", 0f..2f, m.wobble),
    p("streak", 0f..1f, m.streak), pd("streakLength", 4f..80f, m.streakLength),
    pd("streakWidth", 0.2f..3f, m.streakWidth), p("streakAngle", -45f..45f, m.streakAngle),
    p("skip", 0f..1f, m.skip), p("skipCoverage", 0f..1f, m.skipCoverage),
    pd("skipSize", 6f..80f, m.skipSize), p("pinhole", 0f..1f, m.pinhole),
    p("grain", 0f..0.5f, m.grain), pd("grainSize", 0.2f..3f, m.grainSize),
    p("smudge", 0f..0.6f, m.smudge), pd("smudgeSize", 10f..120f, m.smudgeSize),
    pd("smudgeRadius", 0.5f..8f, m.smudgeRadius), p("speck", 0f..1f, m.speck),
)

private fun distressParams(d: DistressSpec) = listOf(
    p("strength", 0f..2f, d.strength), pd("edgeSoftness", 0.1f..3f, d.edgeSoftness),
    pd("edgeRough", 0f..2f, d.edgeRough), pd("edgeSize", 0.3f..8f, d.edgeSize),
    p("edgeErode", 0f..1.5f, d.edgeErode), p("bleed", 0f..0.6f, d.bleed),
    p("patchy", 0f..1f, d.patchy), pd("patchSize", 6f..100f, d.patchSize),
    p("dropout", 0f..1.5f, d.dropout), pd("dropoutSize", 1f..30f, d.dropoutSize),
    p("scratch", 0f..1.5f, d.scratch), pd("scratchLength", 6f..120f, d.scratchLength),
    pd("scratchWidth", 0.1f..2f, d.scratchWidth), p("scratchAngle", -90f..90f, d.scratchAngle),
    p("speck", 0f..1.5f, d.speck), pd("speckSize", 0.5f..8f, d.speckSize),
    p("grain", 0f..1f, d.grain), pd("grainSize", 0.2f..3f, d.grainSize),
    p("splatter", 0f..1f, d.splatter), pd("splatterRadius", 0.5f..8f, d.splatterRadius),
)

private fun Modifier.segmentPaintDemo(base: Color, smear: Float, seed: Int): Modifier = drawWithCache {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val s = RuntimeShader(SEGMENT_PAINT_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("u", density)
            setFloatUniform("smear", smear)
            setColorUniform("baseColor", base.toArgb())
            setColorUniform("deepColor", lerp(base, Color.Black, 0.45f).toArgb())
            setColorUniform("lightColor", lerp(base, Color.White, 0.30f).toArgb())
            setColorUniform("holeColor", Bg.toArgb())
        }
        val b = ShaderBrush(s)
        onDrawWithContent {
            drawRect(b)
            drawContent()
        }
    } else {
        onDrawWithContent {
            drawRect(base)
            drawContent()
        }
    }
}

private fun Modifier.sliderTrackDemo(fill: Color, seed: Int, progress: Float, fadeW: Dp): Modifier = drawWithCache {
    val thumbW = 26.dp.toPx()
    val fillX = thumbW / 2f + progress * (size.width - thumbW)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val s = RuntimeShader(SLIDER_TRACK_AGSL).apply {
            setFloatUniform("size", size.width, size.height)
            setFloatUniform("seed", seed.mod(97).toFloat())
            setFloatUniform("u", density)
            setFloatUniform("fillX", fillX)
            setFloatUniform("fadeW", fadeW.toPx())
            setColorUniform("fillColor", fill.toArgb())
            setColorUniform("offColor", Color(0xFF1F2322).toArgb())
        }
        val b = ShaderBrush(s)
        onDrawWithContent { drawRect(b) }
    } else {
        onDrawWithContent {
            drawRoundRect(Color(0xFF1F2322), cornerRadius = CornerRadius(size.height / 2f))
        }
    }
}

private fun Modifier.leakDemo(color: Color, strength: Float, right: Boolean, seed: Int): Modifier = drawWithCache {
    val leak = Leak(size, right, color, strength, seed.mod(97).toFloat())
    onDrawWithContent {
        with(leak) { draw() }
        drawContent()
    }
}

private val DarkSw = listOf(
    Swatch("panel", Color(0xFF0C1212)),
    Swatch("cream", FeldPalette.Cream),
    Swatch("rust", Color(0xFFE45720)),
    Swatch("teal", Teal),
)
private val InkSw = listOf(
    Swatch("chalk", Color(0xFFEDE9DD)),
    Swatch("ink", Color(0xFF14110F)),
    Swatch("orange", FeldPalette.Orange),
    Swatch("teal", Teal),
)
private val BrushSw = listOf(
    Swatch("teal", Teal),
    Swatch("orange", FeldPalette.Orange),
    Swatch("cream", FeldPalette.Cream),
    Swatch("purple", Color(0xFF8F6FD6)),
)

@Composable
private fun Caption(text: String) =
    Text(text, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = FeldPalette.Muted)

private fun buildDemos(): List<ShaderDemo> {
    val panel = FeldPaper.Panel
    val dry = DryPaperSpec(base = Color.White, crack = Color.Black)
    val tape = FeldTape.Cream
    val marker = MarkerFilmSpec()
    val distress = DistressSpec()
    val paperAll = paperParams(panel) + roughParams()

    return listOf(
        ShaderDemo(
            id = "paper",
            title = "PAPER_AGSL",
            fn = "paperMaterial",
            family = "SURFACE",
            note = "Main panel: worn edges, stains, grain, ink outline. Base for cards and buttons",
            params = paperAll,
            swatches = DarkSw,
        ) { v, c, seed, m ->
            Box(
                m.padding(18.dp).paperMaterial(roughShape(v, seed), paperSpec(v, c), press = { v["press"] }),
                contentAlignment = Alignment.Center,
            ) { Caption("PANEL") }
        },

        ShaderDemo(
            id = "ink",
            title = "INK_AGSL",
            fn = "paperMaterial (ink edge)",
            family = "SURFACE",
            note = "Ragged ink outline only",
            params = (paperParams(panel).filter { it.key.startsWith("ink") } + roughParams())
                .map { if (it.key == "inkWidth") it.copy(default = 2f) else it },
            swatches = listOf(Swatch("panel", Color(0xFF0C1212))),
            fixed = (paperParams(FeldPaper.flat(panel.base)) + roughParams()).associate { it.key to it.default },
        ) { v, c, seed, m ->
            Box(
                m.padding(18.dp).paperMaterial(roughShape(v, seed), paperSpec(v, c)),
                contentAlignment = Alignment.Center,
            ) { Caption("INK EDGE") }
        },

        ShaderDemo(
            id = "dry",
            title = "DRY_PAPER_AGSL",
            fn = "dryPaperMaterial",
            family = "SURFACE",
            note = "Cracked dry paper for cream/rust buttons. Shares grain, mottling, and fibers with PAPER, i don't like it",
            params = dryParams(dry),
            swatches = listOf(Swatch("cream", FeldPalette.Cream), Swatch("rust", Color(0xFFE45720))),
        ) { v, c, seed, m ->
            val spec = DryPaperSpec(
                base = c, crack = lerp(c, Color.Black, 0.62f),
                tearDepth = v.dp("tearDepth"), tearScale = v.dp("tearScale"),
                tearCoverage = v["tearCoverage"], bow = v.dp("bow"), crackScale = v.dp("crackScale"),
                crackDepth = v.dp("crackDepth"), crackWidth = v.dp("crackWidth"),
                crackStrength = v["crackStrength"], crackCoverage = v["crackCoverage"],
                grainScale = v.dp("grainScale"), grainStrength = v["grainStrength"],
                fiberStrength = v["fiberStrength"], mottleScale = v.dp("mottleScale"),
                mottleStrength = v["mottleStrength"], pressShade = v["pressShade"],
            )
            Box(
                m.padding(22.dp).dryPaperMaterial(spec, seed, press = { v["press"] }),
                contentAlignment = Alignment.Center,
            ) {
                Text("DRY", style = MaterialTheme.typography.titleLarge, color = FeldPalette.Ink)
            }
        },

        ShaderDemo(
            id = "tape",
            title = "STICKER_TAPE_AGSL",
            fn = "stickerTapeMaterial",
            family = "SURFACE",
            note = "Torn tape with a layered corner. Reusable torn edges",
            params = tapeParams(tape),
            swatches = listOf(
                Swatch("cream", tape.base),
                Swatch("grey", Color(0xFFB9B4A8)),
                Swatch("rust", Color(0xFFC77B4A)),
            ),
        ) { v, c, seed, m ->
            val spec = StickerTapeSpec(
                base = c, stain = lerp(c, Color.Black, 0.4f), rim = lerp(c, Color.White, 0.4f),
                tearAmp = v.dp("tearAmp"), tearScale = v.dp("tearScale"),
                slantLeft = v.dp("slantLeft"), slantRight = v.dp("slantRight"),
                fray = v.dp("fray"), rimWidth = v.dp("rimWidth"), cutAmp = v.dp("cutAmp"),
                stainScale = v.dp("stainScale"), stainStrength = v["stainStrength"],
                mottleScale = v.dp("mottleScale"), mottleStrength = v["mottleStrength"],
                fiberStrength = v["fiberStrength"], grainStrength = v["grainStrength"],
                speckStrength = v["speckStrength"], ageStrength = v["ageStrength"],
                cornerRx = v.dp("cornerRx"), cornerRy = v.dp("cornerRy"),
                layerZone = v.dp("layerZone"), layerAmp = v.dp("layerAmp"),
                layerScale = v.dp("layerScale"), layerStrength = v["layerStrength"],
            )
            Box(m, contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxWidth().height(
                        96.dp,
                    ).stickerTapeMaterial(spec, seed).padding(horizontal = spec.edgeInset),
                    contentAlignment = Alignment.Center,
                ) { Text("TAPE", style = MaterialTheme.typography.titleLarge, color = FeldPalette.Ink) }
            }
        },

        ShaderDemo(
            id = "segment",
            title = "SEGMENT_PAINT_AGSL",
            fn = "segmentPaint (local wrapper)",
            family = "BRUSH",
            note = "Chippinkossed paint stripe. WORN + ACCENT in a stripe shape",
            params = listOf(p("smear", -1f..1f, 0f)),
            swatches = BrushSw,
            tileHeight = 110.dp,
        ) { v, c, seed, m ->
            Box(m, contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxWidth(0.82f).height(44.dp).segmentPaintDemo(c, v["smear"], seed))
            }
        },

        ShaderDemo(
            id = "track",
            title = "SLIDER_TRACK_AGSL",
            fn = "sliderTrack (local wrapper)",
            family = "BRUSH",
            note = "Slider track: textured fill on dark stone. Basically ACCENT + grain",
            params = listOf(
                p("progress", 0f..1f, 0.6f),
                pd("fadeW", 0f..60f, 18.dp),
                pd("trackHeight", 4f..28f, 10.dp),
            ),
            swatches = BrushSw,
            tileHeight = 100.dp,
        ) { v, c, seed, m ->
            Box(m, contentAlignment = Alignment.Center) {
                Box(
                    Modifier.fillMaxWidth().padding(
                        horizontal = 16.dp,
                    ).height(v.dp("trackHeight")).sliderTrackDemo(c, seed, v["progress"], v.dp("fadeW")),
                )
            }
        },

        ShaderDemo(
            id = "leak",
            title = "LEAK_AGSL",
            fn = "Leak (decal)",
            family = "OVERLAY",
            note = "Edge light leak for decals. Cheap and standalone",
            params = listOf(p("strength", 0f..0.5f, 0.13f), p("right (0/1)", 0f..1f, 1f)),
            swatches = listOf(
                Swatch("orange", FeldPalette.Orange),
                Swatch("red", Color(0xFFD0422A)),
                Swatch("cream", FeldPalette.Cream),
            ),
        ) { v, c, seed, m ->
            Box(
                m.padding(
                    14.dp,
                ).background(Color(0xFF0C1212)).leakDemo(c, v["strength"], v["right (0/1)"] > 0.5f, seed),
                contentAlignment = Alignment.Center,
            ) {
                Caption("LEAK")
            }
        },

        ShaderDemo(
            id = "marker",
            title = "MARKER_FILM_AGSL",
            fn = "markerFilmMaterial",
            family = "INK MASK",
            note = "Live marker texture applied to content alpha (icons, text). Mostly overlaps with DISTRESS.",
            params = markerParams(marker),
            swatches = InkSw,
        ) { v, c, seed, m ->
            val spec = MarkerFilmSpec(
                color = c, coverage = v["coverage"], unevenness = v["unevenness"],
                blotchSize = v.dp("blotchSize"), poolEdge = v["poolEdge"],
                edgeSoftness = v.dp("edgeSoftness"), erode = v["erode"], erodeSize = v.dp("erodeSize"),
                wobble = v.dp("wobble"), streak = v["streak"], streakLength = v.dp("streakLength"),
                streakWidth = v.dp("streakWidth"), streakAngle = v["streakAngle"], skip = v["skip"],
                skipCoverage = v["skipCoverage"], skipSize = v.dp("skipSize"), pinhole = v["pinhole"],
                grain = v["grain"], grainSize = v.dp("grainSize"), smudge = v["smudge"],
                smudgeSize = v.dp("smudgeSize"), smudgeRadius = v.dp("smudgeRadius"), speck = v["speck"],
            )
            Box(m, contentAlignment = Alignment.Center) {
                Row(
                    Modifier.markerFilmMaterial(spec, seed, bake = false).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("FELD", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    FeldIcon(FeldIconId.Play, tint = Color.White, size = 30.dp)
                    FeldIcon(FeldIconId.Settings, tint = Color.White, size = 30.dp)
                }
            }
        },

        ShaderDemo(
            id = "distress",
            title = "DISTRESS_AGSL",
            fn = "distressMaterial",
            family = "INK MASK",
            note = "Distress texture for stamps and headlines. Like MARKER FILM, but with scratches and dropouts",
            params = distressParams(distress),
            swatches = listOf(
                Swatch("as text", Color.Unspecified),
                Swatch("cream", FeldPalette.Cream),
                Swatch("orange", FeldPalette.Orange),
            ),
        ) { v, c, seed, m ->
            val spec = DistressSpec(
                color = c, strength = v["strength"], edgeSoftness = v.dp("edgeSoftness"),
                edgeRough = v.dp("edgeRough"), edgeSize = v.dp("edgeSize"), edgeErode = v["edgeErode"],
                bleed = v["bleed"], patchy = v["patchy"], patchSize = v.dp("patchSize"),
                dropout = v["dropout"], dropoutSize = v.dp("dropoutSize"), scratch = v["scratch"],
                scratchLength = v.dp("scratchLength"), scratchWidth = v.dp("scratchWidth"),
                scratchAngle = v["scratchAngle"], speck = v["speck"], speckSize = v.dp("speckSize"),
                grain = v["grain"], grainSize = v.dp("grainSize"), splatter = v["splatter"],
                splatterRadius = v.dp("splatterRadius"),
            )
            Box(m, contentAlignment = Alignment.Center) {
                Text(
                    "DISTRESS 07",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    modifier = Modifier.distressMaterial(spec, seed).padding(12.dp),
                )
            }
        },

        ShaderDemo(
            id = "accent",
            title = "ACCENT_AGSL",
            fn = "accentBrush",
            family = "BRUSH",
            note = "Live fill with faded, glowing patches",
            params = listOf(
                p("fade", 0f..1.5f, 0.9f),
                p("glow", 0f..1.5f, 0.9f),
                p("blotch", 0f..0.3f, 0f),
                p("grain", 0f..0.3f, 0.07f),
            ),
            swatches = BrushSw,
        ) { v, c, seed, m ->
            val d = LocalDensity.current.density
            val brush = accentBrush(c, seed, d, v["fade"], v["glow"], v["blotch"], v["grain"])
            Box(m.padding(18.dp).clip(RoundedCornerShape(6.dp)).background(brush))
        },

        ShaderDemo(
            id = "worn",
            title = "WORN_AGSL",
            fn = "wornBrush",
            family = "BRUSH",
            note = "Chippik fill for diamonds and speaker bars. Similar to SEGMENT PAINT",
            params = listOf(p("wear", 0f..1f, 0.5f), p("scale", 4f..80f, 26f)),
            swatches = BrushSw,
        ) { v, c, seed, m ->
            val d = LocalDensity.current.density
            val brush = wornBrush(c, seed, d, v["wear"], v["scale"])
            Box(m.padding(18.dp).clip(RoundedCornerShape(6.dp)).background(brush))
        },
    )
}

@Composable
fun FeldShaderPlayground(modifier: Modifier = Modifier) {
    val demos = remember { buildDemos() }
    val values = remember {
        demos.associate { d ->
            d.id to mutableStateMapOf<String, Float>().apply {
                putAll(d.fixed)
                d.params.forEach { put(it.key, it.default) }
            }
        }
    }
    val colorIdx = remember { mutableStateMapOf<String, Int>() }
    val seeds = remember { mutableStateMapOf<String, Int>() }

    var grid by remember { mutableStateOf(true) }
    var index by remember { mutableIntStateOf(0) }
    var panelOpen by remember { mutableStateOf(true) }

    fun colorOf(d: ShaderDemo) = d.swatches[(colorIdx[d.id] ?: 0).coerceIn(0, d.swatches.lastIndex)].color

    Column(modifier.fillMaxSize().background(Bg).statusBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("SHADERS", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(Modifier.weight(1f))
            Text(
                if (grid) "ALL" else "ONE + TUNE",
                style = MaterialTheme.typography.labelSmall,
                color = FeldPalette.Muted,
            )
            Spacer(Modifier.width(10.dp))
            FeldSwitch(grid, { grid = it })
        }

        if (grid) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                itemsIndexed(demos, key = { _, d -> d.id }) { i, d ->
                    Column(
                        Modifier.clickable {
                            index = i
                            grid = false
                        },
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(
                                d.tileHeight,
                            ).clip(RoundedCornerShape(4.dp)).background(TileBg),
                        ) {
                            d.content(
                                Values(values.getValue(d.id)),
                                colorOf(d),
                                seeds[d.id] ?: 1,
                                Modifier.fillMaxSize(),
                            )
                        }
                        Text(
                            d.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        Text(
                            "${d.family} · ${d.fn}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FeldPalette.Muted,
                        )
                    }
                }
            }
        } else {
            val d = demos[index]
            val vm = values.getValue(d.id)
            val seed = seeds[d.id] ?: 1
            val clipboard = LocalClipboardManager.current

            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.graphicsLayer { rotationZ = 180f }) {
                    FeldIconButton(FeldIconId.Chevron, { index = (index - 1 + demos.size) % demos.size })
                }
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(d.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(
                        "${index + 1}/${demos.size} · ${d.family} · ${d.fn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = FeldPalette.Muted,
                    )
                }
                FeldIconButton(FeldIconId.Chevron, { index = (index + 1) % demos.size })
            }
            Text(
                d.note,
                style = MaterialTheme.typography.bodySmall,
                color = FeldPalette.Muted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )

            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(4.dp)).background(TileBg),
            ) { d.content(Values(vm), colorOf(d), seed, Modifier.fillMaxSize()) }

            Row(
                Modifier.fillMaxWidth().clickable { panelOpen = !panelOpen }.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("PARAMS ${d.params.size}", style = MaterialTheme.typography.labelMedium, color = FeldPalette.Cream)
                Spacer(Modifier.weight(1f))
                Text(if (panelOpen) "▼" else "▲", color = FeldPalette.Muted)
            }

            AnimatedVisibility(panelOpen) {
                Column {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton({
                            d.params.forEach { vm[it.key] = it.default }
                            colorIdx[d.id] = 0
                            seeds[d.id] = 1
                        }) { Text("RESET") }
                        OutlinedButton({ seeds[d.id] = seed - 1 }) { Text("seed −") }
                        Text("$seed", color = Color.White)
                        OutlinedButton({ seeds[d.id] = seed + 1 }) { Text("seed +") }
                        OutlinedButton({
                            val text = buildString {
                                appendLine(
                                    "// ${d.title}, color=${d.swatches.getOrNull(
                                        colorIdx[d.id] ?: 0,
                                    )?.name}, seed=$seed",
                                )
                                d.params.forEach {
                                    appendLine(
                                        "${it.key} = ${"%.3f".format(vm[it.key] ?: it.default)}",
                                    )
                                }
                            }
                            clipboard.setText(AnnotatedString(text))
                        }) { Text("COPY") }
                    }

                    if (d.swatches.size > 1) {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            d.swatches.forEachIndexed { i, s ->
                                FeldChip(
                                    text = s.name,
                                    selected = (colorIdx[d.id] ?: 0) == i,
                                    onClick = { colorIdx[d.id] = i },
                                )
                            }
                        }
                    }
                    LazyColumn(
                        Modifier.heightIn(max = 300.dp).padding(horizontal = 16.dp),
                    ) {
                        items(d.params, key = { it.key }) { prm ->
                            FeldSlider(
                                value = vm[prm.key] ?: prm.default,
                                onValueChange = { vm[prm.key] = it },
                                valueRange = prm.range,
                                label = prm.label,
                                valueText = { "%.2f".format(it) },
                                accent = FeldSliderColors.Cream,
                                modifier = Modifier.padding(vertical = 5.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 380, heightDp = 800, showBackground = true, backgroundColor = 0xFF0B1011)
@Composable
private fun ShaderPlaygroundPreview() = DiscoDialogueTheme { FeldShaderPlayground() }
