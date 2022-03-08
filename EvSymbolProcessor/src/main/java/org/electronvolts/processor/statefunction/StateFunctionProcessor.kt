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
        val closedStateFile = generator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = containingPackage,
            fileName = "ClosedStates"
        )
        closedStateFile += """
            package $containingPackage
            
            import org.electronvolts.evlib.statemachine.internal.StateName
            import org.electronvolts.evlib.statemachine.StateMachineBuilder
            
            
        """.trimIndent()

        definitions.forEach {
            closedStateFile += "${it.toClosedStateFunction()}\n\n"
        }

        closedStateFile.close()

        val openStateFile = generator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = containingPackage,
            fileName = "OpenStates"
        )
        openStateFile += """
            package $containingPackage 
            
            import org.electronvolts.evlib.statemachine.internal.asOpenState
            import org.electronvolts.evlib.statemachine.internal.StateName
            import org.electronvolts.evlib.statemachine.StateSequenceBuilder
            
            
        """.trimIndent()

        definitions.forEach {
            openStateFile += "${it.toOpenStateFunction()}\n\n"
        }

        openStateFile.close()
    }
}