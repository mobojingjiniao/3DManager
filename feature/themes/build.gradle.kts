plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.threed.manager.feature.themes"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core:scene-api"))
    implementation(project(":core:design"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:system"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.coroutines)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.bundles.test.unit)
    testImplementation(libs.robolectric)
}
