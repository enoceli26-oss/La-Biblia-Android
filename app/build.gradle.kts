plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.biblia.rv1909"
    compileSdk = 34
    defaultConfig { applicationId = "com.biblia.rv1909"; minSdk = 24; targetSdk = 34; versionCode = 2; versionName = "2.0" }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
}
