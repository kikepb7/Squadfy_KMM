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
private const val DEFAULT_BASE_URL_HTTP = "http://10.0.2.2:8080/api/v1"
private const val DEFAULT_BASE_URL_WS = "ws://10.0.2.2:8080/ws"

/** Build environment for feature flag defaults (spec 013). CI release jobs for production pass `pro`. */
private const val DEFAULT_ENVIRONMENT = "pre"
private val SUPPORTED_ENVIRONMENTS = setOf("pre", "pro")

class BuildKonfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            val localProperties = gradleLocalProperties(rootDir, rootProject.providers)

            // Lookup order: -Pkey / gradle.properties, then environment (CI secrets), then local.properties.
            fun lookup(key: String): String? =
                providers.gradleProperty(key).orNull
                    ?: providers.environmentVariable(key).orNull
                    ?: localProperties.getProperty(key)

            fun resolve(key: String, default: String): String = lookup(key) ?: default

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    val apiKey = lookup("API_KEY")
                        ?: throw IllegalStateException(
                            "Missing API_KEY: set it in local.properties, as -PAPI_KEY or as an environment variable"
                        )
                    buildConfigField(FieldSpec.Type.STRING, "API_KEY", apiKey)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_HTTP", resolve("BASE_URL_HTTP", DEFAULT_BASE_URL_HTTP))
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", resolve("BASE_URL_WS", DEFAULT_BASE_URL_WS))

                    val environment = resolve("SQUADFY_ENV", DEFAULT_ENVIRONMENT).lowercase()
                    check(environment in SUPPORTED_ENVIRONMENTS) {
                        "Invalid SQUADFY_ENV '$environment'. Expected one of $SUPPORTED_ENVIRONMENTS"
                    }
                    buildConfigField(FieldSpec.Type.STRING, "ENVIRONMENT", environment)
                }
            }
        }
    }
}