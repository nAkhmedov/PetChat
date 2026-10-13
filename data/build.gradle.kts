plugins {
    alias(libs.plugins.petchat.android.library)
    alias(libs.plugins.petchat.android.hilt)
}

android {
    namespace = "uz.petchat.data"
}

dependencies {
    api(projects.domain)
    implementation(libs.kotlinx.coroutines.android)
}
