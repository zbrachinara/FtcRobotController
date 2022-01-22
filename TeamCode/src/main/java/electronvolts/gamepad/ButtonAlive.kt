package electronvolts.gamepad

import electronvolts.util.TimerOnce
import electronvolts.util.unit.Duration

class ButtonAlive(private val button: DigitalInput, private val start: DigitalInput) {
    private val timer = TimerOnce(Duration.fromMilliseconds(500.0))

    fun isAlive() = if (button.invoke() && start.invoke()) {
        timer.init()
        false
    } else {
        timer.finished
    }
}