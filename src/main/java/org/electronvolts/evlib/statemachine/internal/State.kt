package org.electronvolts.evlib.statemachine.internal

fun <T : StateName> asOpenState(state: State<T>): OpenState<T> = { name ->
    object : State<T> {
        override fun act(): T? =
            when (this.act()) {
                null -> null
                else -> name
            }
    }
}

interface State<T : StateName> {
    fun act(): T?
}

interface StateName {
    val name: String
}