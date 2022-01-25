package org.electronvolts.evlib.statemachine

import org.electronvolts.evlib.statemachine.internal.State
import org.electronvolts.evlib.statemachine.internal.Task

typealias TaillessState<T> = (next: T) -> State<T>
typealias TaillessTask<T> = (next: T) -> Task<T>