package org.electronvolts.evlib.statemachine.internal

interface Task {
    fun init()
    fun isDone(): Boolean
    fun next(): StateName
}