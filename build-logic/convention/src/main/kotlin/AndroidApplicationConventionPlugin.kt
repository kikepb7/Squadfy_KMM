import com.android.build.api.dsl.ApplicationExtension
import com.kikepb.squadfy.convention.configureKotlinAndroid
import com.kikepb.squadfy.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import java.util.Properties

/**
 * Android application setup (spec 011):
 * - release signed with `keystore.properties` (root, gitignored) or the `SIGNING_*` environment variables (CI);
 *   without them, local release builds fall back to the debug key so the minified app can be installed for QA;
 * - R8 minify + resource shrinking with `proguard-rules.pro`;
 * - `versionCode` from `-PversionCode` (CI: github.run_number), `versionName` from the version catalog (SemVer).
 */
class AndroidApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
            }

            val keystoreProperties = Properties().apply {
                val file = rootProject.file("keystore.properties")
                if (file.exists()) file.inputStream().use(::load)
            }
            fun signing(key: String, env: String): String? =
                keystoreProperties.getProperty(key) ?: providers.environmentVariable(env).orNull

            extensions.configure<ApplicationExtension> {
                namespace = "org.kikepb.squadfy"

                defaultConfig {
                    applicationId = libs.findVersion("projectApplicationId").get().toString()
                    targetSdk = libs.findVersion("projectTargetSdkVersion").get().toString().toInt()
                    versionCode = providers.gradleProperty("versionCode").orNull?.toInt()
                        ?: libs.findVersion("projectVersionCode").get().toString().toInt()
                    // QA candidates from release/* carry "-rc.N" (spec 015 AC-015-08); store builds from tags do not
                    versionName = libs.findVersion("projectVersionName").get().toString() +
                        providers.gradleProperty("versionNameSuffix").orNull.orEmpty()
                    manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config"
                }
                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }

                val storeFilePath = signing("storeFile", "SIGNING_STORE_FILE")
                val releaseSigning = storeFilePath?.let { path ->
                    signingConfigs.create("release") {
                        storeFile = rootProject.file(path)
                        storePassword = signing("storePassword", "SIGNING_STORE_PASSWORD")
                        keyAlias = signing("keyAlias", "SIGNING_KEY_ALIAS")
                        keyPassword = signing("keyPassword", "SIGNING_KEY_PASSWORD")
                    }
                }

                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        // Only for testing a minified release against the local backend (AC-011-03)
                        if (providers.gradleProperty("ALLOW_INSECURE_RELEASE").orNull.toBoolean()) {
                            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_local"
                        }
                        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                        signingConfig = releaseSigning ?: signingConfigs.getByName("debug").also {
                            logger.warn("Squadfy: no release keystore (keystore.properties / SIGNING_*); release is signed with the debug key and cannot be uploaded to the stores")
                        }
                    }
                }

                configureKotlinAndroid(this)
            }
        }
    }
}
