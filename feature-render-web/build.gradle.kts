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
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":core:scene-api"))

    // Capacitor 7.x — provides BridgeActivity, JS<->Kotlin bridge plumbing
    // Phase 1.3: real integration; web assets (Spark + three.js) live in src/main/assets/public/
    implementation(libs.capacitor.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.coroutines)

    testImplementation(libs.bundles.test.unit)
    testImplementation(libs.robolectric)
}