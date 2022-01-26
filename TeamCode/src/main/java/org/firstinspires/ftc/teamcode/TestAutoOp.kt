package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import org.electronvolts.evlib.opmode.AbstractAutoOp
import org.electronvolts.evlib.statemachine.StateMachineBuilder
import org.electronvolts.evlib.statemachine.addEnd
import org.electronvolts.evlib.statemachine.internal.OpenState
import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName

enum class TestStates : StateName {
    START,
    A,
    B,
    C,
    END,
}

val dummy: OpenState<TestStates> = { name ->
    object : State<TestStates> {
        override fun act() = name
    }
}

@Autonomous(name = "Test Auto Kotlin")
@Suppress("UNUSED")
class TestAutoOp : AbstractAutoOp<BlankConfig, TestStates>() {
    override fun buildStates(): StateMachine<TestStates> =
        StateMachineBuilder(TestStates.START, TestStates.values())
            .addSequence {
                it.add(TestStates.START, dummy)
                    .add(TestStates.A, dummy)
                    .add(TestStates.B, dummy)
                    .add(TestStates.C, dummy)
                    .finish(TestStates.END)
            }
            .addEnd(TestStates.END)
            .build()

    override fun createRobotCfg(): BlankConfig = BlankConfig(hardwareMap)

    override fun setup() = Unit

    override fun setupAct() = Unit

    override fun go() = Unit

    override fun act() = Unit

    override fun end() = Unit


}