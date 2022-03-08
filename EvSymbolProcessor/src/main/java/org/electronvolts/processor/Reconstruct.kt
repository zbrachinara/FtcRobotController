package org.electronvolts.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter

fun reconstructTypeParameters(tys: Sequence<KSTypeParameter>): String {


    return tys.joinToString(", ", "<", ">") { ty ->

        val boundsList = ty.bounds.toList()
        val bounds = if (boundsList.isEmpty()) {
            ""
        } else {
            ": ${boundsList.joinToString(", ") { reconstructType(it.resolve()) }}"
        }

        "${ty.variance.label}${
            if (ty.isReified) {
                "reified"
            } else {
                ""
            }
        } ${ty.name.asString()}$bounds"
    }
}

fun reconstructType(ty: KSType): String {
    return when (val decl = ty.declaration) {
        is KSTypeParameter -> {
            decl.name.asString()
        }
        is KSClassDeclaration -> {
            val typeArgs = ty.arguments.joinToString(", ") {
                reconstructType(it.type!!.resolve())
            }

            "${decl.qualifiedName!!.asString()}${
                if (typeArgs.isBlank()) {
                    ""
                } else {
                    "<${typeArgs}>"
                }
            }"
        }
        else -> {
            throw RuntimeException("Unknown declaration: ${decl.simpleName.asString()}")
        }
    }
//    if decl.
//    "${decl.qualifiedName!!}"
}
