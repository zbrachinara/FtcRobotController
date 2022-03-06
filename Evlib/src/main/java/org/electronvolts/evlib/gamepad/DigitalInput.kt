package org.electronvolts.evlib.gamepad

/**
 * A note on this class:
 *
 * By providing the `extractor` and `alive` lambdas, you are guaranteeing that at the creation of
 * this class, the values returned from those lambdas are accurate (not when you call update!)
 */
class DigitalInput(
    private val extractor: () -> Boolean,
    private val alive: () -> Boolean = { true },
) : () -> Boolean {

    // set currentValue and previousValue to the reading so no edges are triggered
    private var value = extractor()
    private var previousValue = extractor()

    fun update(): Boolean {
        if (alive()) {
            previousValue = value
            value = extractor()
        }
        return value
    }

    fun justPressed(): Boolean {
        return value && !previousValue
    }

    fun justReleased(): Boolean {
        return !value && previousValue
    }

    override fun invoke() = extractor()

}