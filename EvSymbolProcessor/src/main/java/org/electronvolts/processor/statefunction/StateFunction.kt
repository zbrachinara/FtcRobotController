package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

const val kclassPath_State = "org.electronvolts.evlib.statemachine.internal.State"

class StateFunction private constructor(
    private val loggerRef: KSPLogger,
    private val name: String,
    private val location: String,
    private val nameType: KSType,
    private val genericOver: List<KSName>,
    private val arguments: List<KSValueParameter>,
) {
    companion object {
        fun fromClassDeclaration(klass: KSClassDeclaration, logger: KSPLogger): StateFunction {
            val classNameSimple = klass.simpleName.asString()

            val stateClass = klass.getAllSuperTypes().find {
                it.declaration.qualifiedName!!.asString() == kclassPath_State
            }

            val nameType = when (stateClass) {
                null -> throw RuntimeException(
                    "The annotated class $classNameSimple does not " +
                        "extend `${kclassPath_State}`, which is required for this annotation"
                )
                // get the name of the type that State<?> is generic over
                else -> stateClass.innerArguments[0]
            }

            val constructor = when (klass.primaryConstructor) {
                null -> throw RuntimeException(
                    "The annotated class is not allowed to be" +
                        " an interface or otherwise not constructable"
                )
                else -> klass.primaryConstructor!!
            }
            val argumentsRaw = constructor.parameters;

            return StateFunction(
                loggerRef = logger,
                name = classNameSimple,
                nameType = nameType.type!!.resolve(),
                location = klass.qualifiedName!!.asString(),
                arguments = klass.primaryConstructor!!.parameters,
                genericOver = klass.typeParameters.map { it.name }
            )
        }
    }

    private fun generateParameters(): List<Pair<String, String>> = this.arguments.map { param ->
        loggerRef.warn("len of generic list is ${genericOver.size}")
        val simpleTypeName = param.type.resolve().declaration.simpleName
        val qualifiedTypeName = param.type.resolve().declaration.qualifiedName!!
        if (this.genericOver.any {
                loggerRef.warn("comparing ${it.asString()} and ${simpleTypeName.asString()}")
                it.asString() == simpleTypeName.asString() // TODO: Hacky, don't do it
            }) {
            Pair(param.name!!.asString(), simpleTypeName.asString())
        } else {
            Pair(param.name!!.asString(), qualifiedTypeName.asString())
        }
    }.toCollection(mutableListOf())
//    }

    fun toClosedState(): String {
        val argumentList =
            listOf(Pair("thisState", this.nameType))
                .plus(generateParameters())
                .joinToString(", ") {
                    "${it.first}: ${it.second}"
                }
        val argumentSignature = "($argumentList)"
        val signature =
            "fun <${this.nameType}: StateName> " +
                "StateMachineBuilder<${this.nameType}>.add${this.name}$argumentSignature"

        return "${signature}{}"
    }

    override fun toString(): String {
        return "fun add${this.name}() {}"
    }
}