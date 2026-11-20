package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.domain.notification.CreditCardBillNotificationScheduler
import com.example.ui.MainScreen
import com.example.ui.theme.OmniFinCalcTheme
import com.example.ui.theme.ThemeManager

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ThemeManager.initialize(applicationContext)
    CreditCardBillNotificationScheduler.rescheduleAll(applicationContext)
    enableEdgeToEdge()

    setContent {
      OmniFinCalcTheme {
        MainScreen()
      }
    }
  }
}
