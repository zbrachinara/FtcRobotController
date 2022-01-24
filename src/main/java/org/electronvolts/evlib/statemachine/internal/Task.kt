package org.electronvolts.evlib.statemachine.internal

import org.electronvolts.evlib.statemachine.internal.StateName

interface Task<T: StateName> {
    fun init()
    fun isDone(): Boolean
    fun next(): T
}