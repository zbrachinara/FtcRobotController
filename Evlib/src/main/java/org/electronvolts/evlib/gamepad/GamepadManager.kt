package org.electronvolts.evlib.gamepad

import com.qualcomm.robotcore.hardware.Gamepad
import java.util.ArrayList

enum class InitButton { A, B }

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
    gamepad: Gamepad,
    scalingFunction: Function = { x -> x },
    initButton: InitButton? = null,
) {

    //this stores all the wrapped digital inputs
    val a: DigitalInput
    val b: DigitalInput
    val x: DigitalInput
    val y: DigitalInput
    val left_bumper: DigitalInput
    val right_bumper: DigitalInput
    val dpad_up: DigitalInput
    val dpad_down: DigitalInput
    val dpad_left: DigitalInput
    val dpad_right: DigitalInput
    val left_stick_button: DigitalInput
    val right_stick_button: DigitalInput
    val back: DigitalInput
    val start: DigitalInput

    //this stores all the wrapped analog inputs
    val left_stick_x: AnalogInput
    val left_stick_y: AnalogInput
    val right_stick_x: AnalogInput
    val right_stick_y: AnalogInput
    val left_trigger: AnalogInput
    val right_trigger: AnalogInput
    private val detectors: MutableList<DigitalInput>

    //TODO: More pragmatic way to do this perhaps..?
    fun justTriggered(): Boolean {
        return a.justPressed() || b.justPressed() || x.justPressed() || y.justPressed() ||
                left_bumper.justPressed() || right_bumper.justPressed() ||
                dpad_up.justPressed() || dpad_down.justPressed() || dpad_left.justPressed() || dpad_right.justPressed() ||
                left_stick_button.justPressed() || right_stick_button.justPressed() ||
                back.justPressed() || start.justPressed()
    }

    fun update() {
        //update all the values
        for (d in detectors) {
            d.update()
        }
        a.update()
        b.update()
        x.update()
        y.update()
        left_bumper.update()
        right_bumper.update()
        left_trigger.update()
        right_trigger.update()
        dpad_up.update()
        dpad_down.update()
        dpad_left.update()
        dpad_right.update()
        left_stick_button.update()
        right_stick_button.update()
        back.update()
        start.update()
        left_stick_x.update()
        left_stick_y.update()
        right_stick_x.update()
        right_stick_y.update()
    }

    //use this constructor for custom joystick scaling
    init {
        detectors = ArrayList<DigitalInput>()
        val rawStart = DigitalInput({ gamepad.start })
        detectors.add(rawStart)
//        val at: AlivenessTester

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
        a = DigitalInput({ gamepad.a }, at)
        b = DigitalInput({ gamepad.b }, at)
        x = DigitalInput({ gamepad.x })
        y = DigitalInput({ gamepad.y })
        left_bumper = DigitalInput({ gamepad.left_bumper })
        right_bumper = DigitalInput({ gamepad.right_bumper })
        dpad_up = DigitalInput({ gamepad.dpad_up })
        dpad_down = DigitalInput({ gamepad.dpad_down })
        dpad_left = DigitalInput({ gamepad.dpad_left })
        dpad_right = DigitalInput({ gamepad.dpad_right })
        left_stick_button = DigitalInput({ gamepad.left_stick_button })
        right_stick_button = DigitalInput({ gamepad.right_stick_button })
        back = DigitalInput({ gamepad.back })
        start = DigitalInput({ gamepad.start }, at)

        //create all the AnalogInput objects
        left_stick_x = AnalogInput({ gamepad.left_stick_x.toDouble() }, scalingFunction)
        left_stick_y = AnalogInput({ gamepad.left_stick_y.toDouble() }, scalingFunction)
        right_stick_x = AnalogInput({ gamepad.right_stick_x.toDouble() }, scalingFunction)
        right_stick_y = AnalogInput({ gamepad.right_stick_y.toDouble() }, scalingFunction)
        left_trigger = AnalogInput({ gamepad.left_trigger.toDouble() }, scalingFunction)
        right_trigger = AnalogInput({ gamepad.right_trigger.toDouble() }, scalingFunction)
    }
}