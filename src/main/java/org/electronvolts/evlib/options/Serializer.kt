package org.electronvolts.evlib.options

interface Serializer<T> {
    fun toString(o: T): String
    fun fromString(s: String): T
}