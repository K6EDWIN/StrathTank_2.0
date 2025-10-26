plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.strathtankalumni"
    compileSdk = 36 // Use direct assignment for integer properties

    defaultConfig {
        applicationId = "com.example.strathtankalumni"
        minSdk = 24
        targetSdk = 36
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
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Note: It looks like this dependencies block is nested. I'm keeping your structure but
    // ensure this is inside the main `dependencies { ... }` block of your app module.
    dependencies {
        // 1. KOTLIN COROUTINES (Good practice for async Firebase/network calls)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1") // Latest stable

        // 2. FIREBASE (Using the BOM for unified version management)
        implementation(platform("com.google.firebase:firebase-bom:32.7.0")) // BOM

        // Auth - Fixes the .ktx error and provides the latest Auth SDK
        implementation("com.google.firebase:firebase-auth-ktx")

        // Firestore (Needed for your Registration Screen to save user data)
        implementation("com.google.firebase:firebase-firestore-ktx")


        // 3. ANDROIDX & COMPOSE
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.graphics)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.compose.material3)

        // *** FIX: ADDED DEPENDENCY FOR EXTENDED MATERIAL ICONS (e.g., Icons.Filled.People, Icons.Filled.Work) ***
        implementation("androidx.compose.material:material-icons-extended")

        // 4. NAVIGATION - FIXES NavHostController error
        implementation("androidx.navigation:navigation-compose:2.7.5") // Standard, stable version

        // 5. TESTING
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.compose.ui.test.junit4)
        debugImplementation(libs.androidx.compose.ui.tooling)
        debugImplementation(libs.androidx.compose.ui.test.manifest)
    }
}
