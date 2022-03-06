package org.electronvolts.evlib.util

import java.io.File

class Path private constructor(
    raw: List<String>,
    val absolute: Boolean
) {

    constructor(path: String) : this(path.split('/').filter { it != "" }, path[0] == '/')
    constructor(file: File) : this(file.path)

    private val components: MutableList<String> = raw.toMutableList()

    operator fun plus(rhs: Path) = when (rhs.absolute) {
        false -> {
            val rightIterator: Iterable<String> = rhs.components.asIterable()
            Path(this.components + rightIterator, this.absolute)
        }
        true -> throw IllegalArgumentException("Attempted to add an absolute path to a path")
    }

    override fun toString() =
        if (absolute) {
            "/"
        } else {
            ""
        } + components.joinToString("/")


    fun file() = File(this.toString())

}