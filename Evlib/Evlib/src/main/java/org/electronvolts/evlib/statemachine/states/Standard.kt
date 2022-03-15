package org.electronvolts.evlib.statemachine.states

import org.electronvolts.StateFunction
import org.electronvolts.evlib.statemachine.internal.OpenState
import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.StateName

@StateFunction
fun <T : StateName> blankState(): OpenState<T> = { name ->
    object : State<T> {
        override fun act() = name
    }
}

@StateFunction
fun <T : StateName> endState() = blankState<T>()