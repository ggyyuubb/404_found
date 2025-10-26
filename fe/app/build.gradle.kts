plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.wearther"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wearther"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 버전 변수
    val cameraxVersion = "1.3.1"

    // ========== Core Android ==========
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.0")
    implementation("androidx.activity:activity-compose:1.9.0")

    // ========== Compose ==========
    implementation(platform("androidx.compose:compose-bom:2024.04.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended:1.6.5")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.5")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ========== Lucide Icons ==========
    implementation("com.composables:icons-lucide-android:1.1.0")

    // ========== Firebase ==========
    implementation("com.google.firebase:firebase-storage-ktx:21.0.0")
    implementation("com.google.firebase:firebase-firestore-ktx:25.0.0")
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // ========== Network ==========
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ========== Image Loading ==========
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ========== CameraX ==========
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // ========== Image Cropper ==========
    implementation("com.github.CanHub:Android-Image-Cropper:4.3.3")

    // ========== Coroutines ==========
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // ========== Utilities ==========
    implementation("at.favre.lib:bcrypt:0.10.2")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.34.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.android.material:material:1.12.0")

    // ========== Testing ==========
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.04.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // ========== Debug ==========
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

tasks.whenTaskAdded {
    if (name == "mapDebugSourceSetPaths") {
        dependsOn("processDebugGoogleServices")
    }
}