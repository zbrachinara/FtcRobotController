package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.blankCfg
import org.electronvolts.evlib.opmode.AbstractAutoOp
import org.electronvolts.evlib.statemachine.StateMachineBuilder
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName
import org.electronvolts.evlib.statemachine.statefunction.addBlankState
import org.electronvolts.evlib.statemachine.statefunction.addEndState

enum class TestStates : StateName {
    START,
    A,
    B,
    C,
    END,
}

@Autonomous(name = "Test Auto Kotlin")
@Suppress("UNUSED")
class TestAutoOp : AbstractAutoOp<RobotCfg, TestStates>() {
    override fun buildStates(): StateMachine<TestStates> =
        StateMachineBuilder(TestStates.START, TestStates.values())
            .addSequence {
                it.addBlankState(TestStates.START)
                    .addBlankState(TestStates.A)
                    .addBlankState(TestStates.B)
                    .addBlankState(TestStates.C)
                    .finish(TestStates.END)
            }
            .addEndState(TestStates.END, TestStates.END)
            .build()

    override fun createRobotCfg() = blankCfg(hardwareMap)

    override fun setup() = Unit

    override fun setupAct() = Unit

    override fun go() = Unit

    override fun act() = Unit

    override fun end() = Unit


}