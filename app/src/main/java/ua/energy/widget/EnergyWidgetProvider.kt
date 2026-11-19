package ua.energy.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EnergyWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        if (intent?.action == ACTION_REFRESH || intent?.action == ACTION_TICK) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val mgr = AppWidgetManager.getInstance(context)
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                updateAppWidget(context, mgr, id)
            } else {
                mgr.getAppWidgetIds(ComponentName(context, EnergyWidgetProvider::class.java))
                    .forEach { updateAppWidget(context, mgr, it) }
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { updateAppWidget(context, appWidgetManager, it) }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            try { OutageNotifier.cancel(context, it) } catch (_: Exception) {}
            cancelTick(context, it)
        }
    }

    companion object {
        private const val TAG = "EnergyWidget"
        const val ACTION_REFRESH = "ua.energy.widget.ACTION_REFRESH"
        const val ACTION_TICK = "ua.energy.widget.ACTION_TICK"
        private const val TICK_MS = 2 * 60 * 1000L

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 180)
            val isSmall = minW < 120
            val layoutId = if (isSmall) R.layout.widget_layout_small else R.layout.widget_layout
            val views = RemoteViews(context.packageName, layoutId)

            // Клик → настройки
            val conf = Intent(context, ConfigureActivity::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(context, appWidgetId, conf,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val theme = WidgetPrefs.theme(context, appWidgetId)
            val defaultBg = if (theme == 1) 0xFFF5F5F5.toInt() else WidgetPrefs.bgColor(context, appWidgetId)

            // Фон
            if (isSmall) {
                views.setInt(R.id.widget_root, "setBackgroundColor", defaultBg)
            } else {
                views.setInt(R.id.widget_content, "setBackgroundColor", defaultBg)
            }

            // Окантовка (колір + показ)
            if (!isSmall && WidgetPrefs.showBorder(context, appWidgetId)) {
                val borderRes = when (WidgetPrefs.borderColor(context, appWidgetId)) {
                    0xFFF44336.toInt() -> R.drawable.widget_border_red
                    0xFF03A9F4.toInt() -> R.drawable.widget_border_blue
                    0xFFFFCC00.toInt() -> R.drawable.widget_border_yellow
                    0xFFFFFFFF.toInt() -> R.drawable.widget_border_white
                    0xFF888888.toInt() -> R.drawable.widget_border_gray
                    else -> R.drawable.widget_border   // зелений
                }
                views.setInt(R.id.widget_root, "setBackgroundResource", borderRes)
            }

            // Размеры
            views.setTextViewTextSize(R.id.tv_icon, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.sizeIcon(context, appWidgetId))
            views.setTextViewTextSize(R.id.tv_timer, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.sizeTimer(context, appWidgetId))
            if (!isSmall) {
                views.setTextViewTextSize(R.id.tv_periods, TypedValue.COMPLEX_UNIT_SP, WidgetPrefs.sizePeriods(context, appWidgetId))
            }

            // Видимость
            views.setViewVisibility(R.id.tv_icon, if (WidgetPrefs.showIcon(context, appWidgetId)) View.VISIBLE else View.GONE)
            views.setViewVisibility(R.id.tv_timer, if (WidgetPrefs.showTimer(context, appWidgetId)) View.VISIBLE else View.GONE)
            if (!isSmall) {
                views.setViewVisibility(R.id.tv_periods, if (WidgetPrefs.showPeriods(context, appWidgetId)) View.VISIBLE else View.GONE)
            }

            views.setTextViewText(R.id.tv_icon, "…")
            views.setTextViewText(R.id.tv_timer, "")
            if (!isSmall) views.setTextViewText(R.id.tv_periods, "")

            try {
                val result = OutageFetcher.fetchTodayPeriods(context, appWidgetId)
                val periods = result.periods
                val nowSec = System.currentTimeMillis() / 1000
                val isOutageNow = periods.any { nowSec >= it.timeFrom && nowSec < it.timeTo }

                // Автофон
                val bg = when {
                    WidgetPrefs.autoBg(context, appWidgetId) -> {
                        if (isOutageNow) WidgetPrefs.autoBgOffColor(context, appWidgetId)
                        else WidgetPrefs.autoBgOnColor(context, appWidgetId)
                    }
                    theme == 1 -> 0xFFF5F5F5.toInt()
                    else -> WidgetPrefs.bgColor(context, appWidgetId)
                }
                if (isSmall) {
                    views.setInt(R.id.widget_root, "setBackgroundColor", bg)
                } else {
                    views.setInt(R.id.widget_content, "setBackgroundColor", bg)
                }

                // Иконка
                val iconText = when {
                    periods.isEmpty() -> "—"
                    isOutageNow -> WidgetPrefs.iconOff(context, appWidgetId)
                    else -> WidgetPrefs.iconOn(context, appWidgetId)
                }
                val iconColor = when {
                    periods.isEmpty() -> 0xFF888888.toInt()
                    isOutageNow -> 0xFFE53935.toInt()
                    else -> 0xFF43A047.toInt()
                }
                views.setTextViewText(R.id.tv_icon, iconText)
                views.setTextColor(R.id.tv_icon, iconColor)

                // Таймер
                val timerText: String
                val timerColor: Int
                if (periods.isEmpty()) {
                    timerText = "Немає графіка"
                    timerColor = 0xFF888888.toInt()
                } else if (isOutageNow) {
                    val current = periods.first { nowSec >= it.timeFrom && nowSec < it.timeTo }
                    val left = current.timeTo - nowSec
                    timerText = "${WidgetPrefs.labelUntilOn(context, appWidgetId)} ${formatLeft(left)}"
                    timerColor = WidgetPrefs.colorTimerOff(context, appWidgetId)
                } else {
                    val next = periods.firstOrNull { it.timeFrom > nowSec }
                    if (next != null) {
                        val left = next.timeFrom - nowSec
                        timerText = "${WidgetPrefs.labelUntilOff(context, appWidgetId)} ${formatLeft(left)}"
                        timerColor = WidgetPrefs.colorTimerOn(context, appWidgetId)
                    } else {
                        timerText = "Світло до кінця дня"
                        timerColor = WidgetPrefs.colorTimerOn(context, appWidgetId)
                    }
                }
                views.setTextViewText(R.id.tv_timer, timerText)
                views.setTextColor(R.id.tv_timer, timerColor)

                                // Периоды (сьогодні + завтра)
                if (!isSmall && WidgetPrefs.showPeriods(context, appWidgetId)) {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val sb = StringBuilder()
                    if (periods.isNotEmpty()) {
                        sb.append("Сьогодні:\n")
                        periods.forEach { p ->
                            sb.append("• ").append(sdf.format(Date(p.timeFrom * 1000)))
                              .append("–").append(sdf.format(Date(p.timeTo * 1000))).append("\n")
                        }
                    }
                    if (result.tomorrowPeriods.isNotEmpty()) {
                        if (sb.isNotEmpty()) sb.append("\n")
                        sb.append("Завтра:\n")
                        result.tomorrowPeriods.forEach { p ->
                            sb.append("• ").append(sdf.format(Date(p.timeFrom * 1000)))
                              .append("–").append(sdf.format(Date(p.timeTo * 1000))).append("\n")
                        }
                    }
                    val periodsText = if (sb.isEmpty()) "Немає графіка" else sb.toString().trim()
                    views.setTextViewText(R.id.tv_periods, periodsText)
                    views.setTextColor(R.id.tv_periods, WidgetPrefs.colorPeriods(context, appWidgetId))
                }

                if (theme == 1 && !WidgetPrefs.autoBg(context, appWidgetId)) {
                    views.setTextColor(R.id.tv_timer, 0xFF333333.toInt())
                    if (!isSmall) views.setTextColor(R.id.tv_periods, 0xFF555555.toInt())
                }

            } catch (e: Exception) {
                Log.e(TAG, "fail", e)
                views.setTextViewText(R.id.tv_icon, "!")
                views.setTextViewText(R.id.tv_timer, "Помилка")
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            scheduleTick(context, appWidgetId)
        }

        private fun formatLeft(sec: Long): String {
            val h = sec / 3600
            val m = (sec % 3600) / 60
            return when {
                h > 0 && m > 0 -> "%d год %d хв".format(h, m)
                h > 0 -> "%d год".format(h)
                else -> "%d хв".format(m)
            }
        }

        private fun scheduleTick(context: Context, widgetId: Int) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(context, EnergyWidgetProvider::class.java).apply {
                action = ACTION_TICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val pi = PendingIntent.getBroadcast(context, 6000 + widgetId, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TICK_MS, pi)
                else am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TICK_MS, pi)
            } catch (e: Exception) { Log.e(TAG, "tick", e) }
        }

        private fun cancelTick(context: Context, widgetId: Int) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val i = Intent(context, EnergyWidgetProvider::class.java).apply {
                action = ACTION_TICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            am.cancel(PendingIntent.getBroadcast(context, 6000 + widgetId, i,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        }
    }
}
