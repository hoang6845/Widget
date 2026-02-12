pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://artifact.bytedance.com/repository/pangle/")
        maven(url = "https://android-sdk.is.com/")
        maven(url = "https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
        maven(url = "https://cboost.jfrog.io/artifactory/chartboost-ads/")
        maven(url = "https://maven.pkg.github.com/Braly-Danang/DN_Common_Libraries") {
            credentials {
                val props = java.util.Properties()
                file("local.properties").inputStream().use { props.load(it) }
                username = props.getProperty("github_username")
                password = props.getProperty("github_token")
            }
        }
    }
}

rootProject.name = "widget"
include(":app")
include(":base")
//include(":ads-module")
//project(":ads-module").projectDir = File("../DN_Common_Libraries/ads-module")