package org.electronvolts.evlib.util.unit

class Instant private constructor(
    private val ms: Long
) : DiscreteUnit<Instant> {
    override fun fromRaw(raw: Long) = Instant(raw)
    override fun raw() = ms

    companion object {
        fun now() = Instant(System.currentTimeMillis())
    }

    fun milliseconds() = ms
    fun seconds() = ms * MSC_PER_SEC
    fun minutes() = ms * MSC_PER_MIN
    fun hours() = ms * MSC_PER_HOR

}