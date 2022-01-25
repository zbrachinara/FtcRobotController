package org.electronvolts.evlib.statemachine

import org.electronvolts.evlib.statemachine.internal.State

typealias TaillessState<T> = (next: T) -> State<T>