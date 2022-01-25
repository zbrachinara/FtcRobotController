package org.electronvolts.evlib.statemachine

import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateName

class StateSequence<T : StateName> internal constructor(first: T) {

    internal val sequence = HashMap<T, State<T>>()
    private var tail = first

    fun next(next: T, state: State<T>): StateSequence<T> {
        sequence[tail] = object : State<T> {
            override fun act(): T? {
                return when (state.act()) {
                    null -> null
                    else -> next
                }
            }
        }
        tail = next
        return this
    }

}