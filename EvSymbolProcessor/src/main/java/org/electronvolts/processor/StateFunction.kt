package org.electronvolts.processor

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import java.io.OutputStream

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

        val file = generator.createNewFile(
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = "org.electronvolts.evlib.statemachine.states",
            fileName = "StdStates"
        )
        file += "package org.electronvolts.evlib.statemachine.states\n"

        symbols.forEach {
            it.accept(StateClassVisitor(file), Unit)
            logger.info(it.qualifiedName.toString())
        }

        return emptyList()
    }
}

class StateClassVisitor(private val f: OutputStream) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val name = "add" + classDeclaration.simpleName.toString()
    }
}