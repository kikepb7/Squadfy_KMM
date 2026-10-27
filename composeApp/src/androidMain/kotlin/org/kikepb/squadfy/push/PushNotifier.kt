package org.kikepb.squadfy.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.kikepb.core.domain.notification.PushChannel
import com.kikepb.core.domain.notification.PushMessage
import com.kikepb.core.domain.notification.PushRouter
import org.kikepb.squadfy.MainActivity
import org.kikepb.squadfy.R

/**
 * Android notification channels and foreground notifications (AC-009-02). Spec 021 adds «Club y temporada» and
 * «Novedades de Squadfy», so the user can silence them without losing the match ones; the Functions pick the same
 * channel for background pushes (`channelOf`).
 */
object PushNotifier {

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannels(
            PushChannel.entries.map { channel ->
                val (name, description, importance) = when (channel) {
                    PushChannel.MATCH_UPDATES ->
                        Triple(R.string.channel_match_updates, R.string.channel_match_updates_description, NotificationManager.IMPORTANCE_HIGH)
                    PushChannel.CHAT -> Triple(R.string.channel_chat, R.string.channel_chat_description, NotificationManager.IMPORTANCE_HIGH)
                    PushChannel.CLUB -> Triple(R.string.channel_club, R.string.channel_club_description, NotificationManager.IMPORTANCE_DEFAULT)
                    PushChannel.APP -> Triple(R.string.channel_app, R.string.channel_app_description, NotificationManager.IMPORTANCE_DEFAULT)
                }
                NotificationChannel(channel.id, context.getString(name), importance).apply { this.description = context.getString(description) }
            }
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
        val channel = PushRouter.channelOf(message.data)
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setPriority(if (channel == PushChannel.MATCH_UPDATES || channel == PushChannel.CHAT) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
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
