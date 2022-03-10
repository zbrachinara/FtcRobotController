package org.electronvolts

/**
 * Used to mark class, function, or constructor declarations for the extending of state machine
 * classes.
 *
 * All classes should inherit from `State<T>`, and all functions (or constructors) should return an
 * object which inherits from `State<T>`.
 *
 * All private declarations are forbidden from being marked.
 *
 * If a class is marked for conversion to `StateFunction`, then all public constructors will be
 * converted. Either only constructors should be marked or the class should be marked. If both the
 * class and any of its constructors are marked, an error will be presented to the user.
 *
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR)
@Retention(AnnotationRetention.SOURCE)
annotation class StateFunction