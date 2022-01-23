package org.electronvolts.evlib.util

import org.electronvolts.evlib.util.unit.Duration
import java.util.*

class TimerOnce(private val duration: Duration) {

    var finished = false
        private set

    fun init() {
        Timer().schedule(object : TimerTask() {
            override fun run() {
                finished = true
            }
        }, duration.milliseconds().toLong())
    }

}