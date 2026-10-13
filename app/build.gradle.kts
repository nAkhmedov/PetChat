plugins {
    alias(libs.plugins.petchat.android.application)
    alias(libs.plugins.petchat.android.compose)
    alias(libs.plugins.petchat.android.hilt)
}

android {
    namespace = "uz.petchat"

    defaultConfig {
        applicationId = "uz.petchat"
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation(projects.core.mvi)
    implementation(projects.core.ui)
    implementation(projects.data)
    implementation(projects.feature.chatlist)
    implementation(projects.feature.conversation)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
}
