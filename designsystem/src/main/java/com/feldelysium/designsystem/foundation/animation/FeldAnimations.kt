package com.feldelysium.designsystem.foundation.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlin.random.Random

@Immutable
internal data class FeldAnimationValues(val values: Map<String, Float>) {
    operator fun get(key: String): Float = values[key] ?: 0f

    fun getOrDefault(key: String, default: Float): Float = values[key] ?: default

    companion object {
        operator fun invoke(vararg values: Pair<String, Float>): FeldAnimationValues =
            FeldAnimationValues(values.toMap())
    }
}

@Immutable
internal data class FeldAnimationState(val values: Map<String, Float>) {
    operator fun get(key: String): Float = values[key] ?: 0f

    fun getOrDefault(key: String, default: Float): Float = values[key] ?: default
}

@Stable
internal fun interface FeldAnimation {
    @Composable
    operator fun invoke(values: FeldAnimationValues): FeldAnimationState
}

@Stable
internal fun interface FeldInteractionAnimation {
    @Composable
    operator fun invoke(values: FeldAnimationValues, active: Boolean): FeldAnimationState
}

@Composable
internal fun feldAnimate(values: FeldAnimationValues, animation: FeldAnimation): FeldAnimationState = animation(values)

@Composable
internal fun feldAnimate(
    values: FeldAnimationValues,
    animation: FeldInteractionAnimation,
    active: Boolean,
): FeldAnimationState = animation(values, active)

internal object FeldAnimations {

    object Reveal {

        fun from(
            initial: Map<String, (Float) -> Float>,
            enabled: Boolean = true,
            durationMillis: Int = 700,
            easing: Easing = RevealEasing,
        ): FeldAnimation = FeldAnimation { values ->

            val progress = remember {
                Animatable(if (enabled) 0f else 1f)
            }

            LaunchedEffect(enabled) {
                if (enabled) {
                    progress.snapTo(0f)

                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = easing,
                        ),
                    )
                } else {
                    progress.snapTo(1f)
                }
            }

            val animated = values.values.mapValues { (key, target) ->
                val start = initial[key]?.invoke(target) ?: target

                lerp(
                    start = start,
                    end = target,
                    fraction = progress.value,
                )
            }

            FeldAnimationState(animated)
        }

        fun random(
            enabled: Boolean = true,
            multiplier: ClosedFloatingPointRange<Float> = 0.85f..1.5f,
            durationMillis: Int = 700,
            easing: Easing = RevealEasing,
            seed: Int? = null,
        ): FeldAnimation = FeldAnimation { values ->

            val random = remember(seed, values) {
                Random(seed ?: values.values.hashCode())
            }

            val factors = remember(values, multiplier, seed) {
                values.values.keys.associateWith {
                    multiplier.start +
                        random.nextFloat() *
                        (multiplier.endInclusive - multiplier.start)
                }
            }

            val progress = remember {
                Animatable(if (enabled) 0f else 1f)
            }

            LaunchedEffect(enabled) {
                if (enabled) {
                    progress.snapTo(0f)

                    progress.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = easing,
                        ),
                    )
                } else {
                    progress.snapTo(1f)
                }
            }

            val animated = values.values.mapValues { (key, target) ->
                val start = target * (factors[key] ?: 1f)

                lerp(
                    start = start,
                    end = target,
                    fraction = progress.value,
                )
            }

            FeldAnimationState(animated)
        }
    }

    object Hover {

        fun values(
            targets: Map<String, (Float) -> Float>,
            durationMillis: Int = 160,
            easing: Easing = FastOutSlowInEasing,
        ): FeldInteractionAnimation = FeldInteractionAnimation { values, active ->

            val progress by animateFloatAsState(
                targetValue = if (active) 1f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = easing,
                ),
                label = "feldHover",
            )

            val animated = values.values.mapValues { (key, value) ->
                val target = targets[key]?.invoke(value) ?: value

                lerp(
                    start = value,
                    end = target,
                    fraction = progress,
                )
            }

            FeldAnimationState(animated)
        }
    }

    object Press {

        fun values(
            targets: Map<String, (Float) -> Float>,
            durationMillis: Int = 90,
            easing: Easing = FastOutSlowInEasing,
        ): FeldInteractionAnimation = FeldInteractionAnimation { values, active ->

            val progress by animateFloatAsState(
                targetValue = if (active) 1f else 0f,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = easing,
                ),
                label = "feldPress",
            )

            val animated = values.values.mapValues { (key, value) ->
                val target = targets[key]?.invoke(value) ?: value

                lerp(
                    start = value,
                    end = target,
                    fraction = progress,
                )
            }

            FeldAnimationState(animated)
        }
    }

    object Change {

        fun smooth(durationMillis: Int = 220, easing: Easing = FastOutSlowInEasing): FeldAnimation =
            FeldAnimation { values ->

                val animated = values.values.mapValues { (_, value) ->

                    val animatedValue by animateFloatAsState(
                        targetValue = value,
                        animationSpec = tween(
                            durationMillis = durationMillis,
                            easing = easing,
                        ),
                        label = "feldChange",
                    )

                    animatedValue
                }

                FeldAnimationState(animated)
            }
    }

    object SpringAnim {

        fun soft(
            stiffness: Float = Spring.StiffnessLow,
            dampingRatio: Float = Spring.DampingRatioNoBouncy,
        ): FeldAnimation = FeldAnimation { values ->

            val animated = values.values.mapValues { (_, value) ->

                val animatedValue by animateFloatAsState(
                    targetValue = value,
                    animationSpec = spring(
                        stiffness = stiffness,
                        dampingRatio = dampingRatio,
                    ),
                    label = "feldSpring",
                )

                animatedValue
            }

            FeldAnimationState(animated)
        }
    }
}

private val RevealEasing = FastOutSlowInEasing

private fun lerp(start: Float, end: Float, fraction: Float): Float = start + (end - start) * fraction
