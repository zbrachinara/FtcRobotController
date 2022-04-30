package org.electronvolts.evlib.statemachine.internal

/**
 * An interface which provides the behavior of a state (not to be confused with [Behavior]).
 *
 * TODO: Add exactly how periodically the state machine passes control to states
 *
 * Periodically, the state machine will decide to pass control to a `State`. If this state is the
 * currently active one, the provided `act()` method will be called. This method is allowed to do
 * some quick work (keeping in mind that it runs in a tight loop), then return.
 *
 * States are commonly constructed from [Task]s and [Behavior]s. Tasks define the endpoints of a
 * state, while Behaviors define the actions that a state takes. When the [State.act] is first
 * called, both [Task.init] and [Behavior.init] are called. Afterwards, [Behavior.act] will be
 * called in a loop to do the action of the state, and [Task.isDone] will be called in the same
 * loop to determine whether to finish. If `isDone` has determined that it will finish, then the
 * method [Behavior.drop] will be called to clean up resources and [Task.next] will yield control to
 * the next state
 *
 */
interface State<T : StateName> {
    /**
     * @return `null` to keep this state active, or a name to pass control to the named state
     */
    fun act(): T?
}

/**
 * The name that a state machine matches a state to. Implement on enums, then register *one* state
 * for each name. If multiple states are registered for one name, then the most recent one
 * registered will be used.
 *
 * As of right now, the state machine also requires that all names be used on a state.
 *
 * @see StateMachine
 * @see org.electronvolts.evlib.statemachine.StateMachineBuilder
 */
interface StateName {
    val name: String
}