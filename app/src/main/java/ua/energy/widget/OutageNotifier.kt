package ua.energy.widget

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object OutageNotifier {

    private const val CHANNEL_ID = "outage_alerts"
    private const val CHANNEL_NAME = "Сповіщення про відключення"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Попередження до вимкнення/увімкнення світла"
                enableVibration(true)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    private fun resolveUri(context: Context, widgetId: Int, slot: Int, soundType: Int): String? {
        val custom = if (slot == 1) WidgetPrefs.customSound1(context, widgetId)
                     else WidgetPrefs.customSound2(context, widgetId)
        if (custom.isNotBlank()) return custom
        val uri = when (soundType) {
            0 -> null
            2 -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            3 -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            else -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        }
        return uri?.toString()
    }

    fun scheduleNext(context: Context, widgetId: Int, periods: List<OutagePeriod>) {
        cancel(context, widgetId)
        ensureChannel(context)

        val now = System.currentTimeMillis()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        var reqCode = 10000 + widgetId * 40

        data class Slot(val minutes: Int, val slotNum: Int, val soundType: Int)
        val slots = mutableListOf<Slot>()
        if (WidgetPrefs.notifyEnabled1(context, widgetId)) {
            val m = WidgetPrefs.notifyMinutes(context, widgetId)
            if (m > 0) slots.add(Slot(m, 1, WidgetPrefs.notifySound1(context, widgetId)))
        }
        if (WidgetPrefs.notifyEnabled2(context, widgetId)) {
            val m = WidgetPrefs.notifyMinutes2(context, widgetId)
            if (m > 0) slots.add(Slot(m, 2, WidgetPrefs.notifySound2(context, widgetId)))
        }

        for (slot in slots) {
            val offsetMs = slot.minutes * 60_000L
            val soundUriStr = resolveUri(context, widgetId, slot.slotNum, slot.soundType)
            for (p in periods) {
                val offAt = p.timeFrom * 1000L - offsetMs
                if (offAt > now + 5000) {
                    scheduleOne(context, am, widgetId, reqCode++, offAt,
                        "Скоро вимкнення ${WidgetPrefs.iconOff(context, widgetId)}",
                        "Світло вимкнеться через ${slot.minutes} хв",
                        soundUriStr)
                }
                val onAt = p.timeTo * 1000L - offsetMs
                if (onAt > now + 5000) {
                    scheduleOne(context, am, widgetId, reqCode++, onAt,
                        "Скоро увімкнення ${WidgetPrefs.iconOn(context, widgetId)}",
                        "Світло з’явиться через ${slot.minutes} хв",
                        soundUriStr)
                }
            }
        }
    }

    private fun scheduleOne(
        context: Context, am: AlarmManager, widgetId: Int, requestCode: Int,
        triggerAt: Long, title: String, body: String, soundUriStr: String?
    ) {
        val intent = Intent(context, OutageAlarmReceiver::class.java).apply {
            putExtra("widgetId", widgetId)
            putExtra("title", title)
            putExtra("body", body)
            putExtra("soundUri", soundUriStr ?: "")
        }
        val pi = PendingIntent.getBroadcast(context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            try { am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi) } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    fun cancel(context: Context, widgetId: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0 until 40) {
            val pi = PendingIntent.getBroadcast(
                context, 10000 + widgetId * 40 + i,
                Intent(context, OutageAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.cancel(pi)
        }
    }

    fun showNotification(context: Context, widgetId: Int, title: String, body: String, soundUriStr: String? = null) {
        ensureChannel(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (!soundUriStr.isNullOrBlank()) {
            try {
                builder.setSound(Uri.parse(soundUriStr))
            } catch (_: Exception) {
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            }
        } else {
            builder.setSilent(true)
        }

        if (WidgetPrefs.notifyVibrate(context, widgetId)) {
            builder.setVibrate(longArrayOf(0, 400, 200, 400))
        }

        try {
            NotificationManagerCompat.from(context).notify(
                20000 + widgetId + (soundUriStr?.hashCode()?.and(0xFF) ?: 0),
                builder.build()
            )
        } catch (_: SecurityException) {}
    }
}
