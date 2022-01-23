package electronvolts.options

interface Serializer<T> {
    fun toString(o: T): String
    fun fromString(s: String): T
}