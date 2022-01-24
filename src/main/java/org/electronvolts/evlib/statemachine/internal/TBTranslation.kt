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

fun <T: StateName> TaskList<T>.initTasks() {
    this.forEach {
        it.init()
    }
}

fun <T: StateName> TaskList<T>.finishedTask(): Task<T>? {
    return this.find {
        it.isDone()
    }
}

fun <T: StateName> taskBehavior(tasks: TaskList<T>, behavior: Behavior) = taskBehavior<T>(tasks, listOf(behavior))

fun <T: StateName> taskBehavior(task: Task<T>, behaviors: BehaviorList) = taskBehavior<T>(listOf(task), behaviors)

fun <T: StateName> taskBehavior(tasks: TaskList<T>, behaviors: BehaviorList): State<T> {
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