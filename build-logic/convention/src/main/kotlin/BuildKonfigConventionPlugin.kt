import com.codingfeline.buildkonfig.compiler.FieldSpec
import com.codingfeline.buildkonfig.gradle.BuildKonfigExtension
import com.kikepb.squadfy.convention.SquadfyBackend
import com.kikepb.squadfy.convention.isSquadfyReleaseBuild
import com.kikepb.squadfy.convention.pathToPackageName
import com.kikepb.squadfy.convention.squadfyBackend
import com.kikepb.squadfy.convention.squadfyEnvironment
import com.kikepb.squadfy.convention.squadfyProperty
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

/** Firebase Emulator Suite (spec 019 AC-019-02): the Android emulator reaches the host through 10.0.2.2. */
private const val DEFAULT_FIREBASE_EMULATOR_HOST = "10.0.2.2"

/**
 * Legal pages (spec 011 AC-011-07/08). With the own backend the deletion page is served next to the API (BE spec 010);
 * with Firebase both pages live in Firebase Hosting (spec 019 T-026), overridable with `SQUADFY_WEB_URL`.
 */
private const val DEFAULT_PRIVACY_POLICY_URL = "https://squadfy.app/privacy"
private const val DEFAULT_FIREBASE_WEB_URL = "https://squadfy-5233d.web.app"
private const val ACCOUNT_DELETION_PATH = "/account/delete"
private const val PRIVACY_POLICY_PATH = "/legal/privacy"

class BuildKonfigConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.codingfeline.buildkonfig")
            }

            fun lookup(key: String): String? = squadfyProperty(key)

            fun resolve(key: String, default: String): String = lookup(key) ?: default

            extensions.configure<BuildKonfigExtension> {
                packageName = target.pathToPackageName()
                defaultConfigs {
                    // No client API key (ADR-0008, D-2): the backend authenticates users with JWT, and any key
                    // compiled into the app could be extracted from the APK anyway.
                    val baseUrlHttp = resolve("BASE_URL_HTTP", DEFAULT_BASE_URL_HTTP)
                    val baseUrlWs = resolve("BASE_URL_WS", DEFAULT_BASE_URL_WS)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_HTTP", baseUrlHttp)
                    buildConfigField(FieldSpec.Type.STRING, "BASE_URL_WS", baseUrlWs)

                    // Spec 011 AC-011-03: a release (Android *Release* tasks or Xcode Release) must use HTTPS/WSS.
                    // ALLOW_INSECURE_RELEASE=true is only for testing a minified release against a local backend.
                    val isReleaseBuild = isSquadfyReleaseBuild
                    val allowInsecure = lookup("ALLOW_INSECURE_RELEASE")?.toBoolean() == true
                    // Only the own backend (frozen, ADR-0010) talks to these URLs; the Firebase variant ignores them
                    if (isReleaseBuild && !allowInsecure && squadfyBackend == SquadfyBackend.API) {
                        check(baseUrlHttp.startsWith("https://") && baseUrlWs.startsWith("wss://")) {
                            "Release builds need BASE_URL_HTTP=https://… and BASE_URL_WS=wss://… (got $baseUrlHttp / $baseUrlWs)"
                        }
                    }
                    // Logging and diagnostics are reduced in release builds (AC-011-04)
                    buildConfigField(FieldSpec.Type.BOOLEAN, "IS_RELEASE", isReleaseBuild.toString())

                    val webUrl = resolve("SQUADFY_WEB_URL", DEFAULT_FIREBASE_WEB_URL).removeSuffix("/")
                    val onFirebase = squadfyBackend == SquadfyBackend.FIREBASE
                    buildConfigField(
                        FieldSpec.Type.STRING,
                        "PRIVACY_POLICY_URL",
                        resolve("PRIVACY_POLICY_URL", if (onFirebase) webUrl + PRIVACY_POLICY_PATH else DEFAULT_PRIVACY_POLICY_URL)
                    )
                    buildConfigField(
                        FieldSpec.Type.STRING,
                        "ACCOUNT_DELETION_URL",
                        resolve(
                            "ACCOUNT_DELETION_URL",
                            if (onFirebase) webUrl + ACCOUNT_DELETION_PATH
                            else baseUrlHttp.removeSuffix("/").removeSuffix("/api/v1") + ACCOUNT_DELETION_PATH
                        )
                    )

                    buildConfigField(FieldSpec.Type.STRING, "ENVIRONMENT", squadfyEnvironment.key)
                    // Spec 019 AC-019-01: Firebase (default) or the frozen own backend (ADR-0010)
                    buildConfigField(FieldSpec.Type.STRING, "BACKEND", squadfyBackend.key)
                    // Spec 019 AC-019-02: the Emulator Suite is only reachable from debug builds
                    val useEmulator = !isReleaseBuild && lookup("squadfy.firebase.useEmulator")?.toBoolean() == true
                    buildConfigField(FieldSpec.Type.BOOLEAN, "FIREBASE_USE_EMULATOR", useEmulator.toString())
                    buildConfigField(
                        FieldSpec.Type.STRING,
                        "FIREBASE_EMULATOR_HOST",
                        resolve("squadfy.firebase.emulatorHost", DEFAULT_FIREBASE_EMULATOR_HOST)
                    )
                }
            }
        }
    }
}