plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {


    namespace = "com.example.visionassist"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.visionassist"
        minSdk = 24
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

    aaptOptions {
        noCompress("tflite")
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.camera.core)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.androidx.room.external.antlr)
    //implementation(libs.litert.support.api)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.espresso.core)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    // ML Kit Text Recognition (for reading text)
    implementation ("com.google.mlkit:text-recognition:16.0.0")

// ML Kit Translation (for translating text)
    implementation ("com.google.mlkit:translate:17.0.0")

// CameraX dependencies for camera preview
    implementation ("androidx.camera:camera-core:1.2.3")
    implementation ("androidx.camera:camera-camera2:1.2.3")
    implementation ("androidx.camera:camera-lifecycle:1.2.3")
    implementation ("androidx.camera:camera-view:1.2.3" )// Important for Compose integration
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")

    implementation ("com.google.mlkit:translate:17.0.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")


   // implementation ("com.google.mlkit:object-detection:17.0.2")
    //implementation ("com.google.mlkit:object-detection-custom:17.0.2")


    implementation("org.tensorflow:tensorflow-lite:2.14.0")

    // TensorFlow Lite Support (for TensorImage, ImageProcessor, NormalizeOp, etc.)
    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    // (Optional) GPU Delegate for speed
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
}