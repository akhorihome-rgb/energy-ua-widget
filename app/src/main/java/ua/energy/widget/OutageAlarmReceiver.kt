package ua.energy.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class OutageAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val widgetId = intent?.getIntExtra("widgetId", 0) ?: 0
        val title = intent?.getStringExtra("title") ?: "Сповіщення"
        val body = intent?.getStringExtra("body") ?: ""
        val soundUri = intent?.getStringExtra("soundUri")
        OutageNotifier.showNotification(context, widgetId, title, body, soundUri)
    }
}
