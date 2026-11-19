plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ua.energy.widget"
    compileSdk = 34

    defaultConfig {
        applicationId = "ua.energy.svitloyes"
        minSdk = 26
        targetSdk = 34
        versionCode = 14
        versionName = "1.4"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    // Для виджета (пока классический RemoteViews — проще для начала)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Сеть
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Парсинг HTML
    implementation("org.jsoup:jsoup:1.17.2")
}
