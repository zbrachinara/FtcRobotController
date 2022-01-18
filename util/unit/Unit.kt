package electronvolts.util.unit

fun Int.dunit(): DiscreteUnit<Int> {
    val i = this
    return object : DiscreteUnit<Int> {
        override fun fromRaw(raw: Long) = raw.toInt()
        override fun raw() = i.toLong()
    }
}

fun Double.cunit(): ContinuousUnit<Double> {
    val i = this
    return object : ContinuousUnit<Double> {
        override fun fromRaw(raw: Double) = raw
        override fun raw() = i
    }
}

interface DiscreteUnit<Unit> {
    fun fromRaw(raw: Long): Unit
    fun raw(): Long

    // This will allow any unit to be combined with any other unit
    // kind of flawed design choice, but it's the only way to cleanly implement scaling

    fun <RHS> plus(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() + rhs.raw())

    fun <RHS> minus(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() - rhs.raw())

    fun <RHS> times(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() * rhs.raw())

    fun <RHS> div(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() / rhs.raw())

    fun zero() = fromRaw(0)
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