package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.symbol.KSClassDeclaration

class StateFunction(
    private val name: String
) {
    companion object {
        fun fromClass(klass: KSClassDeclaration): StateFunction {
            return StateFunction(klass.simpleName.asString())
        }
    }

    override fun toString(): String {
        return "fun add${this.name}() {}"
    }
}