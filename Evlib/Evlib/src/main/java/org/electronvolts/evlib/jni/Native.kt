package org.electronvolts.evlib.jni

class Native {
    init {
        System.loadLibrary("evlib_native")
    }

    external fun getStr(s: String): String
}