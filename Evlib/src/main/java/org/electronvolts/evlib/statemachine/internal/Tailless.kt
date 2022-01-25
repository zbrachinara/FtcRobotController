package org.electronvolts.evlib.statemachine.internal

typealias TaillessState<T> = (next: T) -> State<T>
typealias TaillessTask<T> = (next: T) -> Task<T>