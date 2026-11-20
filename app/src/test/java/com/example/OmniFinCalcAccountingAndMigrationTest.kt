package com.example

import com.example.data.local.AppDatabase
import com.example.data.local.entity.LoanRecord
import com.example.data.local.entity.SubscriptionRecord
import com.example.domain.backup.BackupParseResult
import com.example.domain.backup.FinanceBackupManager
import com.example.domain.calculator.EmiCalculator
import com.example.domain.calculator.SubscriptionDueDateCalculator
import com.example.domain.finance.MoneyMath
import com.example.domain.splitbill.DebtSimplifier
import com.example.domain.splitbill.Participant
import com.example.domain.splitbill.PaymentDeepLinkHelper
import com.example.domain.splitbill.ReceiptItem
import com.example.domain.splitbill.SplitMode
import com.example.ui.screens.analytics.AnalyticsDateRangeCalculator
import com.example.ui.screens.analytics.AnalyticsPeriod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.LocalDate
import java.time.ZoneId

class OmniFinCalcAccountingAndMigrationTest {

  @Test
  fun `launcher app_name resource is rebranded to OmniFinCalc with zero typos`() {
    val stringsFile = listOf(
      File("src/main/res/values/strings.xml"),
      File("app/src/main/res/values/strings.xml")
    ).first { it.exists() }
    val xml = stringsFile.readText()
    assertTrue("app_name must be OmniFinCalc", xml.contains("<string name=\"app_name\">OmniFinCalc</string>"))
    assertFalse("strings.xml must not contain old OmniCalc branding", xml.contains("OmniCalc", ignoreCase = true))
    assertFalse("strings.xml must not contain typo OnmiFinCalc", xml.contains("OnmiFinCalc", ignoreCase = true))
    assertEquals("Room database identity must remain unchanged", "omni_calc_db", AppDatabase.DATABASE_NAME)
  }

  @Test
  fun `room migration chain covers every version from 1 to 10 contiguously without destructive fallback`() {
    val migrations = AppDatabase.ALL_MIGRATIONS.sortedBy { it.startVersion }
    assertEquals("Must have 9 explicit migrations (1->10)", 9, migrations.size)
    assertEquals(1, migrations.first().startVersion)
    assertEquals(10, migrations.last().endVersion)
    for (i in 0 until migrations.size - 1) {
      assertEquals(
        "Migration chain must be contiguous",
        migrations[i].endVersion,
        migrations[i + 1].startVersion
      )
    }
  }

  @Test
  fun `money math avoids floating point drift across paise operations`() {
    val sum = MoneyMath.sumOfExact(listOf(0.1, 0.2, 0.3, 19.99, 0.01)) { it }
    assertEquals(20.60, sum, 0.0)
    assertEquals(0.0, MoneyMath.subtract(sum, 20.60), 0.0)
    assertEquals(100_000.05, MoneyMath.add(99_999.99, 0.06), 0.0)
    assertEquals(-250.75, MoneyMath.roundCurrency(-250.754), 0.0)
    assertEquals(0.0, MoneyMath.roundCurrency(Double.NaN), 0.0)
  }

