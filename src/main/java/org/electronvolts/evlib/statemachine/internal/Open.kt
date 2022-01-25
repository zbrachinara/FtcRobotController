package org.electronvolts.evlib.statemachine.internal

typealias OpenState<T> = (next: T) -> State<T>

data class OpenTask<T: StateName>(
    val init: () -> Unit,
    val isDone: () -> Boolean,
) {
    operator fun invoke(next: T) = object:Task<T> {
        override fun init() = this@OpenTask.init()
        override fun isDone() = this@OpenTask.isDone()
        override fun next() = next
    }
}

typealias OpenTaskList<T> = List<OpenTask<T>>

internal fun <T: StateName> OpenTaskList<T>.initOpenTask() {
    this.forEach {
        it.init()
    }
}

internal fun <T: StateName> OpenTaskList<T>.finishedOpenTask(): OpenTask<T>? {
    return this.find {
        it.isDone()
    }
}
