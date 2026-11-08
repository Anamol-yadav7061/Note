package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.ExternalNotesBackup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Anmol Notes", appName)
  }

  @Test
  fun `externalNotesBackup parses sample Keep JSON`() {
    val sampleJson = ExternalNotesBackup.getSampleKeepBackupData()
    val notes = ExternalNotesBackup.parseGoogleKeepJson(sampleJson, "test_user")
    assertEquals(3, notes.size)
    assertEquals("Python Automation Routine", notes[0].title)
    assertTrue(notes[0].tags.contains("Python"))
  }
}
