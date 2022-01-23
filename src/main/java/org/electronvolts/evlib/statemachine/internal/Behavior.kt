package electronvolts.statemachine.internal

interface Behavior {

    fun init()
    fun act()
    fun drop()

}