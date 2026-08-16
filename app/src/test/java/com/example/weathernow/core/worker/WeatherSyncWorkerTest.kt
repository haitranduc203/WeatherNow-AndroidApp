package com.example.weathernow.core.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.weathernow.WeatherNowApp
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = WeatherNowApp::class)
class WeatherSyncWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
    }

    @Test
    fun testWorkerInstantiation() {
        val worker = TestListenableWorkerBuilder<WeatherSyncWorker>(context).build()
        assertNotNull(worker)
    }

    @Test
    fun testWorkerDisabledWhenBackgroundSyncOff() = runBlocking {
        val app = context as WeatherNowApp
        // Turn off background sync
        app.appContainer.userPreferencesRepository.setBackgroundRefreshEnabled(false)

        val worker = TestListenableWorkerBuilder<WeatherSyncWorker>(context).build()
        val result = worker.doWork()

        // Should return success immediately without hitting network
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun testWorkSchedulerPeriodicScheduling() {
        WeatherWorkScheduler.schedulePeriodicSync(context, intervalHours = 3)
        // Verify cancel does not throw exception
        WeatherWorkScheduler.cancelPeriodicSync(context)
    }
}
