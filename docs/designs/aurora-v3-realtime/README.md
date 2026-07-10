# 3DManager v3.2 — Cinematic Dark Aurora · Realtime HUD

**Version**: v3.2 · 2026-07-11
**Aesthetic**: Cinematic Dark Aurora — Realtime HUD
**Mood**: Tech-forward · Consumer product · Information-dense · **GPS-aware**

## v3.2 highlights

- **Wallpaper still supported** — "Set wallpaper" purple button on detail screen, dedicated Wallpaper tab with live preview & system intent.
- **NEW: GPS-aware map view** (Porin Cloud-style) — every captured avatar pins to a real-world location.
- **NEW: Avatar metadata gains GPS fields** — `locationLat`, `locationLng`, `locationName`, `locationAccuracyM`.
- **NEW: Mini-map on detail screen** — shows where the avatar was captured + nearby avatars + coordinates.

## File index

| # | File | Type | Dimensions |
|---|---|---|---|
| 01 | `01-app-icon-square.svg` | Adaptive icon | 1080×1080 |
| 02 | `02-app-icon-circle.svg` | Adaptive icon | 1080×1080 |
| 03 | `03-splash.svg` | Product launch screen | 1080×2400 |
| 08 | `08-avatar-library.svg` | Avatar grid (now with location chips) | 1080×2400 |
| 09 | `09-avatar-library-empty.svg` | Empty state with clear CTA | 1080×2400 |
| 10 | `10-avatar-detail.svg` | Viewer w/ mini-map + GPS coords | 1080×2400 |
| 11 | `11-avatar-map.svg` | **NEW · Full map view of all avatars** | 1080×2400 |

## Map feature (Porin Cloud-inspired)

Each captured avatar carries GPS metadata. The map screen shows all avatars as glowing pins on a dark topographic map, connected by capture-order paths. Tapping a pin opens a bottom-sheet with full details.

### AvatarMetadata GPS fields (added to domain model)

```kotlin
val locationLat: Double? = null,         // -90..90
val locationLng: Double? = null,         // -180..180
val locationName: String? = null,        // "Tokyo, JP" (reverse-geocoded)
val locationAccuracyM: Float? = null,    // GPS accuracy in meters
```

### Permission flow

Capture flow needs `ACCESS_FINE_LOCATION` to stamp the avatar's coordinates at the moment of capture. Optional — avatar saves without GPS if denied.

```
CaptureScreen
  └─ request ACCESS_FINE_LOCATION (rationale: "Pin where this avatar was captured")
       ├─ granted → FusedLocationProviderClient.getCurrentLocation() → AvatarMetadata.locationLat/Lng/Name
       └─ denied → save without GPS, show toast "Capture saved without location"
```

### Map screen anatomy

```
┌─────────────────────────────────────────┐
│  ← Map                  📍              │  Top app bar
├─────────────────────────────────────────┤
│  [All] [Captured] [Imported]      [+/-] │  Filter + zoom controls
├─────────────────────────────────────────┤
│                                         │
│            ░░░░░░░░░░░░░                │
│        ░░ dark topo map ░░              │  Procedural topographic noise
│      ░░  (navy/teal grid)  ░░          │
│       ░░  ◉ pin  ◉ pin  ░░             │  Aurora-glowing pins (mini avatar)
│        ░░   \  |  /  ░░               │
│         ░░   ◉ pin (selected) ░░       │  Selected pin: violet glow + label
│          ░░░░░░░░░░░░░                 │
│                                         │
│  ┌──────────────┐    [⊕]               │  Coordinate readout + recenter
│  │ 35.6762° N   │                       │
│  │ 139.6503° E  │                       │
│  └──────────────┘                       │
├─────────────────────────────────────────┤
│ ┌──┐ Me                       [Open →] │  Selected avatar bottom sheet
│ │  │ CAPTURED · Tokyo, JP · ±4m         │
│ └──┘ 35.6762° N · 139.6503° E           │
│      1.2M splats · 12.4 MB · 24 views   │
├─────────────────────────────────────────┤
│ Avatars │ Map │ Wallpaper │ Settings   │  Bottom nav (Map is its own tab)
└─────────────────────────────────────────┘
```

### Library updates (v3.2 with location)

- Each card gets a **mini location pin** (top-right of thumbnail)
- Card meta row adds `📍 Tokyo, JP` chip
- Filter row gains **"Map" toggle** to switch from grid to full map view

### Detail screen updates (v3.2 with location)

- Source / format / **location** chips (Tokyo, JP)
- Mono coords line: `35.6762° N · 139.6503° E · ±4m`
- New **"CAPTURED HERE"** mini-map preview with:
  - Current avatar pin (selected, larger, with halo)
  - Nearby avatars as smaller pins
  - Connecting path lines (capture order)
  - Coordinate readout overlay
  - "OPEN IN MAP →" CTA

## Other v3.2 elements (unchanged from prior iteration)

- **Status bar** — time, signal, battery
- **Top app bar** — back + title + action icons
- **Bottom nav** — Avatars / Map / Wallpaper / Settings + center FAB
- **Telemetry HUD** — FPS / splats / tier on viewer
- **Orbit compass** — yaw indicator on viewer
- **Viewfinder brackets** — corner markers on viewer + cards

## Companion Docs
- `docs/designs/aurora-v3/` — original v3 (low-fidelity)
- `docs/designs/aurora-v3-director/` — v3.1 Director's Cut (art-house)
- `docs/designs/aurora-v3-realtime/` — v3.2 (current, product-grade + GPS)
- `/home/mobo/.claude/plans/avatar-uiux-uiue-playful-crown.md` — master plan
