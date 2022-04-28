package org.electronvolts.evlib.jni

val NATIVE_INSTANCE = Native()

class Native {
    init {
        System.loadLibrary("evlib_native")
    }

    external fun getStr(s: String): String
}