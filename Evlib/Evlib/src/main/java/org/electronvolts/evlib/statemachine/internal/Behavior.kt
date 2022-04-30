package org.electronvolts.evlib.statemachine.internal

/**
 * Behaviors describe the actions of the state
 *
 * @see State
 */
interface Behavior {

    fun init()
    fun act()
    fun drop()

}

internal typealias BehaviorList = List<Behavior>

internal inline fun <reified Bhvr : Behavior> List<Bhvr>.init() {
    this.forEach {
        it.init()
    }
}

internal inline fun <reified Bhvr : Behavior> List<Bhvr>.act() {
    this.forEach {
        it.act()
    }
}

internal inline fun <reified Bhvr : Behavior> List<Bhvr>.drop() {
    this.forEach {
        it.drop()
    }
}