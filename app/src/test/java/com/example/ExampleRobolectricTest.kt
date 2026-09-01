package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.EqualizerSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("12 Band EQ", appName)
    }

    @Test
    fun `test 12 bands default configuration`() {
        val defaultBands = EqualizerSettings.DEFAULT_12_BANDS
        assertEquals(12, defaultBands.size)
        assertEquals(32, defaultBands[0].centerFreqHz)
        assertEquals(20000, defaultBands[11].centerFreqHz)
    }
}

