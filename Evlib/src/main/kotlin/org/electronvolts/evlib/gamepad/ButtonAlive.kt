package org.electronvolts.evlib.gamepad

import org.electronvolts.evlib.util.TimerOnce
import org.electronvolts.evlib.util.unit.Duration

class ButtonAlive(private val button: DigitalInput, private val start: DigitalInput) {
    private val timer = TimerOnce(Duration.fromMilliseconds(500.0))

    fun isAlive() = if (button() && start()) {
        timer.init()
        false
    } else {
        timer.finished
    }
}