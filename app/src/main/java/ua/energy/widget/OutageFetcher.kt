package ua.energy.widget

import java.util.Calendar
import java.util.TimeZone

data class FetchResult(
    val periods: List<OutagePeriod>,
    val fromCache: Boolean = false,
    val debug: String = "",
    val tomorrowPeriods: List<OutagePeriod> = emptyList()
)

object OutageFetcher {

    fun fetchTodayPeriods(
        context: android.content.Context? = null,
        widgetId: Int = 0,
        city: String = "",
        group: String = "5.2"
    ): FetchResult {
        if (context == null) {
            return FetchResult(emptyList(), false, "немає контексту")
        }

        // Авто-перенос: якщо "завтра" вже стало "сьогодні" — переносимо
        promoteTomorrowIfNeeded(context, widgetId)

        val today = todayKyivDate()
        val tomorrow = tomorrowKyivDate()

        val manualDate = WidgetPrefs.manualDate(context, widgetId)
        val raw = WidgetPrefs.manualPeriods(context, widgetId)
        val periods = if (raw.isNotEmpty() && manualDate == today) {
            parseManualPeriods(raw, dayOffset = 0)
        } else emptyList()

        val manualDateT = WidgetPrefs.manualDateTomorrow(context, widgetId)
        val rawT = WidgetPrefs.manualPeriodsTomorrow(context, widgetId)
        val tomorrowPeriods = if (rawT.isNotEmpty() && manualDateT == tomorrow) {
            parseManualPeriods(rawT, dayOffset = 1)
        } else emptyList()

        val debug = buildString {
            append(if (periods.isEmpty()) "сьогодні: немає | " else "сьогодні: ${periods.size} | ")
            append(if (tomorrowPeriods.isEmpty()) "завтра: немає" else "завтра: ${tomorrowPeriods.size}")
        }

        return FetchResult(periods, true, debug, tomorrowPeriods)
    }

    /** Якщо дата "завтра" вже настала — робимо її "сьогодні" і очищаємо завтра. */
    private fun promoteTomorrowIfNeeded(context: android.content.Context, widgetId: Int) {
        val today = todayKyivDate()
        val tmrDate = WidgetPrefs.manualDateTomorrow(context, widgetId)
        if (tmrDate.isNotEmpty() && tmrDate == today) {
            val tmrPeriods = WidgetPrefs.manualPeriodsTomorrow(context, widgetId)
            if (tmrPeriods.isNotEmpty()) {
                WidgetPrefs.setManualPeriods(context, widgetId, tmrPeriods, today)
            }
            WidgetPrefs.setManualPeriodsTomorrow(context, widgetId, emptyList(), "")
        }
    }

    fun parseManualPeriods(raw: List<String>, dayOffset: Int = 0): List<OutagePeriod> {
        val dayStart = kyivDayStartSec() + dayOffset * 86400L
        val result = mutableListOf<OutagePeriod>()
        for (s in raw) {
            val parts = s.split("-")
            if (parts.size != 2) continue
            val fromSec = parseHm(parts[0].trim())
            val toSec = parseHm(parts[1].trim())
            if (fromSec == null || toSec == null) continue
            // Дозволяємо toSec == 0 (00:00 / 24:00) як кінець доби
            val end = if (toSec == 0L && fromSec > 0L) 86400L else toSec
            if (end <= fromSec) continue
            result.add(OutagePeriod(dayStart + fromSec, dayStart + end))
        }
        return result.sortedBy { it.timeFrom }
    }

    /** Приймає 00:00, 0:00, 24:00 */
    private fun parseHm(hm: String): Long? {
        val m = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(hm.trim()) ?: return null
        var h = m.groupValues[1].toIntOrNull() ?: return null
        val min = m.groupValues[2].toIntOrNull() ?: return null
        if (min !in 0..59) return null
        if (h == 24 && min == 0) h = 0          // 24:00 → 00:00
        if (h !in 0..23) return null
        return h * 3600L + min * 60L
    }

    fun kyivDayStartSec(): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kyiv"))
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / 1000
    }

    private fun todayKyivDate(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kyiv"))
        return "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    private fun tomorrowKyivDate(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kyiv"))
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}
