package org.electronvolts.evlib.jni

val NATIVE_INSTANCE = Native()

class Native {
    init {
        System.loadLibrary("ev_native")
    }

    external fun getStr(s: String): String
}