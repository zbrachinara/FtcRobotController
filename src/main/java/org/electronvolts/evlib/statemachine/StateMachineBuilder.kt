package org.electronvolts.evlib.statemachine

import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateMachine
import org.electronvolts.evlib.statemachine.internal.StateName

fun <T : StateName> StateMachineBuilder<T>.addBlank(curr: T, next: T?) =
    this.add(curr, object : State<T> {
        override fun act(): T? = next
    })

fun <T : StateName> StateMachineBuilder<T>.addEnd(st: T) = this.addBlank(st, null)

class StateMachineBuilder<T : StateName>(
    private val first: T,
    private val allNames: Array<T>
) {

    private val stateMap = HashMap<T, State<T>>()

    fun add(name: T, state: State<T>): StateMachineBuilder<T> {
        stateMap[name] = state
        return this
    }

    fun addSequence(
        name: T,
        add: (StateSequenceBuilder<T>) -> StateSequenceBuilder<T>,
    ): StateMachineBuilder<T> {
        var builder = StateSequenceBuilder(name)
        builder = add(builder)

        stateMap += builder.sequence
        return this
    }

    fun build() = StateMachine(stateMap, first, allNames)

}