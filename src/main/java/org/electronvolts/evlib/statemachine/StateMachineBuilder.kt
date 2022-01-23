package electronvolts.statemachine

import electronvolts.statemachine.internal.State
import electronvolts.statemachine.internal.StateMachine
import electronvolts.statemachine.internal.StateName

fun <T : StateName> StateMachineBuilder<T>.addBlank(curr: T, next: T?) =
    this.add(curr, object : State {
        override fun act(): StateName? = next
    })

fun <T : StateName> StateMachineBuilder<T>.addEnd(st: T) = this.addBlank(st, null)

class StateMachineBuilder<T : StateName>(
    private val first: T,
    private val allNames: Array<T>
) {

    private val stateMap = HashMap<StateName, State>()

    fun add(name: T, state: State): StateMachineBuilder<T> {
        stateMap[name] = state
        return this
    }

    fun build() = StateMachine(stateMap as Map<T, State>, first, allNames)

}