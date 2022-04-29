package org.electronvolts.evlib.options

import android.os.Environment
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.electronvolts.evlib.util.Path
import java.io.File

/**
 * Controller for both in-memory and persistent storage of options.
 */
// TODO: 1/21/22 Options with conflicting names can overwrite each other. Enforce!
class OptionFile(path: Path) {

    private val file: File = (
        Path(Environment.getExternalStorageDirectory())
            + Path("FTC/options")
            + path
        ).file()

    private var optionMap: MutableMap<String, Any> = mutableMapOf()

    /**
     * Gets the value of the described option
     */
    // Developer's note: The unchecked cast and the redundant return type specification are
    // essential to the behavior of this function. It prevents the `Any?` type from leaking out of
    // the function from the `optionMap`.
    fun <T : Any> get(option: OptionClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return optionMap[option.name] as T? ?: run {
            optionMap[option.name] = option.typeData.default
            option.typeData.default
        }
    }

    /**
     * Sets the described option to the given value
     */
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