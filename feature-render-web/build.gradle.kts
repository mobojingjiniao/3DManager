plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.threed.manager.feature.renderweb"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        buildConfig = true
    }
    // Capacitor dependencies deferred to Phase 1.3 (require network artifacts at install time).
}

dependencies {
    implementation(project(":core:scene-api"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.coroutines)

    testImplementation(libs.bundles.test.unit)
    testImplementation(libs.robolectric)
}
