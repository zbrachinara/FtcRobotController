package org.electronvolts.evlib.util.unit

fun Int.dunit(): DiscreteUnit<Int> {
    val i = this
    return object : DiscreteUnit<Int> {
        override fun fromRaw(raw: Long) = raw.toInt()
        override fun raw() = i.toLong()
    }
}

fun Long.dunit(): DiscreteUnit<Long> {
    val i = this
    return object : DiscreteUnit<Long> {
        override fun fromRaw(raw: Long) = raw
        override fun raw() = i
    }
}

fun Double.cunit(): ContinuousUnit<Double> {
    val i = this
    return object : ContinuousUnit<Double> {
        override fun fromRaw(raw: Double) = raw
        override fun raw() = i
    }
}

interface DiscreteUnit<Unit> : Comparable<DiscreteUnit<Unit>> {
    fun fromRaw(raw: Long): Unit
    fun raw(): Long

    // This will allow any unit to be combined with any other unit
    // kind of flawed design choice, but it's the only way to cleanly implement scaling

    operator fun <RHS> plus(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() + rhs.raw())

    operator fun <RHS> minus(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() - rhs.raw())

    operator fun <RHS> times(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() * rhs.raw())

    operator fun <RHS> div(rhs: DiscreteUnit<RHS>) = fromRaw(this.raw() / rhs.raw())

    fun zero() = fromRaw(0)

    override fun compareTo(other: DiscreteUnit<Unit>) = this.raw().compareTo(other.raw())
}

interface ContinuousUnit<Unit> : Comparable<ContinuousUnit<Unit>> {
    fun fromRaw(raw: Double): Unit
    fun raw(): Double

    // This will allow any unit to be combined with any other unit
    // kind of flawed design choice, but it's the only way to cleanly implement scaling

    operator fun <RHS> plus(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() + rhs.raw())

    operator fun <RHS> minus(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() - rhs.raw())

    operator fun <RHS> times(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() * rhs.raw())

    operator fun <RHS> div(rhs: ContinuousUnit<RHS>) = fromRaw(this.raw() / rhs.raw())

    fun zero() = fromRaw(0.0)

    override fun compareTo(other: ContinuousUnit<Unit>) = this.raw().compareTo(other.raw())

}