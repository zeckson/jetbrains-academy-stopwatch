package org.hyperskill.stopwatch

object StringUtil {
    fun time(time: Int) = if (time < 10) "0$time" else "$time"
}