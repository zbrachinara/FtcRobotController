package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver

const val stateClass = "org.electronvolts.evlib.statemachine.internal.OpenState"
const val stateFunctionClass = "org.electronvolts.StateFunction"

private fun stateTypeFromType(type: KSType): KSType? {
    return when (type.declaration.qualifiedName?.asString()) {
        stateClass -> type
        else -> stateTypeFromDecl(type.declaration)
    }
}

private fun stateTypeFromDecl(decl: KSDeclaration): KSType? {
    return when (decl) {
        is KSClassDeclaration -> {
            decl.getAllSuperTypes().find {
                it.declaration.qualifiedName!!.asString() == stateClass
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
                "`${org.electronvolts.processor.statefunction.stateClass}`, which is required for this annotation"
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
 * Errors generated by the *compiler* as a result of the output of this class is a **bug**. If code
 * generated by this class would fail to compile, the class itself should raise an error and provide
 * a pointed description of the problem.
 *
 * @param name The name of the class or function that is being represented
 * @param location The fully qualified location of the name or class
 * @param nameType The name of the parameter used as the `StateName` parameter to the `State` the
 *      class or return type inherits from, directly or indirectly
 * @param typeParams The type parameters which the function or class is generic over
 * @param params The parameters which the constructor or function takes
 */
@OptIn(KotlinPoetKspPreview::class)
class StateFunction private constructor(
    private val name: String,
    private val location: String,
    private val nameType: KSType,
    private val typeParams: List<KSTypeParameter>,
    private val params: List<KSValueParameter>,
) {

    private val paramResolver = typeParams.toTypeParameterResolver()
    private val parameterizedStateMachine = ClassName("org.electronvolts.evlib.statemachine",
        "StateMachineBuilder").parameterizedBy(this.nameType.toTypeName(this.paramResolver))

    companion object {
        fun fromClassDeclaration(
            klass: KSClassDeclaration,
        ): List<StateFunction> {
            return klass.getConstructors()
                .onEach {
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
                                    .asString() == stateFunctionClass
                            ) {
                                throw RuntimeException(
                                    """The class ${klass.simpleName.asString()} | ${klass.qualifiedName?.asString() ?: ""}, which is annotated with `StateFunction`, contains a constructor that is also annotated with `StateFunction`.

This would result in double generation of the constructor, which cannot compile."""
                                )
                            }
                        }
                    }
                }
                .filter {
                    it.isPublic()
                }
                .map { fromConstructor(it) }.toList()
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

            if (!function.isPublic()) {
                throw RuntimeException(
                    "Only public functions/classes/constructors can be" +
                        "generated"
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

        ParameterSpec.builder(
            param.name!!.asString(),
            param.type.toTypeName(this.paramResolver)
        ).build()

//        val simpleTypeName = param.type.resolve().declaration.simpleName
//        val qualifiedTypeName = param.type.resolve().declaration.qualifiedName!!
//
//        // checking if the type of the parameter is a type parameter (not a known class/interface)
//        if (this.typeParams.any {
//                it.name.asString() == simpleTypeName.asString() // TODO: Hacky, don't do it
//            }) {
//            Pair(param.name!!.asString(), simpleTypeName.asString())
//        } else {
//            Pair(param.name!!.asString(), qualifiedTypeName.asString())
//        }
    }

    private fun argumentSignature(vararg addl: ParameterSpec) =
        addl.asList() + genParameters().toList()

    private fun genGenericParameters(): List<TypeVariableName> {
        return if (this.typeParams.isEmpty()) {
            listOf(TypeVariableName(
                this.nameType.toString(),
                ClassName("org.electronvolts.evlib.statemachine.internal", "OpenState")
            ))
        } else {
            this.typeParams.map { type ->
                TypeVariableName(
                    type.name.getShortName(),
                    type.bounds.map { bound -> bound.toTypeName(this.paramResolver) }
                        .toList(),
                    // TODO: Encode variance
                )
            }
        }
    }

//    private fun genExprClosed() =
//        """
//        |this.add(
//        |    thisState,
//        |    $location${genGenericArguments()}(
//        |        ${genParameters().joinToString(",\n") { "${it.first} = ${it.first}" }}
//        |    )(nextState)
//        |)
//        """.trimMargin()
//
//    private fun genExprOpen() =
//        """ = this.add(
//        |    thisState,
//        |    $location${genGenericArguments()}(
//        |        ${genParameters().joinToString(",\n") { "${it.first} = ${it.first}" }}
//        |    )
//        |)
//        """.trimMargin()

    fun toClosedStateFunction(): FunSpec =
        FunSpec.builder("add${this.name}")
            .receiver(this.parameterizedStateMachine)
            .returns(this.parameterizedStateMachine)
            .addTypeVariables(this.genGenericParameters())
            .addParameters(argumentSignature(
                ParameterSpec.builder("thisState", this.nameType.toTypeName(this.paramResolver))
                    .build(),
                ParameterSpec.builder("nextState", this.nameType.toTypeName(this.paramResolver))
                    .build()
            ))
            .addCode(CodeBlock.builder()
                .addStatement("this.add()")
                .addStatement("return this")
                .build()
            )
            .build()

//    fun toOpenStateFunction(): String {
//        val nameTypeStr = this.nameType.declaration.simpleName.asString()
//        val signature =
//            "fun ${genGenericParameters()} " +
//                "StateSequenceBuilder<${this.nameType.declaration.simpleName.asString()}>.add${this.name}" +
//                argumentSignature(listOf(Pair("thisState", nameTypeStr)))
//        return "$signature${genExprOpen()}"
//    }
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
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitClassDeclaration(
        classDeclaration: KSClassDeclaration,
        data: Unit,
    ) = StateFunction.fromClassDeclaration(classDeclaration).asSequence()

    override fun visitClassifierReference(
        reference: KSClassifierReference,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDeclarationContainer(
        declarationContainer: KSDeclarationContainer,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitDynamicReference(
        reference: KSDynamicReference,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitFile(file: KSFile, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitFunctionDeclaration(
        function: KSFunctionDeclaration,
        data: Unit,
    ) = sequenceOf(StateFunction.fromConstructor(function))

    override fun visitModifierListOwner(
        modifierListOwner: KSModifierListOwner,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitNode(node: KSNode, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitParenthesizedReference(
        reference: KSParenthesizedReference,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyAccessor(
        accessor: KSPropertyAccessor,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyDeclaration(
        property: KSPropertyDeclaration,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertyGetter(
        getter: KSPropertyGetter,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitPropertySetter(
        setter: KSPropertySetter,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitReferenceElement(
        element: KSReferenceElement,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeArgument(
        typeArgument: KSTypeArgument,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeParameter(
        typeParameter: KSTypeParameter,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitTypeReference(
        typeReference: KSTypeReference,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitValueArgument(
        valueArgument: KSValueArgument,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

    override fun visitValueParameter(
        valueParameter: KSValueParameter,
        data: Unit,
    ): Sequence<StateFunction> {
        TODO("Not yet implemented")
    }

}