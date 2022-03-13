package org.electronvolts.evlib.opmode

import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.gamepad.EvFunction
import org.electronvolts.evlib.gamepad.GamepadManager
import org.electronvolts.evlib.gamepad.InitButton
import org.electronvolts.evlib.util.unit.Duration

abstract class AbstractTeleOp<Config : RobotCfg> : AbstractOpMode<Config>() {

    protected lateinit var driver1: GamepadManager
    protected lateinit var driver2: GamepadManager

    protected val joystickScale: EvFunction = { x -> x }

    override val matchTime = Duration.fromMinutes(2.0)

    final override fun preSetup() {
        // stage 1
        driver1 = GamepadManager.fromGamepad(gamepad1, joystickScale)
        driver2 = GamepadManager.fromGamepad(gamepad2, joystickScale)

        // stage 2
        driver1 = GamepadManager.fromGamepad(gamepad1, joystickScale, InitButton.A)
        driver2 = GamepadManager.fromGamepad(gamepad1, joystickScale, InitButton.B)
    }

    final override fun preAct() {
        driver1.update()
        driver2.update()
    }

    final override fun postAct() {
        // empty
    }
}