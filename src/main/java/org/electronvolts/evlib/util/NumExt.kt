package electronvolts.util

fun Int.clamp(min: Int, max: Int): Int = when {
    this > max -> max
    this < min -> min
    else -> this
}