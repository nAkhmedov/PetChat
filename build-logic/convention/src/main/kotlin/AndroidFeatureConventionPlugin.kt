import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import uz.petchat.buildlogic.libs

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("petchat.android.library")
            pluginManager.apply("petchat.android.compose")
            pluginManager.apply("petchat.android.hilt")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                add("implementation", project(mapOf("path" to ":core:mvi")))
                add("implementation", project(mapOf("path" to ":core:ui")))
                add("implementation", project(mapOf("path" to ":domain")))

                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
                add("implementation", libs.findLibrary("kotlinx-collections-immutable").get())
                add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

                add("testImplementation", libs.findLibrary("turbine").get())
            }
        }
    }
}
