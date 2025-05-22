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
        // Enable core library desugaring
        isCoreLibraryDesugaringEnabled = true
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
    coreLibraryDesugaring(libs.desugar.jdk.libs) // Make sure this line is present and correct

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    // Google Play Services
    implementation(libs.play.services.base)
    implementation(libs.play.services.tasks)
    implementation(libs.google.auth)

    // Firebase SDKs
    implementation(platform(libs.firebase.bom)) // Firebase Bill of Materials
    implementation(libs.firebase.auth.ktx)      // Firebase Authentication
    implementation(libs.firebase.firestore.ktx) // Firebase Firestore (database)
    implementation(libs.firebase.storage.ktx)   // Firebase Storage
    implementation(libs.firebase.appcheck.debug) // Firebase App Check Debug
    implementation(libs.firebase.analytics)     // Firebase Analytics - helps with tracking issues

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}