package org.electronvolts.evlib

import com.qualcomm.robotcore.hardware.HardwareMap

fun blankCfg(map: HardwareMap) = object : RobotCfg(map) {
    // empty
}

abstract class RobotCfg(
    val hardwareMap: HardwareMap,
) {
    // empty (for now)
}