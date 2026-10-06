pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "secure-mobile-authentication"
include(":android-sdk:secureauth", ":android-sdk:secureauth-ui", ":sample-app")
