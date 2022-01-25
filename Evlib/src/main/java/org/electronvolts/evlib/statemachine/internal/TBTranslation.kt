package org.electronvolts.evlib.statemachine.internal

data class TaskBehavior<T : StateName>(
    val taskList: TaskList<T>,
    val behaviorList: BehaviorList,
) {
    constructor(task: Task<T>, behaviorList: BehaviorList) : this(listOf(task), behaviorList)
    constructor(taskList: TaskList<T>, behavior: Behavior) : this(taskList, listOf(behavior))
    constructor(task: Task<T>, behavior: Behavior) : this(listOf(task), listOf(behavior))
}

fun <T : StateName> taskBehavior(descriptor: TaskBehavior<T>): State<T> {
    val (tasks, behaviors) = descriptor
    return object : State<T> {
        var init = false
        override fun act(): T? {
            if (!init) {
                tasks.initTasks()
                behaviors.initBehaviors()
                init = true
            }
            return when (val finished = tasks.finishedTask()) {
                null -> {
                    behaviors.actBehaviors()
                    null
                }
                else -> {
                    behaviors.dropBehaviors()
                    finished.next()
                }
            }
        }
    }
}