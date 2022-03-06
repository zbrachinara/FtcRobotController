package org.electronvolts.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

class StateFunctionProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = StateFunctionProcessor(
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
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation("org.electronvolts.StateFunction")
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach {
            logger.info(it.qualifiedName.toString())
        }

        return emptyList()
    }
}