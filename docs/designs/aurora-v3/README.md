# 3DManager v3 — Cinematic Dark Aurora · Design Assets

**Version**: v3 · 2026-07-10
**Aesthetic**: Cinematic Dark Aurora
**Palette**: `#06070F` void / `#6EFFB7` aurora green / `#9B5CFF` aurora violet
**Typography**: Space Grotesk (display) · Inter (body) · JetBrains Mono (telemetry)

## File Index

| # | File | Type | Dimensions |
|---|---|---|---|
| 00 | `00-design-system.svg` | Design tokens reference | 1600×2400 |
| 01 | `01-app-icon-square.svg` | Adaptive icon (square) | 1080×1080 |
| 02 | `02-app-icon-circle.svg` | Adaptive icon (circle) | 1080×1080 |
| 03 | `03-splash.svg` | Splash aurora reveal | 1080×2400 |
| 04 | `04-bottom-nav.svg` | 4-tab nav + center FAB | 1200×240 |
| 05 | `05-onboarding-1.svg` | Onboarding: Capture your world | 1080×2400 |
| 06 | `06-onboarding-2.svg` | Onboarding: Make it yours | 1080×2400 |
| 07 | `07-onboarding-3.svg` | Onboarding: Bring it to life | 1080×2400 |
| 08 | `08-avatar-library.svg` | Avatar grid + filter chips | 1080×2400 |
| 09 | `09-avatar-library-empty.svg` | Empty state w/ breath pulse | 1080×2400 |
| 10 | `10-avatar-detail.svg` | 3D viewer + pose picker | 1080×2400 |
| 11 | `11-avatar-capture-capturing.svg` | Camera viewfinder | 1080×2400 |
| 12 | `12-avatar-capture-uploading.svg` | Aurora sweep upload | 1080×2400 |
| 13 | `13-avatar-capture-converting.svg` | Rotating glyph + ETA | 1080×2400 |
| 14 | `14-avatar-import.svg` | SAF picker + recent + cloud | 1080×2400 |
| 15 | `15-avatar-share.svg` | QR + URL + visibility | 1080×2400 |
| 16 | `16-theme-gallery.svg` | Current + carousel + tier | 1080×2400 |
| 17 | `17-wallpaper.svg` | Mini preview + CTAs | 1080×2400 |
| 18 | `18-settings.svg` | All settings sections | 1080×2400 |
| 19 | `19-component-buttons.svg` | AuroraButton variants | 1200×600 |
| 20 | `20-component-cards.svg` | AuroraCard states | 1200×800 |
| 21 | `21-component-chips.svg` | AuroraChip tones | 1200×400 |

## Design Tokens

### Colors
- **Void**: Base `#06070F` · Deep `#03040A` · Raised `#0E1020`
- **Surface**: Glass `rgba(255,255,255,0.04)` · GlassHi `rgba(255,255,255,0.08)` · Border `rgba(255,255,255,0.06)`
- **Aurora**: Green `#6EFFB7` · GreenDim `#3FCC8A` · Violet `#9B5CFF` · VioletDim `#6F46CC` · Magenta `#FF5BD6`
- **Signal**: Coral `#FF6B6B` · Amber `#FFC857` · Mint `#7BFFCE`
- **Text**: Primary `#F5F7FF` · Secondary 72% · Tertiary 48% · Mono `#9FA8C7`

### Spacing
xxs 2 · xs 4 · s 8 · m 12 · l 16 · xl 24 · xxl 32 · xxxl 48 · jumbo 64

### Radius
S 8 · M 16 · L 28 (signature) · pill 999

### Elevation
L0 page · L1 glass 24dp · L2 raised 32dp · L3 modal 40dp · L4 FAB gradient

### Motion
Standard `cubic(0.2, 0, 0, 1)` · Emphasized `cubic(0.05, 0.7, 0.1, 1)`
Fast 180ms · Medium 280ms · Slow 480ms · Breath 3000ms · AuroraDrift 8000ms

## How to Use

1. **For design reviews**: Open in browser (e.g. `xdg-open 08-avatar-library.svg`) or embed in Figma via drag-drop.
2. **For implementation**: Reference colors/spacing/radius/motion values directly when building `:core:design` tokens in `AuroraTheme.kt`.
3. **For handoff**: The SVG markup uses simple primitives — designers can refine in Figma/Illustrator.
4. **For accessibility**: All contrast ratios verified ≥ 4.5:1; touch targets ≥ 48dp; reduce-motion honored.

## Companion Docs
- `docs/architecture/UI_DESIGN.md` — legacy M2 design tokens (superseded by this file)
- `docs/designs/aurora-v3/plan.md` — full plan with phases & file paths
- `/home/mobo/.claude/plans/avatar-uiux-uiue-playful-crown.md` — master plan
