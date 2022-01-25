package org.electronvolts.evlib.statemachine.internal

typealias TaillessState<T> = (next: T) -> State<T>

data class TaillessTask<T: StateName>(
    val init: () -> Unit,
    val isDone: () -> Boolean,
) {
    operator fun invoke(next: T) = object:Task<T> {
        override fun init() = this@TaillessTask.init()
        override fun isDone() = this@TaillessTask.isDone()
        override fun next() = next
    }
}

typealias TaillessTaskList<T> = List<TaillessTask<T>>

internal fun <T: StateName> TaillessTaskList<T>.initTaillessTasks() {
    this.forEach {
        it.init()
    }
}

internal fun <T: StateName> TaillessTaskList<T>.finishedTaillessTask(): TaillessTask<T>? {
    return this.find {
        it.isDone()
    }
}
