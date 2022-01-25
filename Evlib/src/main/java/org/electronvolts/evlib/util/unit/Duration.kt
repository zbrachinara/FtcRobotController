package org.electronvolts.evlib.util.unit

class Duration private constructor(
    private val seconds: Double
) : ContinuousUnit<Duration> {
    override fun raw() = seconds
    override fun fromRaw(raw: Double) = Duration(raw)

    companion object {
        fun fromSeconds(v: Double) = Duration(v)
        fun fromMilliseconds(v: Double) = Duration(v / SEC_PER_MSC)
        fun fromMinutes(v: Double) = Duration(v / SEC_PER_MIN)
        fun fromHours(v: Double) = Duration(v / SEC_PER_HOR)
    }

    fun milliseconds() = seconds * SEC_PER_MSC
    fun seconds() = seconds
    fun minutes() = seconds * SEC_PER_MIN
    fun hours() = seconds * SEC_PER_HOR

}