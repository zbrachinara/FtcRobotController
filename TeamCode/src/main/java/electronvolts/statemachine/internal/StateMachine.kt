package electronvolts.statemachine.internal

private typealias StateMap = Map<StateName, State>

class StateNotFoundError(s: StateName) : RuntimeException("Could not find state $(s.name}")

class StateMachine<T : StateName>(
    private val stateMap: StateMap,
    firstStateName: T,
    states: Array<T>,
) {

    var currName = firstStateName
        private set
    private var curr: State

    init {
        // load the first state
        when (val state = stateMap[firstStateName]) {
            null -> throw StateNotFoundError(firstStateName)
            else -> curr = state
        }

        // check for null state on all names pre-emptively
        for (name in states) {
            stateMap[name] ?: throw StateNotFoundError(name)
        }
    }

    fun act() {
        when (val next = curr.act()) {
            currName -> null
            null -> null
            else -> {
                currName = next as T;
                curr = stateMap[next]!!
            }
        }
    }

}