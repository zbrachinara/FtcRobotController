package org.electronvolts.evlib.gamepad

import com.qualcomm.robotcore.hardware.Gamepad

typealias EvFunction = (Double) -> Double

enum class InitButton { A, B }

enum class GamepadDigital {
    A, B, X, Y, LEFT_BUMPER, RIGHT_BUMPER, DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
    LEFT_STICK_BUTTON, RIGHT_STICK_BUTTON, BACK, START
}

enum class GamepadAnalog {
    LEFT_STICK_X, LEFT_STICK_Y, RIGHT_STICK_X, RIGHT_STICK_Y, LEFT_TRIGGER, RIGHT_TRIGGER,
}

private typealias Digital = GamepadDigital
private typealias Analog = GamepadAnalog

/**
 * This file was made by the electronVolts, FTC team 7393
 * Date Created: 1/9/16
 *
 * This class wraps a gamepad and adds:
 * Edge detection to the digital inputs (buttons and dpad) {@see DigitalInput}
 * Scaling to the analog inputs (joysticks and triggers) {@see AnalogInput}
 *
 * Specify nothing for scalingFunction to opt out of joystick scaling
 */
class GamepadManager(
    private val detectors: MutableList<DigitalInput>,
    private val digital: Map<GamepadDigital, DigitalInput>,
    private val analog: Map<GamepadAnalog, AnalogInput>,
) {
    val a get() = digital[Digital.A]!!
    val b get() = digital[Digital.B]!!
    val x get() = digital[Digital.X]!!
    val y get() = digital[Digital.Y]!!
    val left_bumper get() = digital[Digital.LEFT_BUMPER]!!
    val right_bumper get() = digital[Digital.RIGHT_BUMPER]!!
    val dpad_up get() = digital[Digital.DPAD_UP]!!
    val dpad_down get() = digital[Digital.DPAD_DOWN]!!
    val dpad_left get() = digital[Digital.DPAD_LEFT]!!
    val dpad_right get() = digital[Digital.DPAD_RIGHT]!!
    val left_stick_button get() = digital[Digital.LEFT_STICK_BUTTON]!!
    val right_stick_button get() = digital[Digital.RIGHT_STICK_BUTTON]!!
    val back get() = digital[Digital.BACK]!!
    val start get() = digital[Digital.START]!!

    val left_stick_x get() = analog[Analog.LEFT_STICK_X]!!
    val left_stick_y get() = analog[Analog.LEFT_STICK_Y]!!
    val right_stick_x get() = analog[Analog.RIGHT_STICK_X]!!
    val right_stick_y get() = analog[Analog.RIGHT_STICK_Y]!!
    val left_trigger get() = analog[Analog.LEFT_TRIGGER]!!
    val right_trigger get() = analog[Analog.RIGHT_TRIGGER]!!

    companion object {
        fun fromGamepad(
            gamepad: Gamepad,
            scalingFunction: EvFunction = { x -> x },
            initButton: InitButton? = null,
        ): GamepadManager {
            val detectors = ArrayList<DigitalInput>()
            val rawStart = DigitalInput({ gamepad.start })
            detectors.add(rawStart)

            val at = when (initButton) {
                InitButton.A -> {
                    val rawA = DigitalInput({ gamepad.a })
                    detectors.add(rawA)
                    ButtonAlive(rawA, rawStart)::isAlive
                }
                InitButton.B -> {
                    val rawB = DigitalInput({ gamepad.b })
                    detectors.add(rawB)
                    ButtonAlive(rawB, rawStart)::isAlive
                }
                null -> {
                    { true }
                }
            }

            //create all the DigitalInput objects
            val digital_inputs = mapOf(
                Pair(Digital.A, DigitalInput({ gamepad.a }, at)),
                Pair(Digital.B, DigitalInput({ gamepad.b }, at)),
                Pair(Digital.X, DigitalInput({ gamepad.x }, at)),
                Pair(Digital.Y, DigitalInput({ gamepad.y }, at)),
                Pair(Digital.LEFT_BUMPER, DigitalInput({ gamepad.left_bumper }, at)),
                Pair(Digital.RIGHT_BUMPER, DigitalInput({ gamepad.right_bumper }, at)),
                Pair(Digital.DPAD_UP, DigitalInput({ gamepad.dpad_up }, at)),
                Pair(Digital.DPAD_DOWN, DigitalInput({ gamepad.dpad_down }, at)),
                Pair(Digital.DPAD_LEFT, DigitalInput({ gamepad.dpad_left }, at)),
                Pair(Digital.DPAD_RIGHT, DigitalInput({ gamepad.dpad_right }, at)),
                Pair(Digital.LEFT_STICK_BUTTON,
                    DigitalInput({ gamepad.left_stick_button }, at)),
                Pair(Digital.RIGHT_STICK_BUTTON,
                    DigitalInput({ gamepad.right_stick_button }, at)),
                Pair(Digital.BACK, DigitalInput({ gamepad.back }, at)),
                Pair(Digital.START, DigitalInput({ gamepad.start }, at)),
            )

            //create all the AnalogInput objects
            val analog_inputs = mapOf(
                Pair(Analog.LEFT_STICK_X,
                    AnalogInput({ gamepad.left_stick_x.toDouble() }, scalingFunction)),
                Pair(Analog.LEFT_STICK_Y,
                    AnalogInput({ gamepad.left_stick_y.toDouble() }, scalingFunction)),
                Pair(Analog.RIGHT_STICK_X,
                    AnalogInput({ gamepad.right_stick_x.toDouble() }, scalingFunction)),
                Pair(Analog.RIGHT_STICK_Y,
                    AnalogInput({ gamepad.right_stick_y.toDouble() }, scalingFunction)),
                Pair(Analog.LEFT_TRIGGER,
                    AnalogInput({ gamepad.left_trigger.toDouble() }, scalingFunction)),
                Pair(Analog.RIGHT_TRIGGER,
                    AnalogInput({ gamepad.right_trigger.toDouble() }, scalingFunction))
            )

            return GamepadManager(
                digital = digital_inputs,
                analog = analog_inputs,
                detectors = detectors,
            )
        }
    }

    fun justTriggered() = digital.values.any { btn -> btn.justPressed() }

    fun update() {
        for (d in detectors) {
            d.update()
        }
        digital.values.forEach { btn -> btn.update() }
        analog.values.forEach { input -> input.update() }
    }

    fun mask(
        mask_digital: List<Digital> = listOf(),
        mask_analog: List<Analog> = listOf(),
    ): GamepadManager {
        return GamepadManager(
            digital = this.digital
                .mapValues { (k, v) -> if (k in mask_digital) DigitalInput.blank() else v },
            analog = this.analog
                .mapValues { (k, v) -> if (k in mask_analog) AnalogInput.blank() else v },
            detectors = this.detectors
        )
    }
}