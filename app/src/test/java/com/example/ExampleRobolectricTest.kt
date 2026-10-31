package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.CbtRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    assertEquals("CBT PIK-R SMANDA", appName)
  }

  @Test
  fun `verify admin authentication`() {
    assertTrue(CbtRepository.verifyAdminPassword("admin123"))
  }

  @Test
  fun `verify pre-registered student login`() {
    val student = CbtRepository.loginStudent("siswa1", "123")
    assertNotNull(student)
    assertEquals("Ahmad Rizky Pratama", student?.fullName)
  }
}
