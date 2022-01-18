package electronvolts.util.unit

fun Int.cunit(): ContinuousUnit<Int> {
    val i = this
    return object : ContinuousUnit<Int> {
        override fun fromRaw(raw: Double) = raw.toInt()
        override fun raw() = (i.toDouble())
    }
}

fun Double.cunit(): ContinuousUnit<Double> {
    val i = this
    return object : ContinuousUnit<Double> {
        override fun fromRaw(raw: Double) = raw
        override fun raw() = i
    }
}

interface ContinuousUnit<Unit> {
    fun fromRaw(raw: Double): Unit
    fun raw(): Double

    // This will allow any unit to be combined with any other unit
    // kind of flawed design choice, but it's the only way to cleanly implement scaling

    fun <RHS> plus(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() + rhs.raw())

    fun <RHS> minus(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() - rhs.raw())

    fun <RHS> times(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() * rhs.raw())

    fun <RHS> div(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() / rhs.raw())

    fun zero() = fromRaw(0.0)

}