pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "secure-digital-certificate-wallet"

// Include Android modules
include(":android:app")
include(":android:core")
include(":android:core-security")
include(":android:core-network")
include(":android:core-database")
include(":android:core-ui")
include(":android:feature-auth")
include(":android:feature-wallet")
include(":android:feature-certificate")
include(":android:feature-scanner")
include(":android:feature-verification")
include(":android:feature-profile")

// Include Backend modules
include(":issuer-backend")
include(":verifier-backend")
