package org.electronvolts.evlib.statemachine.internal

private typealias StateMap<T> = Map<T, State<T>>

class StateNotFoundError(s: StateName) : RuntimeException("Could not find state ${s.name}")

/**
 * Electronvolts state machine
 *
 * This machine encapsulates and runs through a set of states. Control flow is not calculated
 * directly by the state machine, but the states reveal them throughout runtime (therefore this kind
 * of information will always be unavailable).
 *
 * @see State
 */
class StateMachine<T : StateName>(
    private val stateMap: StateMap<T>,
    firstStateName: T,
    names: Array<out T>,
) {

    /**
     * The name for the state which will be called next. This variable may be used to inspect the
     * state machine at runtime.
     */
    var currentName: StateName = firstStateName
        private set
    private var curr: State<T>

    init {
        // load the first state
        when (val state = stateMap[firstStateName]) {
            null -> throw StateNotFoundError(firstStateName)
            else -> curr = state
        }

        // check for null state on all names pre-emptively
        for (name in names) {
            stateMap[name] ?: throw StateNotFoundError(name)
        }
    }

    fun act() {
        when (val next = curr.act()) {
            currentName -> return
            null -> return
            else -> {
                currentName = next
                curr = stateMap[next]!!
            }
        }
    }

}