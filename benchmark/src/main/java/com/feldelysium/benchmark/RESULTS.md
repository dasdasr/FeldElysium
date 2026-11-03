# Benchmark Results

**Device:** Pixel 9 Pro

## Measurement conditions

- Same device and test conditions: charging state, English locale, Record Playback disabled, Tap Anywhere enabled.
- Startup: TTID in milliseconds, median (min–max).
- Frame performance: `frameOverrun` P99 / P95 and `frameDurationCpu` P99, in milliseconds.
- Lower values indicate better performance.

## Results

| Date | Commit | Benchmark | Results | Notes |
|---|---|---|---|---|
| 2026-11-03 | — | Startup · `coldStartNone` | TTID 875 ms (821–893) | Baseline, no compilation optimizations |
| 2026-11-03 | — | Startup · `coldStartPartialNoProfile` | TTID 651 ms (633–681) | `warmupIterations = 3`, no Baseline Profile |
| 2026-11-03 | — | Playback · `playbackTapThrough` | Overrun P99 64.8 / P95 15.7; CPU P99 45.1 | Baseline, visible frame stutters |
| 2026-11-03 | — | Editor Scroll · `scrollEntries` | Overrun P99 25.1 / P95 -0.2; CPU P99 13.4 | Baseline, 80 dialogue entries |
| 2026-11-03 | — | Navigation · `editorToSettingsAndBack` | Overrun P99 172.4 / P95 142.2; CPU P99 110.3 | Baseline, only ~24 frames, one-off hitch on opening Settings |
