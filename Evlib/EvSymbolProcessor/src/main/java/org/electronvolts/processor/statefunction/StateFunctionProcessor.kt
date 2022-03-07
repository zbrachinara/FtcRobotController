package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import org.electronvolts.processor.plusAssign

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
            .filterIsInstance<KSClassDeclaration>()
            .forEach {
                definitions.add(StateFunction.fromClassDeclaration(it, logger))
                logger.info(it.qualifiedName.toString())
            }

        resolver
            .getSymbolsWithAnnotation("org.electronvolts.StateFunction")
            .filterIsInstance<KSFunctionDeclaration>()
            .forEach {
                definitions.add(StateFunction.fromConstructor(it, logger))
            }

        return emptyList()
    }

    override fun finish() {
        val file = generator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "org.electronvolts.evlib.statemachine.statefunction",
            fileName = "ClosedStates"
        )
        file += """
            package org.electronvolts.evlib.statemachine.statefunction
            
            import org.electronvolts.evlib.statemachine.internal.StateName
            import org.electronvolts.evlib.statemachine.StateMachineBuilder
            
            
        """.trimIndent()

        file += "\n"

        definitions.forEach {
            file += "${it.toClosedState()}\n\n"
        }

        file.close()
    }
}