package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.*
import org.electronvolts.processor.reconstructTypeParameters

const val kclassPath_State = "org.electronvolts.evlib.statemachine.internal.State"
const val kclassPath_StateFunction = "org.electronvolts.StateFunction"

private fun stateTypeFromType(type: KSType): KSType? {
    return when (type.declaration.qualifiedName?.asString()) {
        kclassPath_State -> type
        else -> stateTypeFromDecl(type.declaration)
    }
}

private fun stateTypeFromDecl(decl: KSDeclaration): KSType? {
    return when (decl) {
        is KSClassDeclaration -> {
            decl.getAllSuperTypes().find {
                it.declaration.qualifiedName!!.asString() == kclassPath_State
            }
        }
        is KSTypeParameter -> {
            decl.bounds.find { type ->
                stateTypeFromType(type.resolve()) != null
            }?.resolve()
        }
        is KSFunctionDeclaration -> {
            when (val ret = decl.returnType) {
                null -> throw RuntimeException("Nothing is returned from the given function")
                else -> stateTypeFromType(ret.resolve())
            }
        }
        else -> {
            throw RuntimeException(
                "Faulty declaration " +
                    "${decl.simpleName.asString()} | ${decl.qualifiedName?.asString()} " +
                    "passed to symbol processor"
            )
        }
    }
}

private fun getStateNameType(decl: KSDeclaration): KSTypeArgument {
    val simpleName = decl.simpleName.asString()
    return when (val stateClass = stateTypeFromDecl(decl)) {
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

/**
 * A representation of both extension functions for classes in an Evlib `StateMachineBuilder` and
 * `StateSequenceBuilder`. It contains functions to generate receivers for these types, and builders
 * to create them from function or class definitions related to Evlib `State`s.
 *
 * @param name The name of the class or function that is being represented
 * @param location The fully qualified location of the name or class
 * @param nameType The name of the parameter used as the `StateName` parameter to the `State` the
 *      class or return type inherits from, directly or indirectly
 * @param typeParams The type parameters which the function or class is generic over
 * @param params The parameters which the constructor or function takes
 */
class StateFunction private constructor(
    private val name: String,
    private val location: String,
    private val nameType: KSType,
    private val typeParams: List<KSTypeParameter>,
    private val params: List<KSValueParameter>,
) {
    companion object {
        fun fromClassDeclaration(
            klass: KSClassDeclaration,
        ): List<StateFunction> {
            return klass.getConstructors().map {
                // check if the constructor function is already annotated
                when (val trespasser =
                    it.annotations.find { ann -> ann.shortName.asString() == "StateFunction" }) {
                    null -> {}
                    else -> {
                        if (
                            trespasser
                                .annotationType
                                .resolve()
                                .declaration
                                .qualifiedName!!
                                .asString() == kclassPath_StateFunction
                        ) {
                            throw RuntimeException(
                                """The class ${klass.simpleName.asString()} | ${klass.qualifiedName?.asString() ?: ""}, which is annotated with `StateFunction`, contains a constructor that is also annotated with `StateFunction`.

This would result in double generation of the constructor, which cannot compile."""
                            )
                        }
                    }
                } // TODO: Filter constructors on visibility
                fromConstructor(it)
            }.toList()
        }

        fun fromConstructor(function: KSFunctionDeclaration): StateFunction {
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

            val location = if (function.isConstructor()) {
                returnTypeDecl.qualifiedName!!.asString()
            } else {
                function.qualifiedName!!.asString()
            }

            val name = if (function.isConstructor()) {
                returnTypeDecl.simpleName.asString()
            } else {
                function.simpleName.asString().replaceFirstChar { it.titlecase() }
            }

            val typeParameters = if (function.isConstructor()) {
                returnTypeDecl.typeParameters
            } else {
                function.typeParameters
            }

            return StateFunction(
                name = name,
                nameType = nameType,
                location = location,
                params = function.parameters,
                typeParams = typeParameters,
            )
        }
    }

    private fun genParameters() = this.params.map { param ->
        val simpleTypeName = param.type.resolve().declaration.simpleName
        val qualifiedTypeName = param.type.resolve().declaration.qualifiedName!!

        if (this.typeParams.any {
                it.name.asString() == simpleTypeName.asString() // TODO: Hacky, don't do it
            }) {
            Pair(param.name!!.asString(), simpleTypeName.asString())
        } else {
            Pair(param.name!!.asString(), qualifiedTypeName.asString())
        }
    }.toCollection(mutableListOf())

    private fun genOutsideParameters() =
        listOf(Pair("thisState", this.nameType.toString())) + genParameters()

    private fun argumentSignature() = "(\n${
        genOutsideParameters().joinToString(",\n") {
            "    ${it.first}: ${it.second}"
        }
    }\n)"

    private fun genGenericParameters(): String {
        return if (this.typeParams.isEmpty()) {
            "<$nameType: org.electronvolts.evlib.statemachine.internal.StateName>"
        } else {
            reconstructTypeParameters(this.typeParams.asSequence())
        }
    }

    private fun genGenericArguments(): String {
        return this.typeParams.joinToString(", ", "<", ">") { arg ->
            arg.name.asString()
        }
    }

    private fun genExprClosed() =
        """ = this.add(
        |    thisState,
        |    $location${genGenericArguments()}(
        |        ${genParameters().joinToString(",\n") { "${it.first} = ${it.first}" }}
        |    )
        |)
        """.trimMargin()

    private fun genExprOpen() =
        """ = this.add(
        |    thisState,
        |    asOpenState($location${genGenericArguments()}(
        |        ${genParameters().joinToString(",\n") { "${it.first} = ${it.first}" }}
        |    ))
        |)
        """.trimMargin()

    fun toClosedStateFunction(): String {
        val signature =
            "fun ${genGenericParameters()} " +
                "StateMachineBuilder<${this.nameType}>.add${this.name}${argumentSignature()}"
        return "$signature${genExprClosed()}"
    }

    fun toOpenStateFunction(): String {
        val signature =
            "fun ${genGenericParameters()} " +
                "StateSequenceBuilder<${this.nameType}>.add${this.name}${argumentSignature()}"
        return "$signature${genExprOpen()}"
    }
}

class StateFunctionVisitor : KSVisitor<Unit, Sequence<StateFunction>> {
    override fun visitAnnotated(annotated: KSAnnotated, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitCallableReference(
        reference: KSCallableReference,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit
    ) = StateFunction.fromClassDeclaration(classDeclaration).asSequence()

    override fun visitClassifierReference(
        reference: KSClassifierReference,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDeclarationContainer(
        declarationContainer: KSDeclarationContainer,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDynamicReference(
        reference: KSDynamicReference,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitFile(file: KSFile, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: Unit
    ) = sequenceOf(StateFunction.fromConstructor(function))

    override fun visitModifierListOwner(
        modifierListOwner: KSModifierListOwner,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitNode(node: KSNode, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitParenthesizedReference(
        reference: KSParenthesizedReference,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyAccessor(
        accessor: KSPropertyAccessor,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyGetter(
        getter: KSPropertyGetter,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertySetter(
        setter: KSPropertySetter,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitReferenceElement(
        element: KSReferenceElement,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeArgument(
        typeArgument: KSTypeArgument,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeParameter(
        typeParameter: KSTypeParameter,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeReference(
        typeReference: KSTypeReference,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitValueArgument(
        valueArgument: KSValueArgument,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitValueParameter(
        valueParameter: KSValueParameter,
        data: Unit
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

}