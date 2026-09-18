package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Anika", appName)
  }

  @Test
  fun `verify Anika identity and creator rules`() {
    val nameResponse = com.example.engine.AnikaPersona.getLocalResponse(
        "Tumhara naam kya hai?",
        com.example.data.model.PersonalityMode.NORMAL
    )
    assertEquals("Main Anika hoon.", nameResponse)

    val creatorResponse = com.example.engine.AnikaPersona.getLocalResponse(
        "Tumhe kisne banaya hai?",
        com.example.data.model.PersonalityMode.NORMAL
    )
    assertEquals("Mujhe banane wale ka naam Saad Babu hai.", creatorResponse)
  }
}
