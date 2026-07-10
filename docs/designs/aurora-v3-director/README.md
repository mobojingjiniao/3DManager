# 3DManager v3 — Cinematic Dark Aurora · Director's Cut

**Version**: v3.1 · 2026-07-10
**Aesthetic**: Cinematic Dark Aurora — Director's Cut
**Mood**: Cinematic · Editorial · Museum-grade

## What's different from v3

The original v3 designs were functional but generic. The Director's Cut treats each screen as a **movie poster / museum exhibition / film contact sheet**:

| Element | v3 (generic) | v3.1 (Director's Cut) |
|---|---|---|
| App icon | Circle + "3D" monogram | Luminous particle silhouette w/ chromatic aberration |
| Splash | Aurora ring + name | Movie title sequence w/ letterbox, crop marks, italic wordmark |
| Avatar library | Grid + filter chips | "Specimen Index" museum catalog w/ hero shot + 4-row archive |
| Avatar detail | Viewer + chips + buttons | Full-bleed cinematic portrait + museum placard |
| Empty state | Breathing orb + buttons | "Stage is being prepared" w/ scattered seeds + poetic type |
| Typography | Space Grotesk + Inter | + **Playfair Display Italic** for editorial titles |
| Color palette | void / green / violet | + **Amber** `#FFB347` for warm light leaks · + **Cream** `#F4EDE4` for museum labels |
| Layout | Symmetric | **Asymmetric** — content on left, hero on right |
| Visual effects | Aurora mesh | + Film grain · + Chromatic aberration · + Crop marks · + Letterbox bars · + Light leaks |
| Chrome | Tab bar at bottom | Minimal icon-only nav · editorial caption style |

## Files

| # | File | Type | Dimensions |
|---|---|---|---|
| 01 | `01-app-icon-square.svg` | Adaptive icon (square) | 1080×1080 |
| 02 | `02-app-icon-circle.svg` | Adaptive icon (circle) | 1080×1080 |
| 03 | `03-splash.svg` | Splash — movie title sequence | 1080×2400 |
| 08 | `08-avatar-library.svg` | Specimen Index (museum catalog) | 1080×2400 |
| 09 | `09-avatar-library-empty.svg` | "Stage is being prepared" | 1080×2400 |
| 10 | `10-avatar-detail.svg` | Full-bleed cinematic portrait | 1080×2400 |

## Design Tokens (extended)

### Colors (additions)
- **Amber**: `#FFB347` — light leaks, warm accents, "captured" stamp
- **Magenta**: `#FF5BD6` — chromatic aberration channel, accent
- **Cream**: `#F4EDE4` — museum labels, editorial text

### Typography
- **Space Grotesk** 700/900 — display (numeric stamps)
- **Playfair Display Italic** 900 — editorial titles (italic, dramatic)
- **Inter** 400/500/600 — body
- **JetBrains Mono** 400 — telemetry, captions, specimen numbers

### Visual Effects (signature)
- **Letterbox bars**: top 140dp, bottom 220-280dp (cinema aspect)
- **Crop marks**: 4 corners, 40dp inset (print design)
- **Film grain**: SVG `feTurbulence` overlay at 4-6% opacity
- **Chromatic aberration**: 3-channel particle offset (R -22px, G 0, B +22px)
- **Light leaks**: linear gradient from top-right, warm amber → transparent
- **Specimen numbers**: 280sp Space Grotesk as ghost background watermark

## How to Use

1. **Icon** is the brand mark — use it everywhere
2. **Specimen numbering** (AVT-001 etc.) carries through library → detail → share
3. **Italic Playfair** titles signal "important moment"; never use for body
4. **Cream `#F4EDE4`** is the editorial white — for text on dark
5. **Amber light leaks** should appear only on hero moments (splash, detail, empty state)

## Companion Docs
- `docs/designs/aurora-v3/` — original v3 designs (still valid as lower-fidelity fallback)
- `/home/mobo/.claude/plans/avatar-uiux-uiue-playful-crown.md` — master plan
