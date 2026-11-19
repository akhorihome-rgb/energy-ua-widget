package ua.energy.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefs {

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("energy_widget_prefs", Context.MODE_PRIVATE)

    private fun key(id: Int, name: String) = "${name}_$id"

    // === Manual periods (сьогодні) ===
    fun manualDate(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "manual_date"), "") ?: ""

    fun manualPeriods(context: Context, widgetId: Int): List<String> {
        val raw = prefs(context).getString(key(widgetId, "manual_periods"), "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|")
    }

    fun setManualPeriods(context: Context, widgetId: Int, periods: List<String>, date: String) {
        prefs(context).edit()
            .putString(key(widgetId, "manual_periods"), periods.joinToString("|"))
            .putString(key(widgetId, "manual_date"), date)
            .apply()
    }

    // === Manual periods (завтра) ===
    fun manualDateTomorrow(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "manual_date_tmr"), "") ?: ""

    fun manualPeriodsTomorrow(context: Context, widgetId: Int): List<String> {
        val raw = prefs(context).getString(key(widgetId, "manual_periods_tmr"), "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|")
    }

    fun setManualPeriodsTomorrow(context: Context, widgetId: Int, periods: List<String>, date: String) {
        prefs(context).edit()
            .putString(key(widgetId, "manual_periods_tmr"), periods.joinToString("|"))
            .putString(key(widgetId, "manual_date_tmr"), date)
            .apply()
    }

    // === Labels ===
    fun labelUntilOff(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "label_off"), "До вимкнення:") ?: "До вимкнення:"

    fun labelUntilOn(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "label_on"), "До увімкнення:") ?: "До увімкнення:"

    // === Theme & Colors ===
    fun theme(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "theme"), 0)

    fun bgColor(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "bg_color"), 0xFF1A1A2E.toInt())

    fun autoBg(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "auto_bg"), false)

    fun autoBgOnColor(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "auto_bg_on"), 0xFF1B5E20.toInt())

    fun autoBgOffColor(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "auto_bg_off"), 0xFF3E0000.toInt())

    fun colorTimer(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "color_timer"), 0xFFFFFFFF.toInt())

    fun colorTimerOff(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "color_timer_off"), 0xFFFFCC00.toInt())

    fun colorTimerOn(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "color_timer_on"), 0xFF4CAF50.toInt())

    fun colorPeriods(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "color_periods"), 0xFFFFFFFF.toInt())

    fun borderColor(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "border_color"), 0xFF4CAF50.toInt())

    fun borderWidth(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "border_width"), 2)

    // === Sizes & Layout ===
    fun sizeIcon(context: Context, widgetId: Int): Float =
        prefs(context).getFloat(key(widgetId, "size_icon"), 16f)

    fun sizeTimer(context: Context, widgetId: Int): Float =
        prefs(context).getFloat(key(widgetId, "size_timer"), 18f)

    fun sizePeriods(context: Context, widgetId: Int): Float =
        prefs(context).getFloat(key(widgetId, "size_periods"), 14f)

    fun rowGapDp(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "row_gap"), 4)

    fun align(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "align"), 1)

    // === Visibility ===
    fun showIcon(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "show_icon"), true)

    fun showTimer(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "show_timer"), true)

    fun showPeriods(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "show_periods"), true)

    fun showBorder(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "show_border"), false)

    fun onlyNearest(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "only_nearest"), false)

    // === Icons ===
    fun iconOn(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "icon_on"), "✓") ?: "✓"

    fun iconOff(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "icon_off"), "✗") ?: "✗"

    // === Notifications (подвійний таймер) ===
    fun notifyMinutes(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "notify_minutes"), 15)

    fun notifyMinutes2(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "notify_minutes2"), 5)

    fun notifyEnabled1(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "notify_en1"), true)

    fun notifyEnabled2(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "notify_en2"), true)

    fun notifySound(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "notify_sound"), true)

    fun notifyVibrate(context: Context, widgetId: Int): Boolean =
        prefs(context).getBoolean(key(widgetId, "notify_vibrate"), true)


    fun notifySound1(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "notify_sound1"), 1)   // 0=тиша, 1=notification, 2=alarm, 3=ringtone

    fun notifySound2(context: Context, widgetId: Int): Int =
        prefs(context).getInt(key(widgetId, "notify_sound2"), 1)


    fun customSound1(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "custom_sound1"), "") ?: ""

    fun customSound2(context: Context, widgetId: Int): String =
        prefs(context).getString(key(widgetId, "custom_sound2"), "") ?: ""

    fun setCustomSound1(context: Context, widgetId: Int, uri: String) {
        prefs(context).edit().putString(key(widgetId, "custom_sound1"), uri).apply()
    }

    fun setCustomSound2(context: Context, widgetId: Int, uri: String) {
        prefs(context).edit().putString(key(widgetId, "custom_sound2"), uri).apply()
    }

    // === Save ===
    fun save(
        context: Context,
        widgetId: Int,
        bgColor: Int,
        rowGapDp: Int,
        showIcon: Boolean,
        showTimer: Boolean,
        showPeriods: Boolean,
        colorTimer: Int,
        colorPeriods: Int,
        sizeIcon: Float,
        sizeTimer: Float,
        sizePeriods: Float,
        notifyMinutes: Int,
        notifyMinutes2: Int,
        notifyEnabled1: Boolean,
        notifyEnabled2: Boolean,
        align: Int,
        showBorder: Boolean,
        borderColor: Int,
        labelOn: String,
        labelOff: String,
        autoBg: Boolean,
        autoBgOnColor: Int,
        autoBgOffColor: Int,
        onlyNearest: Boolean,
        theme: Int,
        notifySound: Boolean = true,
        notifyVibrate: Boolean,
        iconOn: String,
        iconOff: String,
        notifySound1: Int = 1,
        notifySound2: Int = 1
    ) {
        prefs(context).edit()
            .putInt(key(widgetId, "bg_color"), bgColor)
            .putInt(key(widgetId, "row_gap"), rowGapDp)
            .putBoolean(key(widgetId, "show_icon"), showIcon)
            .putBoolean(key(widgetId, "show_timer"), showTimer)
            .putBoolean(key(widgetId, "show_periods"), showPeriods)
            .putInt(key(widgetId, "color_timer"), colorTimer)
            .putInt(key(widgetId, "color_periods"), colorPeriods)
            .putFloat(key(widgetId, "size_icon"), sizeIcon)
            .putFloat(key(widgetId, "size_timer"), sizeTimer)
            .putFloat(key(widgetId, "size_periods"), sizePeriods)
            .putInt(key(widgetId, "notify_minutes"), notifyMinutes)
            .putInt(key(widgetId, "align"), align)
            .putBoolean(key(widgetId, "show_border"), showBorder)
            .putInt(key(widgetId, "border_color"), borderColor)
            .putString(key(widgetId, "label_on"), labelOn)
            .putString(key(widgetId, "label_off"), labelOff)
            .putBoolean(key(widgetId, "auto_bg"), autoBg)
            .putInt(key(widgetId, "auto_bg_on"), autoBgOnColor)
            .putInt(key(widgetId, "auto_bg_off"), autoBgOffColor)
            .putBoolean(key(widgetId, "only_nearest"), onlyNearest)
            .putInt(key(widgetId, "theme"), theme)
            .putBoolean(key(widgetId, "notify_sound"), notifySound)
            .putBoolean(key(widgetId, "notify_vibrate"), notifyVibrate)
            .putInt(key(widgetId, "notify_minutes2"), notifyMinutes2)
            .putBoolean(key(widgetId, "notify_en1"), notifyEnabled1)
            .putBoolean(key(widgetId, "notify_en2"), notifyEnabled2)
            .putString(key(widgetId, "icon_on"), iconOn)
            .putString(key(widgetId, "icon_off"), iconOff)
            .putInt(key(widgetId, "notify_sound1"), notifySound1)
            .putInt(key(widgetId, "notify_sound2"), notifySound2)
            .apply()
    }
}
