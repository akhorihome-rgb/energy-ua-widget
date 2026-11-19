package ua.energy.widget

import android.Manifest
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.widget.TextView
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import java.util.Calendar
import java.util.TimeZone

class ConfigureActivity : Activity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val REQUEST_SOUND1 = 1001
    private val REQUEST_SOUND2 = 1002

    private val sizes = listOf(10f, 12f, 14f, 16f, 18f, 20f, 22f, 24f, 28f)
    private val gaps = listOf(0, 2, 4, 6, 8, 10, 12, 16)
    private val notifyOptions = listOf(0, 5, 10, 15, 20, 30, 45, 60)
    private val alignOptions = listOf("Ліворуч", "По центру", "Праворуч")
    private val themeOptions = listOf("Темна", "Світла")
    private val iconOnOptions = listOf("✓", "✅", "💡", "⚡", "🔌", "🔋", "🌞", "🟢")
    private val iconOffOptions = listOf("✗", "❌", "🌑", "⚡", "🔌", "🔋", "🔴", "⚫")
    private val soundOptions = listOf("Тиша", "Сповіщення", "Будильник", "Рінгтон")

    private val colors = listOf(
        "Білий" to 0xFFFFFFFF.toInt(),
        "Жовтий" to 0xFFFFCC00.toInt(),
        "Світло-сірий" to 0xFFDDDDDD.toInt(),
        "Сірий" to 0xFF888888.toInt(),
        "Зелений" to 0xFF4CAF50.toInt(),
        "Червоний" to 0xFFF44336.toInt(),
        "Блакитний" to 0xFF03A9F4.toInt(),
        "Помаранчевий" to 0xFFFF9800.toInt()
    )
    private val bgColors = listOf(
        "Темно-синій" to 0xFF1A1A2E.toInt(),
        "Чорний" to 0xFF000000.toInt(),
        "Темно-сірий" to 0xFF212121.toInt(),
        "Темно-зелений" to 0xFF1B5E20.toInt(),
        "Темно-червоний" to 0xFF3E0000.toInt(),
        "Білий" to 0xFFFFFFFF.toInt(),
        "Світло-сірий" to 0xFFF5F5F5.toInt()
    )
    private val borderColors = listOf(
        "Зелений" to 0xFF4CAF50.toInt(),
        "Білий" to 0xFFFFFFFF.toInt(),
        "Жовтий" to 0xFFFFCC00.toInt(),
        "Сірий" to 0xFF888888.toInt(),
        "Червоний" to 0xFFF44336.toInt(),
        "Блакитний" to 0xFF03A9F4.toInt()
    )

    private lateinit var etFrom: Array<EditText>
    private lateinit var etTo: Array<EditText>
    private lateinit var etFromT: Array<EditText>
    private lateinit var etToT: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        setContentView(R.layout.activity_configure)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

        etFrom = arrayOf(
            findViewById(R.id.et_from_1), findViewById(R.id.et_from_2), findViewById(R.id.et_from_3),
            findViewById(R.id.et_from_4), findViewById(R.id.et_from_5), findViewById(R.id.et_from_6),
            findViewById(R.id.et_from_7)
        )
        etTo = arrayOf(
            findViewById(R.id.et_to_1), findViewById(R.id.et_to_2), findViewById(R.id.et_to_3),
            findViewById(R.id.et_to_4), findViewById(R.id.et_to_5), findViewById(R.id.et_to_6),
            findViewById(R.id.et_to_7)
        )
        etFromT = arrayOf(
            findViewById(R.id.et_from_t1), findViewById(R.id.et_from_t2), findViewById(R.id.et_from_t3),
            findViewById(R.id.et_from_t4), findViewById(R.id.et_from_t5), findViewById(R.id.et_from_t6),
            findViewById(R.id.et_from_t7)
        )
        etToT = arrayOf(
            findViewById(R.id.et_to_t1), findViewById(R.id.et_to_t2), findViewById(R.id.et_to_t3),
            findViewById(R.id.et_to_t4), findViewById(R.id.et_to_t5), findViewById(R.id.et_to_t6),
            findViewById(R.id.et_to_t7)
        )

        val today = todayKyiv()
        if (WidgetPrefs.manualDate(this, appWidgetId) == today) {
            WidgetPrefs.manualPeriods(this, appWidgetId).forEachIndexed { i, s ->
                if (i < 7) {
                    val parts = s.split("-")
                    if (parts.size == 2) {
                        etFrom[i].setText(parts[0].trim())
                        etTo[i].setText(parts[1].trim())
                    }
                }
            }
        }

        val tomorrow = tomorrowKyiv()
        if (WidgetPrefs.manualDateTomorrow(this, appWidgetId) == tomorrow) {
            WidgetPrefs.manualPeriodsTomorrow(this, appWidgetId).forEachIndexed { i, s ->
                if (i < 7) {
                    val parts = s.split("-")
                    if (parts.size == 2) {
                        etFromT[i].setText(parts[0].trim())
                        etToT[i].setText(parts[1].trim())
                    }
                }
            }
        }

        findViewById<EditText>(R.id.et_label_off).setText(WidgetPrefs.labelUntilOff(this, appWidgetId))
        findViewById<EditText>(R.id.et_label_on).setText(WidgetPrefs.labelUntilOn(this, appWidgetId))

        val spTheme = findViewById<Spinner>(R.id.spinner_theme)
        val spBg = findViewById<Spinner>(R.id.spinner_bg_color)
        val spAutoOn = findViewById<Spinner>(R.id.spinner_auto_on)
        val spAutoOff = findViewById<Spinner>(R.id.spinner_auto_off)
        val spAlign = findViewById<Spinner>(R.id.spinner_align)
        val spGap = findViewById<Spinner>(R.id.spinner_gap)
        val spBorderColor = findViewById<Spinner>(R.id.spinner_border_color)
        val spNotify1 = findViewById<Spinner>(R.id.spinner_notify1)
        val spNotify2 = findViewById<Spinner>(R.id.spinner_notify2)
        val spSound1 = findViewById<Spinner>(R.id.spinner_sound1)
        val spSound2 = findViewById<Spinner>(R.id.spinner_sound2)
        val btnPick1 = findViewById<Button>(R.id.btn_pick_sound1)
        val btnPick2 = findViewById<Button>(R.id.btn_pick_sound2)
        val tvSound1 = findViewById<TextView>(R.id.tv_sound1_path)
        val tvSound2 = findViewById<TextView>(R.id.tv_sound2_path)
        val spCT = findViewById<Spinner>(R.id.spinner_c_timer)
        val spCP = findViewById<Spinner>(R.id.spinner_c_periods)
        val spSI = findViewById<Spinner>(R.id.spinner_s_icon)
        val spST = findViewById<Spinner>(R.id.spinner_s_timer)
        val spSP = findViewById<Spinner>(R.id.spinner_s_periods)
        val spIconOn = findViewById<Spinner>(R.id.spinner_icon_on)
        val spIconOff = findViewById<Spinner>(R.id.spinner_icon_off)

        val cbIcon = findViewById<CheckBox>(R.id.cb_icon)
        val cbT = findViewById<CheckBox>(R.id.cb_timer)
        val cbP = findViewById<CheckBox>(R.id.cb_periods)
        val cbBorder = findViewById<CheckBox>(R.id.cb_border)
        val cbAutoBg = findViewById<CheckBox>(R.id.cb_auto_bg)
        val cbOnlyNearest = findViewById<CheckBox>(R.id.cb_only_nearest)
        val cbVibrate = findViewById<CheckBox>(R.id.cb_vibrate)
        val cbNotify1 = findViewById<CheckBox>(R.id.cb_notify1)
        val cbNotify2 = findViewById<CheckBox>(R.id.cb_notify2)

        fun ca() = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors.map { it.first })
        fun sa() = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sizes.map { "${it.toInt()} sp" })
        fun ba() = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, bgColors.map { it.first })
        fun na() = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, notifyOptions.map { if (it == 0) "Вимкнено" else "$it хв" })

        spTheme.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, themeOptions)
        spBg.adapter = ba()
        spAutoOn.adapter = ba()
        spAutoOff.adapter = ba()
        spAlign.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, alignOptions)
        spGap.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, gaps.map { "$it dp" })
        spBorderColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, borderColors.map { it.first })
        spNotify1.adapter = na()
        spNotify2.adapter = na()
        spSound1.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, soundOptions)
        spSound2.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, soundOptions)
        spCT.adapter = ca()
        spCP.adapter = ca()
        spSI.adapter = sa()
        spST.adapter = sa()
        spSP.adapter = sa()
        spIconOn.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, iconOnOptions)
        spIconOff.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, iconOffOptions)

        fun idxC(v: Int, list: List<Pair<String, Int>>) = list.indexOfFirst { it.second == v }.let { if (it >= 0) it else 0 }
        fun idxS(s: Float) = sizes.indexOfFirst { it == s }.let { if (it >= 0) it else 2 }
        fun idxG(g: Int) = gaps.indexOfFirst { it == g }.let { if (it >= 0) it else 2 }
        fun idxN(n: Int) = notifyOptions.indexOfFirst { it == n }.let { if (it >= 0) it else 3 }
        fun idxIcon(list: List<String>, v: String) = list.indexOf(v).let { if (it >= 0) it else 0 }

        spTheme.setSelection(WidgetPrefs.theme(this, appWidgetId).coerceIn(0, 1))
        spBg.setSelection(idxC(WidgetPrefs.bgColor(this, appWidgetId), bgColors))
        spAutoOn.setSelection(idxC(WidgetPrefs.autoBgOnColor(this, appWidgetId), bgColors))
        spAutoOff.setSelection(idxC(WidgetPrefs.autoBgOffColor(this, appWidgetId), bgColors))
        spAlign.setSelection(WidgetPrefs.align(this, appWidgetId).coerceIn(0, 2))
        spGap.setSelection(idxG(WidgetPrefs.rowGapDp(this, appWidgetId)))
        spBorderColor.setSelection(idxC(WidgetPrefs.borderColor(this, appWidgetId), borderColors))
        spNotify1.setSelection(idxN(WidgetPrefs.notifyMinutes(this, appWidgetId)))
        spNotify2.setSelection(idxN(WidgetPrefs.notifyMinutes2(this, appWidgetId)))
        spSound1.setSelection(WidgetPrefs.notifySound1(this, appWidgetId).coerceIn(0, 3))
        spSound2.setSelection(WidgetPrefs.notifySound2(this, appWidgetId).coerceIn(0, 3))
        spCT.setSelection(idxC(WidgetPrefs.colorTimer(this, appWidgetId), colors))
        spCP.setSelection(idxC(WidgetPrefs.colorPeriods(this, appWidgetId), colors))
        spSI.setSelection(idxS(WidgetPrefs.sizeIcon(this, appWidgetId)))
        spST.setSelection(idxS(WidgetPrefs.sizeTimer(this, appWidgetId)))
        spSP.setSelection(idxS(WidgetPrefs.sizePeriods(this, appWidgetId)))
        spIconOn.setSelection(idxIcon(iconOnOptions, WidgetPrefs.iconOn(this, appWidgetId)))
        spIconOff.setSelection(idxIcon(iconOffOptions, WidgetPrefs.iconOff(this, appWidgetId)))

        cbIcon.isChecked = WidgetPrefs.showIcon(this, appWidgetId)
        cbT.isChecked = WidgetPrefs.showTimer(this, appWidgetId)
        cbP.isChecked = WidgetPrefs.showPeriods(this, appWidgetId)
        cbBorder.isChecked = WidgetPrefs.showBorder(this, appWidgetId)
        cbAutoBg.isChecked = WidgetPrefs.autoBg(this, appWidgetId)
        cbOnlyNearest.isChecked = WidgetPrefs.onlyNearest(this, appWidgetId)
        cbVibrate.isChecked = WidgetPrefs.notifyVibrate(this, appWidgetId)
        cbNotify1.isChecked = WidgetPrefs.notifyEnabled1(this, appWidgetId)
        cbNotify2.isChecked = WidgetPrefs.notifyEnabled2(this, appWidgetId)

        fun shortName(uriStr: String): String {
            if (uriStr.isBlank()) return "не обрано"
            return try {
                val uri = Uri.parse(uriStr)
                var name = uri.lastPathSegment ?: uriStr
                contentResolver.query(uri, null, null, null, null)?.use { c ->
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0 && c.moveToFirst()) name = c.getString(idx)
                }
                name
            } catch (_: Exception) { uriStr.takeLast(40) }
        }
        tvSound1.text = "Файл: ${shortName(WidgetPrefs.customSound1(this, appWidgetId))}"
        tvSound2.text = "Файл: ${shortName(WidgetPrefs.customSound2(this, appWidgetId))}"

        btnPick1.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(i, REQUEST_SOUND1)
        }
        btnPick2.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "audio/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            }
            startActivityForResult(i, REQUEST_SOUND2)
        }

        findViewById<Button>(R.id.btn_save).setOnClickListener {
            val list = mutableListOf<String>()
            for (i in 0..6) {
                val f = etFrom[i].text.toString().trim()
                val t = etTo[i].text.toString().trim()
                if (f.isNotEmpty() && t.isNotEmpty()) list.add("$f-$t")
            }
            WidgetPrefs.setManualPeriods(this, appWidgetId, list, todayKyiv())

            val listT = mutableListOf<String>()
            for (i in 0..6) {
                val f = etFromT[i].text.toString().trim()
                val t = etToT[i].text.toString().trim()
                if (f.isNotEmpty() && t.isNotEmpty()) listT.add("$f-$t")
            }
            WidgetPrefs.setManualPeriodsTomorrow(this, appWidgetId, listT, tomorrowKyiv())

            val labelOff = findViewById<EditText>(R.id.et_label_off).text.toString().ifBlank { "До вимкнення:" }
            val labelOn = findViewById<EditText>(R.id.et_label_on).text.toString().ifBlank { "До увімкнення:" }

            WidgetPrefs.save(
                this, appWidgetId,
                bgColor = bgColors[spBg.selectedItemPosition].second,
                rowGapDp = gaps[spGap.selectedItemPosition],
                showIcon = cbIcon.isChecked,
                showTimer = cbT.isChecked,
                showPeriods = cbP.isChecked,
                colorTimer = colors[spCT.selectedItemPosition].second,
                colorPeriods = colors[spCP.selectedItemPosition].second,
                sizeIcon = sizes[spSI.selectedItemPosition],
                sizeTimer = sizes[spST.selectedItemPosition],
                sizePeriods = sizes[spSP.selectedItemPosition],
                notifyMinutes = notifyOptions[spNotify1.selectedItemPosition],
                notifyMinutes2 = notifyOptions[spNotify2.selectedItemPosition],
                notifyEnabled1 = cbNotify1.isChecked,
                notifyEnabled2 = cbNotify2.isChecked,
                align = spAlign.selectedItemPosition,
                showBorder = cbBorder.isChecked,
                borderColor = borderColors[spBorderColor.selectedItemPosition].second,
                labelOn = labelOn,
                labelOff = labelOff,
                autoBg = cbAutoBg.isChecked,
                autoBgOnColor = bgColors[spAutoOn.selectedItemPosition].second,
                autoBgOffColor = bgColors[spAutoOff.selectedItemPosition].second,
                onlyNearest = cbOnlyNearest.isChecked,
                theme = spTheme.selectedItemPosition,
                notifyVibrate = cbVibrate.isChecked,
                notifySound = true,
                iconOn = iconOnOptions[spIconOn.selectedItemPosition],
                iconOff = iconOffOptions[spIconOff.selectedItemPosition],
                notifySound1 = spSound1.selectedItemPosition,
                notifySound2 = spSound2.selectedItemPosition
            )

            try {
                val result = OutageFetcher.fetchTodayPeriods(this, appWidgetId)
                OutageNotifier.scheduleNext(this, appWidgetId, result.periods)
            } catch (_: Exception) {}

            val mgr = AppWidgetManager.getInstance(this)
            EnergyWidgetProvider.updateAppWidget(this, mgr, appWidgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)
            Toast.makeText(this, "Збережено", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data?.data == null) return
        val uri = data.data!!
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: Exception) {}
        val uriStr = uri.toString()
        if (requestCode == REQUEST_SOUND1) {
            WidgetPrefs.setCustomSound1(this, appWidgetId, uriStr)
            findViewById<TextView>(R.id.tv_sound1_path).text = "Файл: обрано"
            Toast.makeText(this, "Звук 1 збережено", Toast.LENGTH_SHORT).show()
        } else if (requestCode == REQUEST_SOUND2) {
            WidgetPrefs.setCustomSound2(this, appWidgetId, uriStr)
            findViewById<TextView>(R.id.tv_sound2_path).text = "Файл: обрано"
            Toast.makeText(this, "Звук 2 збережено", Toast.LENGTH_SHORT).show()
        }
    }

    private fun todayKyiv(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kyiv"))
        return "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }

    private fun tomorrowKyiv(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Kyiv"))
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return "%04d-%02d-%02d".format(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    }
}
