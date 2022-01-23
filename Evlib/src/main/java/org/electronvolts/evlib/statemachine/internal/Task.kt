package org.electronvolts.evlib.statemachine.internal

import org.electronvolts.evlib.statemachine.internal.StateName

interface Task {
    fun init()
    fun isDone(): Boolean
    fun next(): StateName
}