package org.electronvolts.evlib.opmode

import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.blankCfg
import org.electronvolts.evlib.options.OptionClass
import org.electronvolts.evlib.options.OptionFile
import org.electronvolts.evlib.util.Path

abstract class AbstractOptionsOp : AbstractTeleOp<RobotCfg>() {
    protected abstract val options: List<OptionClass<*>>
    protected abstract val filePath: Path
    private val file = OptionFile(filePath)

    private var index = 0

    private fun display() {
        // TODO: Complete function
    }

    override fun createRobotCfg() = blankCfg(hardwareMap)
    override fun act() {

        // fulfill requests to store/load file
        if (driver1.back.justPressed()) {
            file.forget()
            display()
        }
        if (driver1.start.justPressed()) {
            file.sync()
            display()
        }

        // fulfill requests to select options


    }
}