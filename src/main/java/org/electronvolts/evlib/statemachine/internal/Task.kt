package electronvolts.statemachine.internal

interface Task {
    fun init()
    fun isDone(): Boolean
    fun next(): StateName
}