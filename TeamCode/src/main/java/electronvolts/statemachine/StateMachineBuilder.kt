package electronvolts.statemachine

import electronvolts.statemachine.internal.State
import electronvolts.statemachine.internal.StateMachine
import electronvolts.statemachine.internal.StateName

class StateMachineBuilder<T : StateName>(
    private val first: T,
    private val allNames: Array<T>
) {

    private val stateMap = HashMap<StateName, State>()

    fun add(name: StateName, state: State): StateMachineBuilder<T> {
        stateMap[name] = state
        return this
    }

    fun build() = StateMachine(stateMap, first, allNames)

}