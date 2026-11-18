plugins {
    id("com.android.application")
}

android {
    namespace = "com.eldar.chess"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.eldar.chess"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.5.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    // Chess rules engine from GitHub: https://github.com/bhlangonijr/chesslib
    implementation("com.github.bhlangonijr:chesslib:1.3.0")
}
