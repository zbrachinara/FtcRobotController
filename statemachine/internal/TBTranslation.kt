package electronvolts.statemachine.internal

private typealias TaskList = List<Task>
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

fun TaskList.initTasks() {
    this.forEach {
        it.init()
    }
}

fun TaskList.finishedTask(): Task? {
    return this.find {
        it.isDone()
    }
}

fun taskBehavior(tasks: TaskList, behavior: Behavior) = taskBehavior(tasks, listOf(behavior))

fun taskBehavior(task: Task, behaviors: BehaviorList) = taskBehavior(listOf(task), behaviors)

fun taskBehavior(tasks: TaskList, behaviors: BehaviorList): State {
    return object : State {
        var init = false
        override fun act(): StateName? {
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