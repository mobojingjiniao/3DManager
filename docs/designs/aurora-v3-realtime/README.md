# 3DManager v3.2 — Cinematic Dark Aurora · Realtime HUD

**Version**: v3.2 · 2026-07-10
**Aesthetic**: Cinematic Dark Aurora — Realtime HUD
**Mood**: Tech-forward · Consumer product · Information-dense

## What's different from v3.1 (Director's Cut)

The Director's Cut went too editorial / art-gallery. v3.2 brings it back to **product**:

| Element | v3.1 (Director's Cut) | v3.2 (Realtime HUD) |
|---|---|---|
| **Mood** | Museum / exhibition / movie poster | Real product · terminal-grade · HUD |
| **Copy** | "The stage is being prepared" (poetic) | "No avatars yet" (clear, direct) |
| **Top bar** | Letterbox + crop marks + reel/take | Standard app bar: ← + title + actions |
| **Bottom nav** | Minimal icons + editorial captions | Standard nav with text labels |
| **Chrome** | Custom specimen language | Standard product chrome (chips, pills, CTAs) |
| **Empty state** | Scattered seeds + 4-line italic poem | Concentric scan rings + clear CTA buttons |
| **Telemetry** | Hidden in corners | Visible FPS / splat count / tier readout |
| **Avatar cards** | Editorial rows with italic names | Real cards: thumbnail + name + meta + chip + arrow |
| **Viewer** | Full-bleed cinematic portrait | Boxed viewer w/ orbit compass + control buttons |
| **Typography** | Playfair Display Italic | Removed (too magazine) — back to Space Grotesk + Inter |
| **Color** | Amber light leaks + cream | Kept but more restrained |

## Design principles (v3.2)

1. **Information density** — show real data, not abstract decoration
2. **Functional hierarchy** — primary action obvious, secondary clearly secondary
3. **Tech vocabulary** — FPS, SH, splat count, tier, render mode, paths
4. **Standard chrome** — top bar + filter chips + grid + FAB + bottom nav
5. **Real product** — every screen is something a user can interact with

## Files

| # | File | Type | Dimensions |
|---|---|---|---|
| 01 | `01-app-icon-square.svg` | Adaptive icon | 1080×1080 |
| 02 | `02-app-icon-circle.svg` | Adaptive icon | 1080×1080 |
| 03 | `03-splash.svg` | Product launch screen | 1080×2400 |
| 08 | `08-avatar-library.svg` | Avatar grid (real product) | 1080×2400 |
| 09 | `09-avatar-library-empty.svg` | Empty state with clear CTA | 1080×2400 |
| 10 | `10-avatar-detail.svg` | Viewer with HUD + controls | 1080×2400 |

## Tech HUD elements (always present)

- **FPS readout** — top-right of viewer, mono font, green
- **Splats count** — abbreviated (1.2M, 800K)
- **Tier badge** — HIGH / NORMAL / LOW with color coding
- **Coordinate readouts** — yaw / pitch (optional)
- **Orbit compass** — small ring showing rotation
- **Viewfinder brackets** — corner brackets on viewer
- **Status bar** — time + battery + signal at top
- **Telemetry footer** — render / tier / build / sync

## Companion Docs
- `docs/designs/aurora-v3/` — original v3 (low-fidelity)
- `docs/designs/aurora-v3-director/` — v3.1 Director's Cut (art-house)
- `docs/designs/aurora-v3-realtime/` — v3.2 (current, product-grade)
- `/home/mobo/.claude/plans/avatar-uiux-uiue-playful-crown.md` — master plan
