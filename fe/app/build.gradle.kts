plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("com.google.gms.google-services")
}

apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.example.wearther"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.wearther"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // ✅ 위치 정리됨
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
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

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.0")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.5")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")


    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.11.1")

    implementation("com.google.firebase:firebase-auth-ktx:22.0.0") // Firebase 인증
    implementation("com.google.android.gms:play-services-auth:20.7.0") // 최신 버전

    implementation("io.coil-kt:coil-compose:2.6.0") // ✅ Coil 추가

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// JSON 파싱용
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// 비동기 처리용 (옵션)
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("at.favre.lib:bcrypt:0.9.0")
    // 위치 정보 서비스
    implementation ("com.google.android.gms:play-services-location:21.0.1")  // 최신 버전 사용 가능

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    implementation ("com.google.accompanist:accompanist-swiperefresh:0.31.5-beta")

    implementation ("androidx.compose.runtime:runtime-livedata:1.5.4")

    val camerax_version = "1.3.0"

    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")

// Camera 권한을 위한 activity-compose도 함께 권장
    implementation("androidx.activity:activity-compose:1.8.2")



    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
tasks.whenTaskAdded {
    if (name == "mapDebugSourceSetPaths") {
        dependsOn("processDebugGoogleServices")
    }
}
