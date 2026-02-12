import java.io.FileInputStream
import java.util.Properties
import kotlin.apply

val keystoreProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.devtools.ksp") version "2.0.20-1.0.24"
}

android {
    namespace = "com.app.base" // Đổi theo package app mới
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.app.base" //Đổi tên theo bundle app mới
        minSdk = 28
        targetSdk = 35
        versionCode = project.findProperty("versionCode")?.toString()?.toInt() ?: 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] ?: "no_file")
            storePassword = keystoreProperties["storePassword"]?.toString()
            keyAlias = keystoreProperties["keyAlias"]?.toString()
            keyPassword = keystoreProperties["keyPassword"]?.toString()
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    flavorDimensions += "env"

    productFlavors {
        create("dev") {
            dimension = "env"
            applicationId = "dev.app.base"
        }
        create("prod") {
            dimension = "env"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    tasks.register("printVersionName") {
        doLast {
            println(android.defaultConfig.versionName)
        }
    }

    tasks.register("printVersionCode") {
        doLast {
            println(android.defaultConfig.versionCode)
        }
    }

    tasks.matching { it.name.startsWith("bundle") && it.name.endsWith("Release") }.configureEach {
        doLast {
            val versionName = android.defaultConfig.versionName
            val versionCode = android.defaultConfig.versionCode
            val appName = "app_name" //Đổi tên app

            val bundleFile = (this as? AbstractArchiveTask)?.archiveFile?.get()?.asFile
            bundleFile?.let { file ->
                val newName = "${appName}_${versionName}(${versionCode}).aab"
                val newFile = file.resolveSibling(newName)
                file.renameTo(newFile)
                println(":white_check_mark: Rename AAB to $newName")
            }
        }
    }
}

dependencies {
    implementation(project(":base"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}