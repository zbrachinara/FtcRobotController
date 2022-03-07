package org.electronvolts.processor.statefunction

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import org.electronvolts.processor.plusAssign
import java.io.OutputStream

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
        val symbols = resolver
            .getSymbolsWithAnnotation("org.electronvolts.StateFunction")
            .filterIsInstance<KSClassDeclaration>()

        symbols.forEach {
            definitions.add(StateFunction.fromClass(it))
            logger.info(it.qualifiedName.toString())
        }

        return emptyList()
    }

    override fun finish() {
        val file = generator.createNewFile(
            dependencies = Dependencies.ALL_FILES,
            packageName = "org.electronvolts.evlib.statemachine.states",
            fileName = "StdStates"
        )
        file += "package org.electronvolts.evlib.statemachine.states\n"

        definitions.forEach {
            file += "$it\n"
        }

        file.close()
    }
}

class StateClassVisitor(private val f: OutputStream) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val name = "add" + classDeclaration.simpleName.toString()
    }
}