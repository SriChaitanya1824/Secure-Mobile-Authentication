plugins { id("com.android.library"); kotlin("android"); id("org.jetbrains.kotlin.plugin.compose") }
android { namespace="com.srichaitanya.secureauth.ui"; compileSdk=35; defaultConfig { minSdk=26 }; buildFeatures { compose=true } }
dependencies { api(project(":android-sdk:secureauth")); implementation(platform("androidx.compose:compose-bom:2024.12.01")); implementation("androidx.compose.material3:material3"); implementation("androidx.compose.runtime:runtime") }
