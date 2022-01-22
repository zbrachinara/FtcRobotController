package org.electronvolts.evlib.opmode

import org.electronvolts.evlib.RobotCfg
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName
import org.electronvolts.evlib.util.unit.Duration

abstract class AbstractAutoOp<Config: RobotCfg, State: StateName>: AbstractOpMode<Config>() {

    private lateinit var stateMachine: StateMachine<State>

    abstract fun buildStates(): StateMachine<State>
    override val matchTime = Duration.fromSeconds(30.0)

    final override fun preSetup() {
        stateMachine = buildStates()
    }

    final override fun preAct() {

    }

    final override fun postAct() {
        stateMachine.act()
    }

}