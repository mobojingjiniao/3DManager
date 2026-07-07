pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Aliyun mirrors first (much faster than dl.google.com / Maven Central direct
        // in this environment).
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "3DManager"

// ===== Application entry =====
include(":app")

// ===== Core layer (library modules) =====
include(":core:scene-api")        // SplatController + WebBridge + SplatRendererApi interfaces
include(":core:gs-codec")         // Shared .ply / .splat / .ksplat / .spz decoders
include(":core:gs-edit")          // EditCommand + UndoRedoStack + EditLog
include(":core:gs-grouping")      // GaussianGroup / ScreenSpaceSelector
include(":core:sensor")           // SensorSource + GravityFilter
include(":core:data")             // Room + DataStore (Scene, Asset, EditLog, Prefs)
include(":core:model")            // Serialization models (SceneManifest, SplatAsset, ThemePack)
include(":core:design")           // Compose Material3 theming + dynamic color
include(":core:system")           // WallpaperService + AdaptiveRenderStrategy + MediaStore

// ===== Feature layer (Android library modules) =====
include(":feature:scenes")        // 3DGS asset library list, detail, preview
include(":feature:editor")        // Editor: Brush / Lasso / Gizmo / Inspector
include(":feature:roam")          // Orbit / FPS / Cinematic / Trackball
include(":feature:themes")        // Theme management / Live Wallpaper preview
include(":feature:settings")      // Preferences (gravity sensitivity, render backend)

// ===== Render backends =====
include(":feature-render-web")        // Capacitor + Spark + three.js (primary)
include(":feature-render-native")     // Filament + KHR_gaussian_splatting (high-end backup)

// ===== Benchmark (release flavor) =====
// include(":benchmark")  // activated when Phase 5 begins

// Tells all android modules where to find their SDK; populated from local.properties
// (sdk.dir=/home/mobo/.android-sdk)
