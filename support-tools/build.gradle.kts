plugins {
//    alias(libs.plugins.android.library)
//    alias(libs.plugins.kotlin.android)
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hsjeong.supporttools"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15" // Compose Compiler 버전
    }
}

dependencies {
   /* implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.lifecycle.process)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
//    implementation("com.google.dagger:hilt-android:2.56.2")
//    kapt("com.google.dagger:hilt-android-compiler:2.56.2")
//    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // okhttp
    implementation(libs.okhttp)

    // Chucker
    debugImplementation(libs.library)
    releaseImplementation(libs.library.no.op)*/

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2025.04.00"))
    implementation("androidx.activity:activity-compose")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Hilt
//    implementation("com.google.dagger:hilt-android:2.56.2")
//    kapt("com.google.dagger:hilt-android-compiler:2.56.2")
//    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // okhttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Chucker
    debugImplementation("com.github.chuckerteam.chucker:library:4.1.0")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.1.0")
}