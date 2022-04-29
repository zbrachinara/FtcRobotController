package org.electronvolts.evlib.options

import org.electronvolts.evlib.gamepad.GamepadManager
import org.electronvolts.evlib.util.clamp

private typealias Mutator<T> = (GamepadManager, T) -> T?

/**
 * Represents the type of an option. Constructed by using the Builder class, giving a default object
 * and mutator function.
 *
 * @param T must register a kotlinx json serializer. Subject to change
 * @see org.electronvolts.evlib.opmode.AbstractOptionsOp
 */
class TypeData<T> private constructor(
    val default: T,
    val mutator: Mutator<T>,
) {
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
        }

        private var default: T? = null
        private var mutator: Mutator<T>? = null

        fun default(default: T): Builder<T> {
            this.default = default
            return this
        }

        fun mutator(mutator: Mutator<T>): Builder<T> {
            this.mutator = mutator
            return this
        }

        fun build() = TypeData(default!!, mutator!!)
    }
}

/**
 * Describes one option, consisting of its name and its TypeData
 * @see TypeData
 */
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