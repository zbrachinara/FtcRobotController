package electronvolts.util

import electronvolts.util.unit.Duration
import electronvolts.util.unit.Instant
import electronvolts.util.unit.*

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