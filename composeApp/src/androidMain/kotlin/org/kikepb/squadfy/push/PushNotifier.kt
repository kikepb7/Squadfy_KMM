package org.kikepb.squadfy.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kikepb.core.domain.notification.PushMessage
import com.kikepb.core.domain.notification.PushRouter
import org.kikepb.squadfy.MainActivity
import org.kikepb.squadfy.R

/** Android notification channels and foreground notifications (AC-009-02). */
object PushNotifier {

    const val CHANNEL_MATCH_UPDATES = "match_updates"
    const val CHANNEL_CHAT = "chat"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            listOf(
                NotificationChannel(CHANNEL_MATCH_UPDATES, context.getString(R.string.channel_match_updates), NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = context.getString(R.string.channel_match_updates_description) },
                NotificationChannel(CHANNEL_CHAT, context.getString(R.string.channel_chat), NotificationManager.IMPORTANCE_HIGH)
                    .apply { description = context.getString(R.string.channel_chat_description) }
            )
        )
    }

    /** Shows [message]; tapping it opens [MainActivity] with the `data` extras, routed like a background push. */
    fun show(context: Context, message: PushMessage) {
        val notifications = NotificationManagerCompat.from(context)
        if (!notifications.areNotificationsEnabled()) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            message.data.forEach { (key, value) -> putExtra(key, value) }
        }
        val id = (message.data.values.joinToString() + message.title).hashCode()
        val pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val channel = if (PushRouter.isChat(message.data)) CHANNEL_CHAT else CHANNEL_MATCH_UPDATES
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        try {
            notifications.notify(id, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS revoked meanwhile: nothing to show
        }
    }
}
