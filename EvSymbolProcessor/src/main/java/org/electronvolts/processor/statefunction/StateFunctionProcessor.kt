package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import org.electronvolts.processor.plusAssign

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

    override fun finish() {
        //TODO: Support multiple file sets in order to process multiple projects
        val stateFile = generator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = containingPackage,
            fileName = "StateFunctions"
        )
        stateFile += """
            package $containingPackage
            
            import org.electronvolts.evlib.statemachine.internal.asOpenState
            import org.electronvolts.evlib.statemachine.internal.StateName
            import org.electronvolts.evlib.statemachine.StateMachineBuilder
            import org.electronvolts.evlib.statemachine.StateSequenceBuilder
            
            
        """.trimIndent()

        definitions.forEach {
            stateFile += "${it.toClosedStateFunction()}\n\n"
            stateFile += "${it.toOpenStateFunction()}\n\n"
        }

        stateFile.close()
    }
}