import os

files = {
"settings.gradle.kts": 'pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }\ndependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }\nrootProject.name = "BibliaRV1909"\ninclude(":app")\n',
"build.gradle.kts": 'plugins { id("com.android.application") version "8.2.2" apply false; id("org.jetbrains.kotlin.android") version "1.9.22" apply false }\n',
"gradle.properties": 'org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8\nandroid.useAndroidX=true\nkotlin.code.style=official\n',
"local.properties": 'sdk.dir=/opt/android-sdk\n',
"app/build.gradle.kts": '''plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
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
''',
"app/src/main/AndroidManifest.xml": '''<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://android.com">
    <queries><intent><action android:name="android.intent.action.TTS_SERVICE" /></intent></queries>
    <application android:allowBackup="true" android:icon="@mipmap/ic_launcher" android:roundIcon="@mipmap/ic_launcher_round" android:label="La Biblia RV1909" android:supportsRtl="true" android:theme="@style/Theme.Material3.DayNight.NoActionBar">
        <activity android:name=".MainActivity" android:exported="true">
            <intent-filter><action android:name="android.intent.action.MAIN" /><category android:name="android.intent.category.LAUNCHER" /></intent-filter>
        </activity>
        <activity android:name=".SettingsActivity" android:exported="false" />
    </application>
</manifest>
'''
}
for p, c in files.items():
    os.makedirs(os.path.dirname(p) or ".", exist_ok=True)
    open(p, "w", encoding="utf-8").write(c)
print("PART1_OK")
