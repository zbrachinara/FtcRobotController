package electronvolts.opmode

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import electronvolts.RobotCfg

abstract class AbstractOpMode<Config: RobotCfg>: OpMode() {

    protected abstract fun createRobotCfg(): Config

    protected abstract fun setup()
    protected abstract fun setupAct()
    protected abstract fun go()
    protected abstract fun preAct()
    protected abstract fun act()
    protected abstract fun postAct()
    protected abstract fun end()

    protected abstract val matchTimeMillis: Long

    protected lateinit var robotConfig: RobotCfg;

    override fun init() {
        robotConfig = createRobotCfg()
        setup()
    }

    override fun init_loop() {
        setupAct()
    }

    override fun start() {

    }


}