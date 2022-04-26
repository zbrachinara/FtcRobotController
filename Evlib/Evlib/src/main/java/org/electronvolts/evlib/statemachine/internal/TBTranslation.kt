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
                tasks.init()
                behaviors.init()
                init = true
            }
            return when (val finished = tasks.finished()) {
                null -> {
                    behaviors.act()
                    null
                }
                else -> {
                    behaviors.drop()
                    finished.next()
                }
            }
        }
    }
}

// open version
data class OpenTaskBehavior<T : StateName>(
    val taskList: OpenTaskList<T>,
    val behaviorList: BehaviorList,
) {
    constructor(openTask: OpenTask<T>, behaviorList: BehaviorList) : this(
        listOf(openTask),
        behaviorList,
    )

    constructor(openTaskList: OpenTaskList<T>, behavior: Behavior) : this(
        openTaskList,
        listOf(behavior),
    )

    constructor(openTask: OpenTask<T>, behavior: Behavior) : this(
        listOf(openTask), listOf(behavior)
    )
}

fun <T : StateName> openTaskBehavior(descriptor: OpenTaskBehavior<T>): OpenState<T> {
    val (tasks, behaviors) = descriptor

    return { nextState ->
        object : State<T> {
            var init = false
            override fun act(): T? {
                if (!init) {
                    tasks.init()
                    behaviors.init()
                    init = true
                }
                return when (tasks.finished()) {
                    null -> {
                        behaviors.act()
                        null
                    }
                    else -> {
                        behaviors.drop()
                        nextState
                    }
                }
            }
        }
    }

}