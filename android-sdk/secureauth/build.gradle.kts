plugins { id("com.android.library"); kotlin("android"); kotlin("plugin.serialization"); id("maven-publish") }
android { namespace = "com.srichaitanya.secureauth"; compileSdk = 35
    defaultConfig { minSdk = 26; consumerProguardFiles("consumer-rules.pro"); testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
    buildFeatures { buildConfig = false }
    publishing { singleVariant("release") { withSourcesJar() } }
}
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    testImplementation("junit:junit:4.13.2"); testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
afterEvaluate { publishing { publications { create<MavenPublication>("release") { from(components["release"]); groupId="com.srichaitanya.secureauth"; artifactId="secureauth"; version="1.0.0" } } } }
