package org.electronvolts.evlib.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.util.MatchTimer
import org.electronvolts.evlib.util.unit.Duration

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
    val timer: MatchTimer by lazy {
        MatchTimer(matchTime)
    }

    protected lateinit var robotConfig: RobotCfg

    final override fun init() {
        robotConfig = createRobotCfg()
        preSetup()
        setup()
    }

    final override fun init_loop() {
        setupAct()
    }

    final override fun start() {
        timer.start()
        go()
    }

    final override fun loop() {
        if (timer.justFinished()) stop()
        if (timer.finished()) return

        preAct()
        act()
        postAct()
    }

    final override fun stop() {
        end()
    }


}