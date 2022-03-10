package org.electronvolts.evlib.statemachine

import org.electronvolts.StateFunction
import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName

@StateFunction
fun <T : StateName> blankState(next: T?) = object : State<T> {
    override fun act() = next
}

@StateFunction
fun <T : StateName> endState() = blankState<T>(null)

@StateFunction
class GenericState<T : StateName, U : List<V>, V>(@Suppress("UNUSED_PARAMETER") param: U) :
    State<T> {
    override fun act() = null
}

@StateFunction
fun <T : StateName, V> weirdState() = GenericState<T, List<V>, V>(listOf())

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