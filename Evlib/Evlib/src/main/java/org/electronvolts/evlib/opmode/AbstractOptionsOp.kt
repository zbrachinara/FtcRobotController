package org.electronvolts.evlib.opmode

import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.blankCfg
import org.electronvolts.evlib.gamepad.GamepadDigital
import org.electronvolts.evlib.options.OptionClass
import org.electronvolts.evlib.options.OptionFile
import org.electronvolts.evlib.util.Path
import org.electronvolts.evlib.util.clamp

abstract class AbstractOptionsOp : AbstractTeleOp<RobotCfg>() {
    protected abstract val options: List<OptionClass<Any>>
    abstract fun filePath(): Path
    private val file by lazy { OptionFile(filePath()) }

    private var index = 0

    private fun display() {
        TODO("Not yet implemented")
    }

    private fun requestMutate(): Boolean {
        val option = options[index]
        val data = file.get(option)

        return when (val out = option.typeData.mutator(
            driver1.mask(mask_digital = listOf(
                GamepadDigital.BACK,
                GamepadDigital.START,
                GamepadDigital.DPAD_UP,
                GamepadDigital.DPAD_DOWN,
            )),
            data
        )) {
            null -> false
            else -> {
                file.set(option, out)
                true
            }
        }
    }

    final override fun createRobotCfg() = blankCfg(hardwareMap)
    final override fun act() {

        var doDisplay = true

        // fulfill requests to store/load file
        if (driver1.back.justPressed()) {
            file.forget()
        } else if (driver1.start.justPressed()) {
            file.sync()
        }

        // fulfill requests to choose options
        else if (driver1.dpad_up.justPressed()) {
            index = (index + 1).clamp(0, options.size - 1)
        } else if (driver1.dpad_down.justPressed()) {
            index = (index - 1).clamp(0, options.size - 1)
        }

        // value mutation code
        else if (!requestMutate()) {
            doDisplay = false
        }

        if (doDisplay) {
            display()
        }

    }
}