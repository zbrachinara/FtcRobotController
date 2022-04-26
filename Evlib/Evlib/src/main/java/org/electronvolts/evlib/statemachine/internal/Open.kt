package org.electronvolts.evlib.statemachine.internal

typealias OpenState<T> = (next: T) -> State<T>

data class OpenTask<T : StateName>(
    val init: () -> Unit,
    val isDone: () -> Boolean,
) {
    operator fun invoke(next: T) = object : Task<T> {
        override fun init() = this@OpenTask.init()
        override fun isDone() = this@OpenTask.isDone()
        override fun next() = next
    }
}

typealias OpenTaskList<T> = List<OpenTask<T>>

internal inline fun <T : StateName, reified OTsk : OpenTask<T>> List<OTsk>.init() {
    this.forEach {
        it.init()
    }
}

internal inline fun <T : StateName, reified OTsk : OpenTask<T>> List<OTsk>.finished(): OpenTask<T>? {
    return this.find {
        it.isDone()
    }
}
