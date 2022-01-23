package electronvolts.opmode

import electronvolts.RobotCfg
import electronvolts.statemachine.StateMachineBuilder
import electronvolts.statemachine.internal.StateMachine
import electronvolts.statemachine.internal.StateName
import electronvolts.util.unit.Duration

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