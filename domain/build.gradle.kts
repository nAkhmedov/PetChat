plugins {
    alias(libs.plugins.petchat.jvm.library)
}

dependencies {
    api(libs.javax.inject)
    api(libs.kotlinx.coroutines.core)
}
