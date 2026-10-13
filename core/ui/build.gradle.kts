plugins {
    alias(libs.plugins.petchat.android.library)
    alias(libs.plugins.petchat.android.compose)
}

android {
    namespace = "uz.petchat.core.ui"
}

dependencies {
    api(projects.domain)
    api(libs.androidx.compose.foundation)
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.materialIconsCore)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.lifecycle.runtime.compose)
}
