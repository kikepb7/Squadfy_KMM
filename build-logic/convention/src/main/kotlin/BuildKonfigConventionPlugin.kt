import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.kikepb.squadfy.convention.pathToPackageName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * The Android emulator loopback IP (10.0.2.2) and plain HTTP/WS only work for local development.
 * Any real build (release APK, iOS, desktop) must override these via `-PBASE_URL_HTTP=...` /
 * `-PBASE_URL_WS=...` (e.g. from a CI secret) or a `local.properties` entry, otherwise it will
 * silently try to reach an emulator that doesn't exist.
 */
private const val DEFAULT_BASE_URL_HTTP = "http://10.0.2.2:8080/api"
private const val DEFAULT_BASE_URL_WS = "ws://10.0.2.2:8080/ws"

class BuildKonfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            val localProperties = gradleLocalProperties(rootDir, rootProject.providers)

            fun resolve(key: String, default: String): String =
                (providers.gradleProperty(key).orNull ?: localProperties.getProperty(key) ?: default)

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    val apiKey = localProperties.getProperty("API_KEY")
                        ?: throw IllegalStateException(
                            "Missing API_KEY property in local.properties"
                        )
                    buildConfigField(FieldSpec.Type.STRING, "API_KEY", apiKey)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_HTTP", resolve("BASE_URL_HTTP", DEFAULT_BASE_URL_HTTP))
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", resolve("BASE_URL_WS", DEFAULT_BASE_URL_WS))
                }
            }
        }
    }
}