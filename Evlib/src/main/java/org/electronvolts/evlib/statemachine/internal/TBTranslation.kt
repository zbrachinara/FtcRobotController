package org.electronvolts.evlib.statemachine.internal

private typealias TaskList<T> = List<Task<T>>
private typealias BehaviorList = List<Behavior>

fun BehaviorList.initBehaviors() {
    this.forEach {
        it.init()
    }
}

fun BehaviorList.actBehaviors() {
    this.forEach {
        it.act()
    }
}

fun BehaviorList.dropBehaviors() {
    this.forEach {
        it.drop()
    }
}

fun <T : StateName> TaskList<T>.initTasks() {
    this.forEach {
        it.init()
    }
}

fun <T : StateName> TaskList<T>.finishedTask(): Task<T>? {
    return this.find {
        it.isDone()
    }
}

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