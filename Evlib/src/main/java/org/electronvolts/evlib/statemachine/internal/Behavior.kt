package org.electronvolts.evlib.statemachine.internal

interface Behavior {

    fun init()
    fun act()
    fun drop()

}