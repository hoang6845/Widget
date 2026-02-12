package com.brally.mobile.utils

class TimerHelper(private val durationMillis: Long = 45000, private val limitAds: Int = 10) {

    private var startTime: Long = System.currentTimeMillis()
    private var countReload = 0
    var isTimeReached: Boolean = false
        private set

    fun checkTimeReached(): Boolean {
        val elapsed = System.currentTimeMillis() - startTime
        if (elapsed >= durationMillis && !isTimeReached && countReload <= limitAds) {
            isTimeReached = false
            countReload += 1
            resetTimer()
            return true
        }
        return isTimeReached
    }

    fun resetTimer() {
        startTime = System.currentTimeMillis()
    }
}
