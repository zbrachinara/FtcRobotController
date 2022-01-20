package electronvolts.gamepad;

import electronvolts.util.TimerOnce;
import electronvolts.util.unit.Duration;

public class ButtonAlivenessTester {

    private final DigitalInput button;
    private final DigitalInput start;
    private TimerOnce timer = new TimerOnce(Duration.Companion.fromMilliseconds(500));

    public ButtonAlivenessTester(DigitalInput button, DigitalInput start) {
        this.button = button;
        this.start = start;
    }

    public boolean isAlive() {
        if(button.invoke() && start.invoke()) {
            timer.init();
            return false;
        } else {
            return timer.getFinished();
        }
    }
}