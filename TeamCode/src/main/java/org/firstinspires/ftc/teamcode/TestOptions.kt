package org.firstinspires.ftc.teamcode

import org.electronvolts.evlib.options.OptionClass
import org.electronvolts.evlib.options.TypeData

val TestOptions = object {

    val SUSSINESS = OptionClass.define("SUSSINESS", TypeData.Builder.int(0, 100, 5))

}