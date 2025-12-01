plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "uy.edu.tse.hcen"
    compileSdk = 36

    defaultConfig {
        applicationId = "uy.edu.tse.hcen"
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
        getByName("debug") {
            isTestCoverageEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.browser)
    testImplementation(libs.junit)
    androidTestImplementation("org.mockito:mockito-android:5.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.fragment:fragment-testing:1.7.1")

    debugImplementation("androidx.fragment:fragment-testing:1.7.1")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))

    // Dependencies for Firebase products
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.android.material:material:1.9.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation ("androidx.browser:browser:1.8.0")



}