package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.electronvolts.evlib.opmode.AbstractAutoOp
import org.electronvolts.evlib.statemachine.StateMachineBuilder
import org.electronvolts.evlib.statemachine.addEnd
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName

enum class TestStates : StateName {
    START,
}

@Autonomous(name = "Test Auto Kotlin")
class TestAutoOp : AbstractAutoOp<BlankConfig, TestStates>() {
    override fun buildStates(): StateMachine<TestStates> =
        StateMachineBuilder<TestStates>(TestStates.START, TestStates.values())
            .addEnd(TestStates.START)
            .build()

    override fun createRobotCfg(): BlankConfig = BlankConfig(hardwareMap)

    override fun setup() = Unit

    override fun setupAct() = Unit

    override fun go() = Unit

    override fun act() = Unit

    override fun end() = Unit


}