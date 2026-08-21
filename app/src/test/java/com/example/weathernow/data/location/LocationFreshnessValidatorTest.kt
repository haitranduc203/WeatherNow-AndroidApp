package com.example.weathernow.data.location

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationFreshnessValidatorTest {

    private val fixedNow = 1_000_000_000L // arbitrary fixed timestamp in ms
    private val validator = LocationFreshnessValidator(
        clock = { fixedNow },
        maxAgeMillis = 5 * 60 * 1000L // 300,000 ms (5 minutes)
    )

    @Test
    fun recentLastLocation_within5Minutes_isAccepted() {
        val locationTime = fixedNow - (4 * 60 * 1000L) // 4 minutes old
        assertTrue("4-minute-old location should be fresh", validator.isFresh(locationTime))
    }

    @Test
    fun exact5Minutes_isAccepted() {
        val locationTime = fixedNow - (5 * 60 * 1000L) // exactly 5 minutes old
        assertTrue("5-minute-old location should be fresh", validator.isFresh(locationTime))
    }

    @Test
    fun staleLastLocation_over5Minutes_isRejected() {
        val locationTime = fixedNow - (5 * 60 * 1000L + 1L) // 5 minutes and 1 ms old
        assertFalse("Over 5 minutes old location must be stale", validator.isFresh(locationTime))
    }

    @Test
    fun futureTimestamp_isRejected() {
        val locationTime = fixedNow + 10_000L // 10 seconds in future
        assertFalse("Future timestamp must be rejected as invalid", validator.isFresh(locationTime))
    }

    @Test
    fun zeroOrNegativeTimestamp_isRejected() {
        assertFalse("0 timestamp must be rejected", validator.isFresh(0L))
        assertFalse("Negative timestamp must be rejected", validator.isFresh(-1000L))
    }
}