  @Test
  fun `analytics period range calculation handles all 7 filters accurately`() {
    val today = LocalDate.of(2026, 6, 17) // Wednesday
    assertEquals(
      LocalDate.of(2026, 6, 15) to today,
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.THIS_WEEK, today, today, today)
    )
    assertEquals(
      LocalDate.of(2026, 6, 1) to today,
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.THIS_MONTH, today, today, today)
    )
    assertEquals(
      LocalDate.of(2026, 5, 1) to LocalDate.of(2026, 5, 31),
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.LAST_MONTH, today, today, today)
    )
    assertEquals(
      LocalDate.of(2026, 4, 1) to today,
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.LAST_3_MONTHS, today, today, today)
    )
    assertEquals(
      LocalDate.of(2026, 1, 1) to today,
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.LAST_6_MONTHS, today, today, today)
    )
    assertEquals(
      LocalDate.of(2026, 1, 1) to today,
      AnalyticsDateRangeCalculator.resolveRange(AnalyticsPeriod.THIS_YEAR, today, today, today)
    )
    // Swapped custom dates normalize cleanly
    assertEquals(
      LocalDate.of(2026, 3, 1) to LocalDate.of(2026, 3, 15),
      AnalyticsDateRangeCalculator.resolveRange(
        AnalyticsPeriod.CUSTOM,
        LocalDate.of(2026, 3, 15),
        LocalDate.of(2026, 3, 1),
        today
      )
    )
    val (startMs, endExclusiveMs) = AnalyticsDateRangeCalculator.toEpochBounds(
      LocalDate.of(2026, 6, 1),
      LocalDate.of(2026, 6, 1),
      ZoneId.of("UTC")
    )
    assertEquals(86_400_000L, endExclusiveMs - startMs)
  }

  @Test
  fun `backup parser supports both legacy OmniCalc and new OmniFinCalc payloads`() {
    val legacyJson = """
      {
        "app": "OmniCalc",
        "version": 1,
        "categories": [{"name": "Groceries", "colorHex": "#10B981"}],
        "bankAccounts": [{"id": 1, "bankName": "Legacy Bank", "accountType": "Savings", "openingBalance": 1000}],
        "trips": []
      }
    """.trimIndent()
    val legacyResult = FinanceBackupManager.parseFromJson(legacyJson)
    assertTrue(legacyResult is BackupParseResult.Success)
    assertEquals(
      "Legacy Bank",
      (legacyResult as BackupParseResult.Success).payload.bankAccounts.first().bankName
    )

    val newJson = """
      {
        "app": "OmniFinCalc",
        "brand": "OmniFinCalc",
        "version": 3,
        "categories": [{"name": "Dining", "colorHex": "#EF4444"}],
        "bankAccounts": [],
        "trips": []
      }
    """.trimIndent()
    val newResult = FinanceBackupManager.parseFromJson(newJson)
    assertTrue(newResult is BackupParseResult.Success)
    assertFalse((newResult as BackupParseResult.Success).payload.categories.isEmpty())
  }

  @Test
  fun `split bill calculations exclude tax tip and discount and simplify transfers accurately`() {
    val you = Participant(id = "p1", name = "You (Me)")
    val priya = Participant(id = "p2", name = "Priya")
    val arjun = Participant(id = "p3", name = "Arjun")
    val items = listOf(
      ReceiptItem(
        id = "item1",
        title = "Hotel Booking",
        price = 3_000.0,
        quantity = 1,
        claimedParticipantIds = listOf("p1", "p2", "p3"),
        paidByParticipantId = "p1",
        paymentSource = "HDFC Bank",
        paymentAccountId = 1L
      ),
      ReceiptItem(
        id = "item2",
        title = "Fuel",
        price = 600.0,
        quantity = 1,
        claimedParticipantIds = listOf("p1", "p2", "p3"),
        paidByParticipantId = "p2",
        paymentSource = "Cash"
      )
    )

    val breakdowns = DebtSimplifier.calculateBreakdowns(
      mode = SplitMode.ITEMIZED,
      participants = listOf(you, priya, arjun),
      items = items,
      tax = 0.0,
      tip = 0.0,
      discount = 0.0,
      manualBillTotal = 0.0
    )
    assertEquals(3_600.0, breakdowns.sumOf { it.totalToPay }, 0.01)
    assertEquals(3_600.0, breakdowns.sumOf { it.paidAmount }, 0.01)

    val transfers = DebtSimplifier.simplifyDebts(breakdowns)
    // You paid 3000 (share 1200 -> +1800), Priya paid 600 (share 1200 -> -600), Arjun paid 0 (share 1200 -> -1200)
    assertEquals(2, transfers.size)
    assertEquals(1_800.0, transfers.sumOf { it.amount }, 0.01)
    assertTrue(transfers.all { it.toParticipant.id == "p1" })
  }

  @Test
  fun `loan record amortization and active outflow remain consistent across repayments`() {
    val emi = LoanRecord.calculateEmi(500_000.0, 9.0, 60)
    val activeLoan = LoanRecord(
      loanName = "Car Loan",
      principalAmount = 500_000.0,
      annualInterestRate = 9.0,
      tenureMonths = 60,
      emiAmount = emi,
      startDate = "2026-01-01",
      paidEmisCount = 12
    )
    assertFalse(activeLoan.isFullyPaid)
    assertEquals(48, activeLoan.remainingEmisCount)
    assertEquals(emi, activeLoan.activeMonthlyOutflow, 0.001)
    assertTrue(activeLoan.cumulativePrincipalPaid > 0.0)
    assertEquals(
      500_000.0,
      activeLoan.cumulativePrincipalPaid + activeLoan.remainingPrincipal,
      0.01
    )

    val completedLoan = activeLoan.copy(paidEmisCount = 60)
    assertTrue(completedLoan.isFullyPaid)
    assertEquals(0.0, completedLoan.activeMonthlyOutflow, 0.001)
    assertEquals(0.0, completedLoan.remainingPrincipal, 1.0)
  }

  @Test
  fun `subscription due date rolls to next cycle when already paid`() {
    val today = LocalDate.of(2026, 6, 10)
    val paidMillis = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val sub = SubscriptionRecord(
      name = "Fiber Broadband",
      amount = 999.0,
      billingCycle = "MONTHLY",
      dueDayOfMonth = 15,
      lastPaidTimestamp = paidMillis
    )
    assertTrue(SubscriptionDueDateCalculator.isPaidInCurrentCycle(sub, today))
    assertEquals(LocalDate.of(2026, 7, 15), SubscriptionDueDateCalculator.nextDueDate(sub, today))
  }

  @Test
  fun `exported EMI report carries OmniFinCalc branding`() {
    val emiResult = EmiCalculator.calculate(100_000.0, 10.0, 12)
    val emiText = EmiCalculator.exportAmortizationAsText(emiResult)
    assertTrue(emiText.contains("OmniFinCalc"))
    assertFalse(emiText.contains("OmniCalc"))
  }
}
