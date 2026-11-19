package ua.energy.widget

data class OutagePeriod(
    val timeFrom: Long,
    val timeTo: Long,
    val status: String = "red"
)
