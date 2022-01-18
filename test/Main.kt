package electronvolts.test

import electronvolts.statemachine.StateMachineBuilder
import electronvolts.statemachine.internal.State
import electronvolts.statemachine.internal.StateMachine
import electronvolts.statemachine.internal.StateName
import org.junit.Assert.assertEquals
import java.util.*

enum class States : StateName {
    START,
    SLAKE,
    END,
}

fun main() {

    val stateMap = HashMap<StateName, State>()
    stateMap[States.START] = object : State {
        override fun act(): StateName {
            return States.SLAKE
        }
    }
    stateMap[States.SLAKE] = object : State {
        override fun act(): StateName {
            return States.END
        }
    }
    stateMap[States.END] = object: State {
        override fun act(): StateName? {
            return null
        }
    }

    val machineBuilder = StateMachineBuilder(States.START, States.values())
    val machine = StateMachine(stateMap, States.START, States.values())

    assertEquals(machine.currName, States.START)
    machine.act()
    assertEquals(machine.currName, States.SLAKE)
    machine.act()
    assertEquals(machine.currName, States.END)
    machine.act()
    assertEquals(machine.currName, States.END)

}