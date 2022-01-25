package org.electronvolts.evlib.statemachine

import org.electronvolts.evlib.statemachine.internal.OpenState
import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateName
import java.lang.NullPointerException
import kotlin.collections.HashMap

class StateSequenceBuilder<T : StateName> internal constructor() {
    private val sequence = HashMap<T, State<T>>()

    // The `add` function accepts a pair of a name and an open state, but the sequence requires
    // complete states. Therefore, the pair can't be added into the sequence immediately, and
    // must be queued. The state is completed when we get the name of the next state, when `add`
    // is called a second time. A diagram:
    //
    // First call to `add`:
    //
    //   ┌──────────┐
    //   │  Name 1  │
    //   ├──────────┤
    //   │ State  1 ┼──────────> Unknown
    //   └──────────┘
    //
    // Second call to `add`:
    //
    //   ┌──────────┐           ┌──────────┐
    //   │  Name 1  │     ┌─────┼> Name 2  │
    //   ├──────────┤     │     ├──────────┤
    //   │ State  1 ┼─────┘     │ State  2 ┼──────────> Unknown
    //   └──────────┘           └──────────┘
    //
    // As you can see, in the first call to `add` state 1 begins as an open state -- it has to,
    // because we don't know which state comes next. But in the second call to `add`, we do.
    // Making this work requires remembering what the previous call to `add` was, which is
    // exactly what `queued` does. Once the second call to `add` finishes, we move it out of
    // queued, and move the new, incomplete state, in.
    private lateinit var queued: Pair<T, OpenState<T>>

    fun add(name: T, state: OpenState<T>): StateSequenceBuilder<T> {
        // completing the previous state, if it exists
        if (this::queued.isInitialized) {
            val (prevName, openState) = queued
            val completeState = openState(name)
            sequence[prevName] = completeState
        }
        queued = Pair(name, state)
        return this
    }

    fun finish(name: T): StateSequence<T> {
        return if (this::queued.isInitialized) {
            val (prevName, openState) = queued
            val completeState = openState(name)
            sequence[prevName] = completeState
            StateSequence(sequence)
        } else {
            throw NullPointerException(
                "You did not add any states to this sequence (you may want to use the empty state)"
            )
        }
    }

}

data class StateSequence<T : StateName> internal constructor(
    internal val sequence: Map<T, State<T>>
)