package org.electronvolts.evlib.util

import org.electronvolts.evlib.util.unit.Duration
import org.electronvolts.evlib.util.unit.Instant
import org.electronvolts.evlib.util.unit.*

class MatchTimer(
    time: Duration,
){

    val matchTime = time.milliseconds().toLong()

    lateinit var startTime: Instant
    lateinit var previousTime: Instant

    fun start() {
        startTime = Instant.now()
        previousTime = startTime
    }

    fun update() {
        previousTime = Instant.now()
    }

    fun finished() = Instant.now() > (startTime + matchTime.dunit())

    fun justFinished(): Boolean {
        val finish = startTime + matchTime.dunit()
        val now = Instant.now()

        return previousTime < finish && finish < now
    }

}