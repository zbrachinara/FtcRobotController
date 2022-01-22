package org.electronvolts.evlib.statemachine.internal

interface State {
    fun act(): StateName?
}

interface StateName {
    val name: String
}