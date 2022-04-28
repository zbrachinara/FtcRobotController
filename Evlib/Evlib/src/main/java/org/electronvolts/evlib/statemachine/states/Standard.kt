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

@StateFunction
fun <T : StateName, U, V : List<U>> genericState(l: V) = blankState<T>()

//@StateFunction
//class ShouldFail<T: StateName> : OpenState<T> {
//
//    @StateFunction
//    constructor(shouldFail: String)
//
//    override operator fun invoke(next: T): State<T> = blankState<T>()(next)
//}