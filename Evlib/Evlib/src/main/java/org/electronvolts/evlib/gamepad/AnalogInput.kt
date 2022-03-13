package org.electronvolts.evlib.gamepad

/**
 * This file was made by the electronVolts, FTC team 7393
 *
 * Manages the scaling of an analog input such as a joystick
 */
class AnalogInput(
    private val extractor: () -> Double,
    private val inputScaler: EvFunction,
) : () -> Double {
    var rawValue = 0.0
        private set
    var value = 0.0
        private set

    /**
     * updates the output value and raw value
     *
     * @return the scaled value
     */
    fun update(): Double {
        rawValue = extractor()
        value = inputScaler(rawValue)
        return value
    }

    override fun invoke(): Double = value

}
