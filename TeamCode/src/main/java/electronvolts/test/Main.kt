package electronvolts.test

import electronvolts.statemachine.internal.State
import electronvolts.statemachine.internal.StateMachine
import electronvolts.statemachine.internal.StateName
import junit.framework.Assert.assertEquals
import java.util.*

enum class States : StateName {
    START,
    SLAKE,
    END,
}

fun main() {

    val stateMap = HashMap<StateName, State>()
    stateMap.put(States.START, object : State {
        override fun act(): StateName {
            return States.SLAKE
        }
    })
    stateMap.put(States.SLAKE, object : State {
        override fun act(): StateName {
            return States.END
        }
    })
    stateMap.put(States.END, object: State {
        override fun act(): StateName? {
            return null
        }
    })

    val machine = StateMachine(stateMap, States.START, States.values())

    assertEquals(machine.currName, States.START)
    machine.act();
    assertEquals(machine.currName, States.SLAKE)
    machine.act();
    assertEquals(machine.currName, States.END)
    machine.act();
    assertEquals(machine.currName, States.END)

}