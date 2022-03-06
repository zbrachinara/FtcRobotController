package org.electronvolts.evlib.statemachine.internal

interface Behavior {

    fun init()
    fun act()
    fun drop()

}

internal typealias BehaviorList = List<Behavior>

internal fun BehaviorList.initBehaviors() {
    this.forEach {
        it.init()
    }
}

internal fun BehaviorList.actBehaviors() {
    this.forEach {
        it.act()
    }
}

internal fun BehaviorList.dropBehaviors() {
    this.forEach {
        it.drop()
    }
}