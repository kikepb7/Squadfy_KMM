package org.kikepb.squadfy

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kikepb.core.domain.notification.PushMessage
import com.kikepb.core.domain.notification.PushRouter
import org.kikepb.squadfy.navigation.ExternalUriHandler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var shouldShowSplashScreen = true

        installSplashScreen().setKeepOnScreenCondition {
            shouldShowSplashScreen
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        handlePushIntent(intent = intent)

        setContent {
            App(
                onAuthenticationChecked = {
                    shouldShowSplashScreen = false
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handlePushIntent(intent = intent)
    }

    /** A tapped push carries its `data` as extras, both from the system tray and from [PushNotifier] (AC-009-03). */
    private fun handlePushIntent(intent: Intent) {
        val extras = intent.extras ?: return
        val data = extras.keySet().mapNotNull { key -> extras.getString(key)?.let { key to it } }.toMap()
        PushRouter.deepLink(data)?.let { ExternalUriHandler.onNewUri(uri = it) }
        // Spec 021: a new version or an app announcement opens the store or the web page
        PushRouter.externalUrl(data)?.let { url ->
            // Once: a recreated activity gets the same intent back
            intent.removeExtra(PushMessage.KEY_URL)
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (_: ActivityNotFoundException) {
                // No browser or store app: the app just opens
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}