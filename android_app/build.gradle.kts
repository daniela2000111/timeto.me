plugins {
    kotlin("android")
    id("com.android.application")
}

android {

    namespace = "me.timeto.app"
    compileSdk = 33

    defaultConfig {
        applicationId = "me.timeto.app"
        minSdk = 26
        targetSdk = 33
        versionCode = 433
        versionName = "2023.10.13"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures.buildConfig = true
    buildFeatures.compose = true
    composeOptions.kotlinCompilerExtensionVersion = "1.5.4"
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.compose.material:material:1.4.3")
    implementation("androidx.compose.material:material-icons-extended:1.4.3")
    implementation("com.google.android.material:material:1.9.0")
}
