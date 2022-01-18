package electronvolts.util

import electronvolts.util.unit.Duration
import electronvolts.util.unit.Instant

class MatchTimer(
    private val time: Duration,
){

    val matchTime = time.milliseconds();

    var finished = false;
    lateinit var startTime: Instant
    lateinit var previousTime: Instant

    fun start() {
        startTime = Instant.now()
        previousTime = startTime
    }

}