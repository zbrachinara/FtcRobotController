package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*

const val kclassPath_State = "org.electronvolts.evlib.statemachine.internal.State"

private fun stateType(decl: KSDeclaration): KSType? {
    return when (decl) {
        is KSClassDeclaration -> {
            decl.getAllSuperTypes().find {
                it.declaration.qualifiedName!!.asString() == kclassPath_State
            }
        }
        is KSTypeAlias -> {
            stateType(decl.type.resolve().declaration)
        }
        is KSTypeParameter -> {
            decl.bounds.find { type ->
                stateType(type.resolve().declaration) != null
            }?.resolve()
        }
        is KSFunctionDeclaration -> {
            when (val ret = decl.returnType) {
                null -> throw RuntimeException("Nothing is returned from the given function")
                else -> stateType(ret.resolve().declaration)
            }
        }
        else -> {
            throw RuntimeException(
                "Unexpected error: Faulty declaration " +
                    "${decl.simpleName.asString()} | ${decl.qualifiedName?.asString()} " +
                    "passed to symbol processor"
            )
        }
    }
}

private fun getStateNameType(decl: KSDeclaration): KSTypeArgument {
    val simpleName = decl.simpleName.asString()
    return when (val stateClass = stateType(decl)) {
        null -> throw RuntimeException(
            "The annotated declaration $simpleName does not yield a " +
                "`${kclassPath_State}`, which is required for this annotation"
        )
        else -> {
            assert(stateClass.innerArguments.size == 1)
            stateClass.innerArguments[0]
        }
    }
}

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
            val nameType = getStateNameType(klass)

            val constructor = when (klass.primaryConstructor) {
                null -> throw RuntimeException(
                    "The annotated class is not allowed to be" +
                        " an interface or otherwise not constructable"
                )
                else -> klass.primaryConstructor!!
            }

            return StateFunction(
                loggerRef = logger,
                name = classNameSimple,
                nameType = nameType.type!!.resolve(),
                location = klass.qualifiedName!!.asString(),
                arguments = constructor.parameters,
                genericOver = klass.typeParameters.map { it.name }
            )
        }

        fun fromConstructor(method: KSFunctionDeclaration, logger: KSPLogger): StateFunction {
            //TODO: Forbid functions that are both non-static and non-constructor
            val nameType = getStateNameType(method).type!!.resolve()

            val returnTypeDecl = method.returnType!!.resolve().declaration
            val returnTypeSimple = returnTypeDecl.simpleName.asString()

            val location = if (method.isConstructor()) {
                returnTypeDecl.qualifiedName!!.asString()
            } else {
                method.qualifiedName!!.asString()
            }

            logger.warn("Received type parameters: ${method.typeParameters.map { it.name }}")

            return StateFunction(
                loggerRef = logger,
                name = returnTypeSimple,
                nameType = nameType,
                location = location,
                arguments = method.parameters,
                genericOver = method.typeParameters.map { it.name }
            )
        }
    }

    private fun generateParameters(): List<Pair<String, String>> = this.arguments.map { param ->
        loggerRef.warn("len of generic list is ${genericOver.size}")
        val simpleTypeName = param.type.resolve().declaration.simpleName
        val qualifiedTypeName = param.type.resolve().declaration.qualifiedName!!

        if (this.genericOver.any {
                it.asString() == simpleTypeName.asString() // TODO: Hacky, don't do it
            }) {
            Pair(param.name!!.asString(), simpleTypeName.asString())
        } else {
            Pair(param.name!!.asString(), qualifiedTypeName.asString())
        }
    }.toCollection(mutableListOf())

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
                "StateMachineBuilder<${this.nameType}>.add${this.name}$argumentSignature:" +
                "StateMachineBuilder<${this.nameType}>"

        val stateParameters = generateParameters().joinToString(",\n") {
            "${it.first} = ${it.first}"
        }

        return """$signature {
            |   this.add(
            |       thisState,
            |       $location(
            |           $stateParameters
            |       ),
            |   )
            |   
            |   return this
            |}""".trimMargin()
    }

    override fun toString(): String {
        return "fun add${this.name}() {}"
    }
}