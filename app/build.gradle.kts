plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.skills_plus"
    compileSdk = 36 // Using latest stable compileSdk instead of 36 (unstable)

    defaultConfig {
        applicationId = "com.example.skills_plus"
        minSdk = 25
        targetSdk = 36 // Matching compileSdk stable version
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Smooth Bottom Bar
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Firebase BoM (use latest stable, avoids manual versioning)
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging")

    // Circle ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.9.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment:2.8.2")
    implementation("androidx.navigation:navigation-ui:2.8.2")
}
