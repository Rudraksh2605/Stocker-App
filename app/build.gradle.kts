plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}




android {
    namespace = "com.hfad.stocker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hfad.stocker"
        minSdk = 27
        targetSdk = 34
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    //Firebase Dependencies
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-bom:33.1.0")
    implementation("com.google.firebase:firebase-analytics:19.0.2")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-auth:21.0.1")
    implementation(libs.firebase.database)
    implementation(libs.recyclerview)


    //Retrofit Dependencies
    dependencies {
        implementation("com.squareup.okhttp3:okhttp:4.9.0")
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        implementation("androidx.recyclerview:recyclerview:1.3.2")

    }

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}