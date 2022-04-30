package org.electronvolts.evlib.statemachine.internal

/**
 * A state which does not contain information about its next state. It can be transformed into a
 * "closed" state by completing it with the name of the next state. This completion should be able
 * to be done multiple times for the same `OpenState` instance. If you've heard of currying, this
 * is basically a hardcoded way to curry a concrete `State`'s constructor.
 *
 * Note that, due to the definition of an OpenState, a State can be defined completely in terms of
 * an OpenState. Say that a function returns an `OpenState` like so:
 * ```
 * fun openExampleState<T: StateName>(payload: Int): OpenState<T> {
 *     return { name ->
 *         var x = payload
 *         while x < 0 {
 *             x++
 *         }
 *         name
 *     }
 * }
 * ```
 * This state increments `payload` until it is positive or zero, then gives control to the next
 * state, which isn't known (called `name` for now).
 *
 * A function can be created which then describes the closing of the state, like so:
 * ```
 * fun exampleState<T: StateName>(next: StateName, payload: Int): State<T> =
 *     openExampleState(payload)(next)
 * ```
 *
 * @see State
 * @see StateName
 */
typealias OpenState<T> = (next: T) -> State<T>

data class OpenTask<T : StateName>(
    val init: () -> Unit,
    val isDone: () -> Boolean,
) {
    operator fun invoke(next: T) = object : Task<T> {
        override fun init() = this@OpenTask.init()
        override fun isDone() = this@OpenTask.isDone()
        override fun next() = next
    }
}

typealias OpenTaskList<T> = List<OpenTask<T>>

internal inline fun <T : StateName, reified OTsk : OpenTask<T>> List<OTsk>.init() {
    this.forEach {
        it.init()
    }
}

internal inline fun <T : StateName, reified OTsk : OpenTask<T>> List<OTsk>.finished(): OpenTask<T>? {
    return this.find {
        it.isDone()
    }
}
