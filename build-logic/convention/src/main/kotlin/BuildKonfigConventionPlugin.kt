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

/** Legal pages (spec 011 AC-011-07/08). The deletion page is served by the backend (BE spec 010) next to the API. */
private const val DEFAULT_PRIVACY_POLICY_URL = "https://squadfy.app/privacy"
private const val ACCOUNT_DELETION_PATH = "/account/delete"
private val SUPPORTED_ENVIRONMENTS = setOf("pre", "pro")

class BuildKonfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            val localProperties = gradleLocalProperties(rootDir, rootProject.providers)

            // Lookup order: -Pkey / gradle.properties, then environment (CI secrets), then local.properties.
            // Blank values count as missing: GitHub expands an unset secret to "", which must not pass as a real key.
            fun lookup(key: String): String? =
                providers.gradleProperty(key).orNull?.takeIf { it.isNotBlank() }
                    ?: providers.environmentVariable(key).orNull?.takeIf { it.isNotBlank() }
                    ?: localProperties.getProperty(key)?.takeIf { it.isNotBlank() }

            fun resolve(key: String, default: String): String = lookup(key) ?: default

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    val apiKey = lookup("API_KEY")
                        ?: throw IllegalStateException(
                            "Missing API_KEY: set it in local.properties, as -PAPI_KEY or as an environment variable"
                        )
                    buildConfigField(FieldSpec.Type.STRING, "API_KEY", apiKey)
                    val baseUrlHttp = resolve("BASE_URL_HTTP", DEFAULT_BASE_URL_HTTP)
                    val baseUrlWs = resolve("BASE_URL_WS", DEFAULT_BASE_URL_WS)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_HTTP", baseUrlHttp)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", baseUrlWs)

                    // Spec 011 AC-011-03: a release (Android *Release* tasks or Xcode Release) must use HTTPS/WSS.
                    // ALLOW_INSECURE_RELEASE=true is only for testing a minified release against a local backend.
                    val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release") } ||
                        providers.environmentVariable("CONFIGURATION").orNull == "Release"
                    val allowInsecure = lookup("ALLOW_INSECURE_RELEASE")?.toBoolean() == true
                    if (isReleaseBuild && !allowInsecure) {
                        check(baseUrlHttp.startsWith("https://") && baseUrlWs.startsWith("wss://")) {
                            "Release builds need BASE_URL_HTTP=https://… and BASE_URL_WS=wss://… (got $baseUrlHttp / $baseUrlWs)"
                        }
                    }
                    // Logging and diagnostics are reduced in release builds (AC-011-04)
                    buildConfigField(FieldSpec.Type.BOOLEAN, "IS_RELEASE", isReleaseBuild.toString())

                    buildConfigField(
                        FieldSpec.Type.STRING,
                        "PRIVACY_POLICY_URL",
                        resolve("PRIVACY_POLICY_URL", DEFAULT_PRIVACY_POLICY_URL)
                    )
                    buildConfigField(
                        FieldSpec.Type.STRING,
                        "ACCOUNT_DELETION_URL",
                        resolve("ACCOUNT_DELETION_URL", baseUrlHttp.removeSuffix("/").removeSuffix("/api/v1") + ACCOUNT_DELETION_PATH)
                    )

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