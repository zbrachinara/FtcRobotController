package org.electronvolts.evlib.statemachine

import org.electronvolts.StateFunction
import org.electronvolts.evlib.statemachine.internal.OpenState
import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName

@StateFunction
fun <Sugma : StateName> blankState(next: Sugma?): OpenState<Sugma> = { name ->
    object : State<Sugma> {
        override fun act() = name
    }
}

@StateFunction
fun <T : StateName> endState() = blankState<T>(null)

class StateMachineBuilder<T : StateName>(
    private val first: T,
    private val allNames: Array<T>
) {

    private val stateMap = HashMap<T, State<T>>()

    fun add(name: T, state: State<T>): StateMachineBuilder<T> {
        stateMap[name] = state
        return this
    }

    fun addSequence(add: (StateSequenceBuilder<T>) -> StateSequence<T>): StateMachineBuilder<T> {
        stateMap += add(StateSequenceBuilder()).sequence
        return this
    }

    fun build() = StateMachine(stateMap, first, allNames)

}