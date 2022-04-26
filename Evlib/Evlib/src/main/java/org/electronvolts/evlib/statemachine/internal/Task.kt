package org.electronvolts.evlib.statemachine.internal

interface Task<T : StateName> {
    fun init()
    fun isDone(): Boolean
    fun next(): T
}

internal typealias TaskList<T> = List<Task<T>>

internal inline fun <T : StateName, reified Tsk : Task<T>> List<Tsk>.init() {
    this.forEach {
        it.init()
    }
}

internal inline fun <T : StateName, reified Tsk : Task<T>> List<Tsk>.finished(): Task<T>? {
    return this.find {
        it.isDone()
    }
}