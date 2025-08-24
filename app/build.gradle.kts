plugins {
   alias(libs.plugins.android.application)
   alias(libs.plugins.kotlin.android)
}

android {
   namespace = "cat.tron.dictador"
   compileSdk = 36

   defaultConfig {
      applicationId = "cat.tron.dictador"
      minSdk = 24
      targetSdk = 36
      //noinspection ExpiredTargetSdkVersion
      targetSdk = 28  // sin problema si no se publica en Google Play // 35->Android-15 # 28->Android-9 # 22->Android-5.1
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
      //compose = true
      viewBinding = true
   }
   packaging {
      resources {
         excludes += "META-INF/INDEX.LIST"
         excludes += "META-INF/DEPENDENCIES"
      }
   }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.google.cloud.speech)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
