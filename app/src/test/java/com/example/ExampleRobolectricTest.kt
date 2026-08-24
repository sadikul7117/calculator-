package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.calculator.engine.MathEvaluator
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
    assertEquals("This Calculator Created by Sadikul", appName)
  }

  @Test
  fun `test fraction calculation from prompt reference`() {
    val evaluator = MathEvaluator()
    // Test: (2/3 + 4 3/5) * 5 = (2/3 + 23/5) * 5 = (10/15 + 69/15) * 5 = (79/15) * 5 = 79/3 = 26 1/3
    // Test: (2/3 + 4 + 3/5) * 5
    val res = evaluator.evaluate("(2/3 + 23/5) * 5")
    assertEquals("26 1/3", res.displayStr)
  }
}

