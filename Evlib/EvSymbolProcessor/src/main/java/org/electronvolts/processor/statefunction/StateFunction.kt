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

//TODO: Integrate with Visitor pattern
class StateFunction private constructor(
    private val loggerRef: KSPLogger,
    private val name: String,
    private val location: String,
    private val nameType: KSType,
    private val genericOver: List<KSName>,
    private val arguments: List<KSValueParameter>,
) {
    companion object {
        //TODO: Error if both the class and its constructors are annotated
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

        fun fromConstructor(function: KSFunctionDeclaration, logger: KSPLogger): StateFunction {
            // assert that this is, in fact, a function (independent from object data)
            if (
                !function.isConstructor() &&
                !(
                    function.functionKind == FunctionKind.TOP_LEVEL ||
                        function.functionKind == FunctionKind.STATIC)
            ) {
                throw RuntimeException(
                    "${function.simpleName.asString()} is not a valid function for this" +
                        "purpose"
                )
            }
            val nameType = getStateNameType(function).type!!.resolve()

            val returnTypeDecl = function.returnType!!.resolve().declaration
            val returnTypeSimple = returnTypeDecl.simpleName.asString()

            val location = if (function.isConstructor()) {
                returnTypeDecl.qualifiedName!!.asString()
            } else {
                function.qualifiedName!!.asString()
            }

            val typeParameters = if (function.isConstructor()) {
                val parameterTypes = function.parameters.map { param ->
                    param.type.resolve().declaration.simpleName.asString()
                }
                returnTypeDecl.typeParameters.filter {
                    parameterTypes.contains(it.name.asString())
                }
            } else {
                function.typeParameters
            }

            return StateFunction(
                loggerRef = logger,
                name = returnTypeSimple,
                nameType = nameType,
                location = location,
                arguments = function.parameters,
                genericOver = typeParameters.map { it.name },
            )
        }
    }

    private fun genParameters() = this.arguments.map { param ->
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

    private fun genOutsideParameters() =
        listOf(Pair("thisState", this.nameType.toString())).plus(genParameters())

    private fun genBody() =
        """
        |{
        |   this.add(
        |       thisState,
        |       $location(
        |           ${genParameters().joinToString(",\n") { "${it.first} = ${it.first}" }}
        |       )
        |   )
        |    
        |   return this
        |}
        """.trimMargin()

    fun toClosedStateFunction(): String {
        val argumentList =
            genOutsideParameters()
                .joinToString(",\n") {
                    "    ${it.first}: ${it.second}"
                }
        val argumentSignature = "(\n$argumentList\n)"
        val signature =
            "fun <${this.nameType}: StateName> " +
                "StateMachineBuilder<${this.nameType}>.add${this.name}$argumentSignature:" +
                "StateMachineBuilder<${this.nameType}>"

        return "$signature${genBody()}"
    }

    fun toOpenStateFunction(): String {


        return """
            |   
        """.trimMargin()
    }

    override fun toString(): String {
        return "fun add${this.name}() {}"
    }
}