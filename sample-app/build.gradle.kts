plugins { id("com.android.application"); kotlin("android"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace="com.srichaitanya.secureauth.sample"; compileSdk=35
 defaultConfig { applicationId="com.srichaitanya.secureauth.sample"; minSdk=26; targetSdk=35; versionCode=1; versionName="1.0"; testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner" }
 buildFeatures { compose=true }; packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies { implementation(project(":android-sdk:secureauth")); implementation(project(":android-sdk:secureauth-ui")); implementation(platform("androidx.compose:compose-bom:2024.12.01")); implementation("androidx.activity:activity-compose:1.10.0"); implementation("androidx.compose.material3:material3"); implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") }
