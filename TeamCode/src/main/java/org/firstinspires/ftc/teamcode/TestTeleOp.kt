package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.blankCfg
import org.electronvolts.evlib.opmode.AbstractTeleOp

@TeleOp(name = "test tele op kotlin")
class TestTeleOp : AbstractTeleOp<RobotCfg>() {
    override fun createRobotCfg() = blankCfg(hardwareMap)

    override fun setup() {
        print("in setup")
//        TODO("Not yet implemented")
    }

    override fun setupAct() {
//        TODO("Not yet implemented")
    }

    override fun go() {
//        TODO("Not yet implemented")
    }

    override fun act() {
        telemetry.addData("right joystick x", driver1.right_stick_x.value)
    }

    override fun end() {
//        TODO("Not yet implemented")
    }
}