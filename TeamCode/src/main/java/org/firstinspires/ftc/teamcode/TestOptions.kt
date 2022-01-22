package org.firstinspires.ftc.teamcode

import electronvolts.options.OptionClass
import electronvolts.options.TypeData

val TestOptions = object {

    val SUSSINESS = OptionClass.define("SUSSINESS", TypeData.Builder.int(0, 100, 5))

}