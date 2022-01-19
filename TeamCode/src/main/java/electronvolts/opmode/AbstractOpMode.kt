package electronvolts.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import electronvolts.RobotCfg
import electronvolts.util.MatchTimer
import electronvolts.util.unit.Duration

abstract class AbstractOpMode<Config : RobotCfg> : OpMode() {

    protected abstract fun createRobotCfg(): Config

    protected abstract fun preSetup()
    protected abstract fun setup()
    protected abstract fun setupAct()
    protected abstract fun go()
    protected abstract fun preAct()
    protected abstract fun act()
    protected abstract fun postAct()
    protected abstract fun end()

    protected abstract val matchTime: Duration
    val timer = MatchTimer(matchTime)

    protected lateinit var robotConfig: RobotCfg

    override fun init() {
        robotConfig = createRobotCfg()
        preSetup()
        setup()
    }

    override fun init_loop() {
        setupAct()
    }

    override fun start() {
        timer.start()
        go()
    }

    override fun loop() {
        if (timer.justFinished()) stop()
        if (timer.finished()) return

        preAct()
        act()
        postAct()
    }

    override fun stop() {
        end()
    }


}