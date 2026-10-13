import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import uz.petchat.buildlogic.configureAndroidCompose

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val commonExtension: CommonExtension<*, *, *, *, *, *> =
                extensions.findByType<ApplicationExtension>()
                    ?: extensions.getByType<LibraryExtension>()
            configureAndroidCompose(commonExtension)
        }
    }
}
