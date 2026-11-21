plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "edu.cs4730.barcodevideoxdemo"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "edu.cs4730.barcodevideoxdemo"
        minSdk = 32
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.camera.camera2)
    // CameraX Lifecycle Library
    implementation(libs.camera.lifecycle)
    // CameraX View class
    implementation(libs.camera.view)

    implementation(libs.camera.extensions)
    implementation(libs.camera.mlkit.vision)
    implementation(libs.concurrent.futures)

    implementation (libs.barcode.scanning)
}