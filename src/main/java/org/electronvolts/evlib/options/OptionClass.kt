package electronvolts.options

import electronvolts.gamepad.GamepadManager
import electronvolts.util.clamp
import kotlin.reflect.KClass

private typealias Mutator<T> = (GamepadManager, T) -> T?

interface TypeData<T> {

    val default: T
    val mutator: (GamepadManager, T) -> T?
    val serializer: Serializer<T>

    class Builder<T> {

        companion object {
            fun int(
                min: Int,
                max: Int,
                step: Int,
            ) = Builder<Int>()
                .default(min)
                .mutator { gamepad, i ->
                    when {
                        gamepad.right_bumper.justPressed() -> (i + step).clamp(min, max)
                        gamepad.left_bumper.justPressed() -> (i - step).clamp(min, max)
                        else -> null
                    }
                }
                .serializer(object : Serializer<Int> {
                    override fun toString(o: Int): String = o.toString()
                    override fun fromString(s: String) = s.toInt()
                })
        }

        var default: T? = null
        var mutator: Mutator<T>? = null
        var serializer: Serializer<T>? = null

        fun default(default: T): Builder<T> {
            this.default = default
            return this
        }

        fun mutator(mutator: Mutator<T>): Builder<T> {
            this.mutator = mutator
            return this
        }

        fun serializer(serializer: Serializer<T>): Builder<T> {
            this.serializer = serializer
            return this
        }

        fun build() = object : TypeData<T> {
            override val default = this@Builder.default!!
            override val mutator = this@Builder.mutator!!
            override val serializer = this@Builder.serializer!!
        }

    }
}

interface OptionClass<T> {

    companion object {
        fun <T> define(name: String, builder: TypeData.Builder<T>) = object : OptionClass<T> {
            override val typeData = builder.build()
            override val name = name
        }
    }

    val typeData: TypeData<T>
    val name: String
}