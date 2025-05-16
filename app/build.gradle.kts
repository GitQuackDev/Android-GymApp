plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) // Add this line
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.aimingfitness"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.aimingfitness"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs += "-Xlint:deprecation"
    }
    
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    // Google Authentication for Gmail Login
    implementation(libs.google.auth)

    // Firebase SDKs
    implementation(platform(libs.firebase.bom)) // Add Firebase Bill of Materials
    implementation(libs.firebase.auth.ktx)       // For Firebase Authentication
    implementation(libs.firebase.firestore.ktx)  // For Firebase Firestore (database)

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}