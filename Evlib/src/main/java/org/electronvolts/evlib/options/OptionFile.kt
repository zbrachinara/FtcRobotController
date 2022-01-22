package org.electronvolts.evlib.options

import android.os.Environment
import org.electronvolts.evlib.util.Path
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.File
import java.io.FileReader
import java.io.FileWriter

// TODO: 1/21/22 Options with conflicting names can overwrite each other. Enforce!
class OptionFile(path: File) {

    private val file: File = (
            Path(Environment.getExternalStorageDirectory())
                    + Path("FTC")
                    + Path("options")
                    + Path(path)
            ).file()

    private var optionMap: JSONObject = JSONParser().parse(FileReader(file)) as JSONObject

    // Developer's note: The redundant return type declaration is a reminder of the ergonomics of
    // this function. If the return type was removed, this function was still compile. However, if
    // only the unsafe cast was removed, then the function would fail to compile. If both were
    // removed, then the function would still compile, but it would have a return type of `Any?`.
    // This is problematic, because the unsafe cast would have to be performed (potentially
    // incorrectly) outside of this function. The return type is therefore kept to prevent incorrect
    // behavior from leaking out.
    fun <T> get(option: OptionClass<T>): T {
        return optionMap[option.name] as T ?: run {
            val serializer = option.typeData.serializer
            optionMap[option.name] = serializer.toString(option.typeData.default)
            option.typeData.default
        }
    }

    fun <T> set(option: OptionClass<T>, value: T) {
        optionMap[option.name] = value
    }

    /**
     * Sync changes made in memory into the options file
     */
    fun sync() {
        optionMap.writeJSONString(FileWriter(file))
    }

    /**
     * Forget changes made in memory and restore the options file
     */
    fun forget() {
        optionMap = JSONParser().parse(FileReader(file)) as JSONObject
    }

}