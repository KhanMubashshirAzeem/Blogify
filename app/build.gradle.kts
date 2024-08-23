plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.skills_plus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.skills_plus"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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
    implementation(libs.firebase.auth)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")

//    implementation ("com.google.firebase:firebase-bom") // Bill of Materials for Firebase libraries
    implementation("com.google.firebase:firebase-storage-ktx") // Firebase Storage Kotlin extensions
    implementation("com.google.firebase:firebase-database-ktx") // Firebase Realtime Database Kotlin extensions

    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.google.android.material:material:1.12.0")

    implementation("com.google.firebase:firebase-messaging")


}
