package ua.energy.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context

class EnergyWidgetProviderLarge : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { EnergyWidgetProvider.updateAppWidget(context, appWidgetManager, it) }
    }
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach {
            try { OutageNotifier.cancel(context, it) } catch (_: Exception) {}
        }
    }
}
