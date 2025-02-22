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
                null -> throw InvalidStateFunction.NoReturn(decl)
                else -> stateTypeFromType(ret.resolve())
            }
        }
        else -> {
            throw RuntimeException(
                "Faulty declaration ${displayDecl(decl)} passed to symbol processor"
            )
        }
    }
}

private fun getStateNameType(decl: KSDeclaration): KSTypeArgument {
    return when (val stateClass = stateTypeFromDecl(decl)) {
        null -> throw InvalidStateFunction.NotState(decl)
        else -> stateClass.innerArguments[0]
    }
}

fun displayDecl(decl: KSDeclaration) = (decl.qualifiedName ?: decl.simpleName).asString()

class ConflictingStateFunctions(klass: KSClassDeclaration) : RuntimeException(
    "The class ${displayDecl(klass)}, which is annotated with `StateFunction`, contains a " +
        "constructor that is also annotated with `StateFunction`. This would result in double " +
        "generation of the constructor wrapper, which will not compile."
)

sealed class InvalidStateFunction(message: String) : RuntimeException(message) {
    class RequiresObject(fn: KSFunctionDeclaration) : InvalidStateFunction("Cannot call function " +
        "`${displayDecl(fn)}` since that would require an object reference (this is not yet " +
        "supported)")

    class NonPublic(fn: KSFunctionDeclaration) : InvalidStateFunction("Cannot call function " +
        "`${displayDecl(fn)}` as it is not accessible from this call site")

    class NotState(decl: KSDeclaration) : InvalidStateFunction("The type `${displayDecl(decl)} " +
        "is not a `State` or an `OpenState`, which is required for insertion into the state " +
        "machine")

    class NoReturn(fn: KSFunctionDeclaration) : InvalidStateFunction("The function " +
        "`${displayDecl(fn)}` does not return anything")
}

/**
 * A representation of both extension functions for Evlib `StateMachineBuilder` and
 * `StateSequenceBuilder`. These extension functions can generate extension functions for the given
 * function, constructor, or class, given that those classes are public
 *
 * Most errors generated by the *compiler* as a result of the code generated by this class is a bug.
 * If code generated by this class would fail to compile, the symbol processor itself should raise
 * an error and provide a description of the problem. One exception to this rule is the detection of
 * colliding names. This task is delegated to the compiler because the amount of work necessary to
 * make it work on a symbol processor makes no sense, and the effect should be pretty visible on
 * code before generation
 *
 * @param emitter The name of the class or function that produces the `OpenState`
 * @param nameType The name of the parameter used as the `StateName` parameter to the `State` the
 *      class or return type inherits from, directly or indirectly
 * @param typeParams The type parameters which the function or class is generic over
 * @param params The parameters which the constructor or function takes
 */
@OptIn(KotlinPoetKspPreview::class)
class StateFunction private constructor(
    private val emitter: KSDeclaration,
    private val nameType: KSType,
    private val typeParams: List<KSTypeParameter>,
    private val params: List<KSValueParameter>,
) {

    private val paramResolver = typeParams.toTypeParameterResolver()
    private val stateName = this.nameType.toTypeName(this.paramResolver)
    private val stateMachineType = ClassName("org.electronvolts.evlib.statemachine",
        "StateMachineBuilder").parameterizedBy(this.stateName)
    private val stateSequenceType = ClassName("org.electronvolts.evlib.statemachine",
        "StateSequenceBuilder").parameterizedBy(this.stateName)
    private val emitterName = emitter.simpleName.asString().replaceFirstChar { c -> c.uppercase() }

    companion object {
        fun fromClassDeclaration(
            klass: KSClassDeclaration,
        ): List<StateFunction> {
            return klass.getConstructors()
                .onEach { constructor ->
                    constructor.annotations.forEach { ann ->
                        // check if the constructor function is already annotated
                        if (
                            ann.annotationType.toTypeName() ==
                            org.electronvolts.StateFunction::class.asTypeName()
                        ) {
                            throw ConflictingStateFunctions(klass)
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
                throw InvalidStateFunction.RequiresObject(function)
            }

            if (!function.isPublic()) {
                throw InvalidStateFunction.NonPublic(function)
            }

            val nameType = getStateNameType(function).type!!.resolve()

            val returnTypeDecl = function.returnType!!.resolve().declaration

            val decl = if (function.isConstructor()) {
                returnTypeDecl
            } else {
                function
            }

            val typeParameters = if (function.isConstructor()) {
                returnTypeDecl.typeParameters
            } else {
                function.typeParameters
            }

            return StateFunction(
                emitter = decl,
                nameType = nameType,
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
    }

    private fun argumentSignature(vararg additional: ParameterSpec) =
        additional.asList() + genParameters().toList()

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

    private fun genGenericArguments() = "<${genGenericParameters().joinToString(", ")}>"
    private fun passthroughParameters() =
        genParameters().joinToString(", ") { param -> "${param.name} = ${param.name}" }

    fun toClosedStateFunction() = FunSpec.builder("add${this.emitterName}")
        .receiver(this.stateMachineType)
        .returns(this.stateMachineType)
        .addTypeVariables(this.genGenericParameters())
        .addParameters(argumentSignature(
            ParameterSpec.builder("thisState", this.stateName).build(),
            ParameterSpec.builder("nextState", this.stateName).build()
        ))
        .addCode(CodeBlock.builder()
            // TODO: Prettify
            .addStatement("this.add(thisState," +
                "${this.emitter.packageName.asString()}.${this.emitter.simpleName.asString()}" +
                "${genGenericArguments()}(${passthroughParameters()})(nextState))"
            )
            .addStatement("return this")
            .build()
        )
        .build()

    fun toOpenStateFunction() = FunSpec.builder("add${this.emitterName}")
        .receiver(this.stateSequenceType)
        .returns(this.stateSequenceType)
        .addTypeVariables(this.genGenericParameters())
        .addParameters(argumentSignature(
            ParameterSpec.builder("thisState", stateName).build()
        ))
        .addCode(CodeBlock.builder()
            .addStatement("this.add(thisState," +
                "${this.emitter.packageName.asString()}.${this.emitter.simpleName.asString()}" +
                "${genGenericArguments()}(${passthroughParameters()}))"
            )
            .addStatement("return this")
            .build()
        )
        .build()
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