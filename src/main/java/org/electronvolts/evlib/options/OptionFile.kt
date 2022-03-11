package org.electronvolts.evlib.options

import android.os.Environment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.electronvolts.evlib.util.Path
import java.io.File

// TODO: 1/21/22 Options with conflicting names can overwrite each other. Enforce!
class OptionFile(path: Path) {

    private val file: File = (
        Path(Environment.getExternalStorageDirectory())
            + Path("FTC/options")
            + path
        ).file()

    private var optionMap: MutableMap<String, Any> = mutableMapOf()

    // Developer's note: The redundant return type declaration is a reminder of the ergonomics of
    // this function. If the return type was removed, this function was still compile. However, if
    // only the unsafe cast was removed, then the function would fail to compile. If both were
    // removed, then the function would still compile, but it would have a return type of `Any?`.
    // This is problematic, because the unsafe cast would have to be performed (potentially
    // incorrectly) outside of this function. The return type is therefore kept to prevent incorrect
    // behavior from leaking out.
    fun <T : Any> get(option: OptionClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return optionMap[option.name] as T? ?: run {
            optionMap[option.name] = option.typeData.default
            option.typeData.default
        }
    }

    fun <T : Any> set(option: OptionClass<T>, value: T) {
        optionMap[option.name] = value
    }

    /**
     * Sync changes made in memory into the options file
     */
    fun sync() {
        file.writeText(Json.encodeToString(optionMap))
    }

    /**
     * Forget changes made in memory and restore the options file
     */
    fun forget() {
        optionMap = Json.decodeFromString(file.readText())
    }

}