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

// tailless version
data class TaillessTaskBehavior<T : StateName>(
    val taskList: TaillessTaskList<T>,
    val behaviorList: BehaviorList,
) {
    constructor(taillessTask: TaillessTask<T>, behaviorList: BehaviorList) : this(
        listOf(taillessTask),
        behaviorList,
    )

    constructor(taillessTaskList: TaillessTaskList<T>, behavior: Behavior) : this(
        taillessTaskList,
        listOf(behavior),
    )

    constructor(taillessTask: TaillessTask<T>, behavior: Behavior) : this(
        listOf(taillessTask), listOf(behavior)
    )
}

fun <T: StateName> taillessTaskBehavior(descriptor: TaillessTaskBehavior<T>): TaillessState<T> {
    val (tasks, behaviors) = descriptor

    return {
        object : State<T> {
            var init = false
            override fun act(): T? {
                if (!init) {
                    tasks.initTaillessTasks()
                    behaviors.initBehaviors()
                    init = true
                }
                return when (val finished = tasks.finishedTaillessTask()) {
                    null -> {
                        behaviors.actBehaviors()
                        null
                    }
                    else -> {
                        behaviors.dropBehaviors()
                        it
                    }
                }
            }
        }
    }

}