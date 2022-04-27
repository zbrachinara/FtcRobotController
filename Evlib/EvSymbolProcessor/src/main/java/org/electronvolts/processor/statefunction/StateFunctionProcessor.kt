package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.writeTo

const val containingPackage = "org.electronvolts.evlib.statemachine.statefunction"

class StateFunctionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) =
        StateFunctionProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options,
        )
}

class StateFunctionProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>,
) : SymbolProcessor {

    private val definitions: MutableList<StateFunction> = arrayListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver
            .getSymbolsWithAnnotation("org.electronvolts.StateFunction")
            .forEach {
                definitions += it.accept(StateFunctionVisitor(), Unit)
            }

        return emptyList()
    }

    @OptIn(KotlinPoetKspPreview::class)
    override fun finish() {
        //TODO: Support multiple file sets in order to process multiple projects
//        val codeFile = generator.createNewFile(
//            dependencies = Dependencies.ALL_FILES,
//            packageName = containingPackage,
//            fileName = "StateFunctions"
//        )

        val builder = FileSpec.builder(containingPackage, "StateFunctions")
            .addImport("org.electronvolts.evlib.statemachine.internal", "StateName")
            .addImport("org.electronvolts.evlib.statemachine", "StateMachineBuilder")
            .addImport("org.electronvolts.evlib.statemachine", "StateSequenceBuilder")

        definitions.forEach {
            builder.addFunction(it.toClosedStateFunction())
//            builder.addCode(it.toOpenStateFunction())
        }

        builder.build().writeTo(generator, Dependencies.ALL_FILES)

//        codeFile.close()
    }
}