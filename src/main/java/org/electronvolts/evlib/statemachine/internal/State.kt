package org.electronvolts.evlib.statemachine.internal

interface State<T : StateName> {
    fun act(): T?
}

interface StateName {
    val name: String
}