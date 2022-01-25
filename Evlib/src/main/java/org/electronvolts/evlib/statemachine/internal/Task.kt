package org.electronvolts.evlib.statemachine.internal

interface Task<T: StateName> {
    fun init()
    fun isDone(): Boolean
    fun next(): T
}

typealias TaskList<T> = List<Task<T>>

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