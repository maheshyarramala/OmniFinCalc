package com.example.ui.screens.analytics

import android.app.Application
import android.content.Context
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountWithBalance
import com.example.data.local.entity.DebtRecord
import com.example.data.local.entity.ExpenseCategory
import com.example.data.local.entity.ExpenseRecord
import com.example.data.local.entity.LoanRecord
import com.example.data.repository.FinanceRepository
import com.example.domain.finance.MoneyMath
import com.example.domain.report.CsvReportGenerator
import com.example.domain.report.PdfReportGenerator
import com.example.ui.components.BarComparisonPoint
import com.example.ui.components.CategoryBreakdownSlice
import com.example.ui.components.TrendSeriesPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class AnalyticsPeriod(val label: String) {
  THIS_WEEK("This week"),
  THIS_MONTH("This month"),
  LAST_MONTH("Last month"),
  LAST_3_MONTHS("Last 3 months"),
  LAST_6_MONTHS("Last 6 months"),
  THIS_YEAR("This year"),
  CUSTOM("Custom range")
}

data class AccountAnalyticsRow(
  val accountId: Long,
  val bankName: String,
  val accountType: String,
  val liveBalance: Double,
  val periodInflow: Double,
  val periodOutflow: Double,
  val transactionCount: Int
)

data class BudgetPerformanceSummary(
  val monthlyBudgetLimit: Double = 50_000.0,
  val currentMonthSpent: Double = 0.0,
  val remainingBudget: Double = 50_000.0,
  val utilizationPercent: Double = 0.0
)

data class DebtAnalyticsSummary(
  val totalReceivable: Double = 0.0,
  val totalPayable: Double = 0.0,
  val netPosition: Double = 0.0,
  val activeCount: Int = 0,
  val settledCount: Int = 0
)

data class LoanAnalyticsSummary(
  val activeLoansCount: Int = 0,
  val completedLoansCount: Int = 0,
  val totalPrincipalPaid: Double = 0.0,
  val totalPrincipalRemaining: Double = 0.0,
  val totalMonthlyEmiOutflow: Double = 0.0,
  val repaymentProgressPercent: Double = 0.0
)

data class AnalyticsUiState(
  val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.THIS_MONTH,
  val startDate: LocalDate = LocalDate.now().withDayOfMonth(1),
  val endDate: LocalDate = LocalDate.now(),
  val periodRangeText: String = "",
  val totalNetBalance: Double = 0.0,
  val periodIncome: Double = 0.0,
  val periodExpense: Double = 0.0,
  val netSavings: Double = 0.0,
  val savingsRatePercent: Double = 0.0,
  val monthlyComparison: List<BarComparisonPoint> = emptyList(),
  val spendingTrend: List<TrendSeriesPoint> = emptyList(),
  val cashFlowTrend: List<TrendSeriesPoint> = emptyList(),
  val savingsTrend: List<TrendSeriesPoint> = emptyList(),
  val expenseCategories: List<CategoryBreakdownSlice> = emptyList(),
  val incomeCategories: List<CategoryBreakdownSlice> = emptyList(),
  val accountAnalytics: List<AccountAnalyticsRow> = emptyList(),
  val budgetPerformance: BudgetPerformanceSummary = BudgetPerformanceSummary(),
  val debtAnalytics: DebtAnalyticsSummary = DebtAnalyticsSummary(),
  val loanAnalytics: LoanAnalyticsSummary = LoanAnalyticsSummary(),
  val filteredTransactions: List<ExpenseRecord> = emptyList()
)

object AnalyticsDateRangeCalculator {
  fun resolveRange(
    period: AnalyticsPeriod,
    customStart: LocalDate,
    customEnd: LocalDate,
    today: LocalDate = LocalDate.now()
  ): Pair<LocalDate, LocalDate> {
    return when (period) {
      AnalyticsPeriod.THIS_WEEK -> {
        val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        start to today
      }
      AnalyticsPeriod.THIS_MONTH -> {
        today.withDayOfMonth(1) to today
      }
      AnalyticsPeriod.LAST_MONTH -> {
        val lastMonth = YearMonth.from(today).minusMonths(1)
        lastMonth.atDay(1) to lastMonth.atEndOfMonth()
      }
      AnalyticsPeriod.LAST_3_MONTHS -> {
        val startMonth = YearMonth.from(today).minusMonths(2)
        startMonth.atDay(1) to today
      }
      AnalyticsPeriod.LAST_6_MONTHS -> {
        val startMonth = YearMonth.from(today).minusMonths(5)
        startMonth.atDay(1) to today
      }
      AnalyticsPeriod.THIS_YEAR -> {
        LocalDate.of(today.year, 1, 1) to today
      }
      AnalyticsPeriod.CUSTOM -> {
        if (customStart.isAfter(customEnd)) customEnd to customStart else customStart to customEnd
      }
    }
  }

  fun toEpochBounds(
    startDate: LocalDate,
    endDate: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault()
  ): Pair<Long, Long> {
    val startMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
    val endExclusiveMillis = endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    return startMillis to endExclusiveMillis
  }
}

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = FinanceRepository.getInstance(application)
  private val financeDao = AppDatabase.getDatabase(application).financeDao()
  private val prefs = application.getSharedPreferences("omni_calc_finance_prefs", Context.MODE_PRIVATE)

  private val _selectedPeriod = MutableStateFlow(AnalyticsPeriod.THIS_MONTH)
  private val _customStartDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1))
  private val _customEndDate = MutableStateFlow(LocalDate.now())
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private val _budgetLimit = MutableStateFlow(readBudgetLimit())
  private val prefListener =
    android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
      if (key == "monthly_budget_limit_exact" || key == "monthly_budget_limit") {
        _budgetLimit.value = readBudgetLimit()
      }
    }.also(prefs::registerOnSharedPreferenceChangeListener)

  private fun readBudgetLimit(): Double {
    return prefs.getString("monthly_budget_limit_exact", null)?.toDoubleOrNull()
      ?: prefs.getFloat("monthly_budget_limit", 50_000f).toDouble()
  }

  private data class PeriodParams(
    val period: AnalyticsPeriod,
    val customStart: LocalDate,
    val customEnd: LocalDate,
    val budgetLimit: Double
  )

  private data class DomainSnapshot(
    val expenses: List<ExpenseRecord>,
    val accounts: List<AccountWithBalance>,
    val categories: List<ExpenseCategory>,
    val loans: List<LoanRecord>,
    val debts: List<DebtRecord>
  )

  private val periodParamsFlow = combine(
    _selectedPeriod,
    _customStartDate,
    _customEndDate,
    _budgetLimit
  ) { period, start, end, budget ->
    PeriodParams(period, start, end, budget)
  }

  private val domainSnapshotFlow = combine(
    repository.allExpenses,
    repository.accountsWithBalances,
    financeDao.getAllCategories(),
    repository.allLoans,
    repository.allDebts
  ) { expenses, accounts, categories, loans, debts ->
    DomainSnapshot(expenses, accounts, categories, loans, debts)
  }

  val uiState: StateFlow<AnalyticsUiState> = combine(
    domainSnapshotFlow,
    repository.totalNetBalance,
    repository.currentMonthExpenses,
    periodParamsFlow
  ) { snapshot, totalNetBalance, currentMonthSpent, params ->
    buildAnalyticsState(snapshot, totalNetBalance, currentMonthSpent, params)
  }.flowOn(Dispatchers.Default).stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = AnalyticsUiState()
  )

  fun selectPeriod(period: AnalyticsPeriod) {
    _selectedPeriod.value = period
  }

  fun setCustomDateRange(start: LocalDate, end: LocalDate) {
    val (normalizedStart, normalizedEnd) = if (start.isAfter(end)) end to start else start to end
    _customStartDate.value = normalizedStart
    _customEndDate.value = normalizedEnd
    _selectedPeriod.value = AnalyticsPeriod.CUSTOM
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  fun exportCsv(context: Context) {
    viewModelScope.launch {
      try {
        val state = uiState.value
        val accounts = repository.accountsWithBalances.value
        val file = withContext(Dispatchers.IO) {
          CsvReportGenerator.generateFullFinanceReportCsv(context, accounts, state.filteredTransactions)
        }
        CsvReportGenerator.shareCsv(
          context,
          file,
          "OmniFinCalc Analytics (${state.selectedPeriod.label})"
        )
      } catch (e: Exception) {
        _toastMessage.value = "Could not export CSV: ${e.localizedMessage ?: "Unknown error"}"
      }
    }
  }

  fun exportPdf(context: Context) {
    viewModelScope.launch {
      try {
        val state = uiState.value
        val file = withContext(Dispatchers.IO) {
          PdfReportGenerator.generateFinancialAnalyticsReport(
            context = context,
            periodLabel = "${state.selectedPeriod.label} • ${state.periodRangeText}",
            totalBalance = state.totalNetBalance,
            periodIncome = state.periodIncome,
            periodExpense = state.periodExpense,
            netSavings = state.netSavings,
            categoryTotals = state.expenseCategories.map { it.name to it.amount },
            transactions = state.filteredTransactions
          )
        }
        PdfReportGenerator.sharePdf(context, file)
      } catch (e: Exception) {
        _toastMessage.value = "Could not export PDF: ${e.localizedMessage ?: "Unknown error"}"
      }
    }
  }

  private fun buildAnalyticsState(
    snapshot: DomainSnapshot,
    totalNetBalance: Double,
    currentMonthSpent: Double,
    params: PeriodParams
  ): AnalyticsUiState {
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val (startDate, endDate) = AnalyticsDateRangeCalculator.resolveRange(
      params.period,
      params.customStart,
      params.customEnd,
      today
    )
    val (startMillis, endExclusiveMillis) = AnalyticsDateRangeCalculator.toEpochBounds(startDate, endDate, zone)
    val rangeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val rangeText = "${startDate.format(rangeFormatter)} – ${endDate.format(rangeFormatter)}"

    val periodTransactions = snapshot.expenses
      .filter { it.dateTimestamp in startMillis until endExclusiveMillis }
      .sortedByDescending { it.dateTimestamp }

    val periodIncome = MoneyMath.sumOfExact(periodTransactions) {
      if (it.type == "INCOME") it.amount else 0.0
    }
    val periodExpense = MoneyMath.sumOfExact(periodTransactions) {
      if (it.type == "EXPENSE") it.amount else 0.0
    }
    val netSavings = MoneyMath.subtract(periodIncome, periodExpense)
    val savingsRate = if (periodIncome > 0.0) {
      ((netSavings / periodIncome) * 100.0).coerceIn(-100.0, 100.0)
    } else {
      0.0
    }

    val colorMap = snapshot.categories.associate { it.name to parseColorHex(it.colorHex) }
    val expenseCategories = buildCategoryBreakdown(
      transactions = periodTransactions.filter { it.type == "EXPENSE" },
      colorMap = colorMap,
      fallbackPalette = EXPENSE_PALETTE
    )
    val incomeCategories = buildCategoryBreakdown(
      transactions = periodTransactions.filter { it.type == "INCOME" },
      colorMap = colorMap,
      fallbackPalette = INCOME_PALETTE
    )

    val monthlyComparison = buildMonthlyComparison(snapshot.expenses, endDate, zone)
    val (spendingTrend, cashFlowTrend, savingsTrend) = buildTrends(
      periodTransactions = periodTransactions,
      startDate = startDate,
      endDate = endDate,
      zone = zone
    )

    val accountAnalytics = snapshot.accounts.map { accWithBal ->
      val accTx = periodTransactions.filter { it.bankAccountId == accWithBal.account.id }
      val inflow = MoneyMath.sumOfExact(accTx) {
        if (it.type == "INCOME" || it.type == "TRANSFER_IN") it.amount else 0.0
      }
      val outflow = MoneyMath.sumOfExact(accTx) {
        if (it.type == "EXPENSE" || it.type == "TRANSFER_OUT") it.amount else 0.0
      }
      AccountAnalyticsRow(
        accountId = accWithBal.account.id,
        bankName = accWithBal.account.bankName,
        accountType = accWithBal.account.accountType,
        liveBalance = accWithBal.currentBalance,
        periodInflow = inflow,
        periodOutflow = outflow,
        transactionCount = accTx.size
      )
    }

    val budgetLimit = params.budgetLimit.coerceAtLeast(1.0)
    val budgetPerformance = BudgetPerformanceSummary(
      monthlyBudgetLimit = budgetLimit,
      currentMonthSpent = currentMonthSpent,
      remainingBudget = MoneyMath.subtract(budgetLimit, currentMonthSpent),
      utilizationPercent = ((currentMonthSpent / budgetLimit) * 100.0).coerceAtLeast(0.0)
    )

    val activeLent = snapshot.debts.filter { it.type == "LENT" && !it.isSettled }
    val activeBorrowed = snapshot.debts.filter { it.type == "BORROWED" && !it.isSettled }
    val settledDebts = snapshot.debts.filter { it.isSettled }
    val receivable = MoneyMath.sumOfExact(activeLent) { it.amount }
    val payable = MoneyMath.sumOfExact(activeBorrowed) { it.amount }
    val debtAnalytics = DebtAnalyticsSummary(
      totalReceivable = receivable,
      totalPayable = payable,
      netPosition = MoneyMath.subtract(receivable, payable),
      activeCount = activeLent.size + activeBorrowed.size,
      settledCount = settledDebts.size
    )

    val activeLoans = snapshot.loans.filter { !it.isFullyPaid }
    val completedLoans = snapshot.loans.filter { it.isFullyPaid }
    val totalPrincipalPaid = MoneyMath.sumOfExact(snapshot.loans) { it.cumulativePrincipalPaid }
    val totalPrincipalRemaining = MoneyMath.sumOfExact(activeLoans) { it.remainingPrincipal }
    val totalMonthlyEmi = MoneyMath.sumOfExact(activeLoans) { it.activeMonthlyOutflow }
    val totalLoanPrincipal = MoneyMath.sumOfExact(snapshot.loans) { it.principalAmount }
    val loanProgress = if (totalLoanPrincipal > 0.0) {
      ((totalPrincipalPaid / totalLoanPrincipal) * 100.0).coerceIn(0.0, 100.0)
    } else {
      0.0
    }
    val loanAnalytics = LoanAnalyticsSummary(
      activeLoansCount = activeLoans.size,
      completedLoansCount = completedLoans.size,
      totalPrincipalPaid = totalPrincipalPaid,
      totalPrincipalRemaining = totalPrincipalRemaining,
      totalMonthlyEmiOutflow = totalMonthlyEmi,
      repaymentProgressPercent = loanProgress
    )

    return AnalyticsUiState(
      selectedPeriod = params.period,
      startDate = startDate,
      endDate = endDate,
      periodRangeText = rangeText,
      totalNetBalance = totalNetBalance,
      periodIncome = periodIncome,
      periodExpense = periodExpense,
      netSavings = netSavings,
      savingsRatePercent = savingsRate,
      monthlyComparison = monthlyComparison,
      spendingTrend = spendingTrend,
      cashFlowTrend = cashFlowTrend,
      savingsTrend = savingsTrend,
      expenseCategories = expenseCategories,
      incomeCategories = incomeCategories,
      accountAnalytics = accountAnalytics,
      budgetPerformance = budgetPerformance,
      debtAnalytics = debtAnalytics,
      loanAnalytics = loanAnalytics,
      filteredTransactions = periodTransactions
    )
  }

  private fun buildCategoryBreakdown(
    transactions: List<ExpenseRecord>,
    colorMap: Map<String, Color>,
    fallbackPalette: List<Color>
  ): List<CategoryBreakdownSlice> {
    val total = MoneyMath.sumOfExact(transactions) { it.amount }
    if (total <= 0.0) return emptyList()
    return transactions
      .groupBy { it.categoryName.ifBlank { "Other" } }
      .entries
      .map { (name, list) -> name to MoneyMath.sumOfExact(list) { it.amount } }
      .sortedByDescending { it.second }
      .mapIndexed { index, (name, amount) ->
        CategoryBreakdownSlice(
          name = name,
          amount = amount,
          percentage = (amount / total) * 100.0,
          color = colorMap[name] ?: fallbackPalette[index % fallbackPalette.size]
        )
      }
  }

  private fun buildMonthlyComparison(
    allExpenses: List<ExpenseRecord>,
    anchorEndDate: LocalDate,
    zone: ZoneId
  ): List<BarComparisonPoint> {
    val monthFormatter = DateTimeFormatter.ofPattern("MMM")
    val anchorMonth = YearMonth.from(anchorEndDate)
    val months = (5 downTo 0).map { anchorMonth.minusMonths(it.toLong()) }
    return months.map { ym ->
      val startMs = ym.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
      val endMs = ym.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
      var incMinor = 0L
      var expMinor = 0L
      for (tx in allExpenses) {
        if (tx.dateTimestamp in startMs until endMs) {
          when (tx.type) {
            "INCOME" -> incMinor += MoneyMath.toMinorUnits(tx.amount)
            "EXPENSE" -> expMinor += MoneyMath.toMinorUnits(tx.amount)
          }
        }
      }
      BarComparisonPoint(
        label = ym.format(monthFormatter),
        income = MoneyMath.fromMinorUnits(incMinor),
        expense = MoneyMath.fromMinorUnits(expMinor)
      )
    }
  }

  private fun buildTrends(
    periodTransactions: List<ExpenseRecord>,
    startDate: LocalDate,
    endDate: LocalDate,
    zone: ZoneId
  ): Triple<List<TrendSeriesPoint>, List<TrendSeriesPoint>, List<TrendSeriesPoint>> {
    val spanDays = (ChronoUnit.DAYS.between(startDate, endDate) + 1).coerceAtLeast(1L)
    val dayFmt = DateTimeFormatter.ofPattern("d MMM")
    val monthFmt = DateTimeFormatter.ofPattern("MMM yy")

    val buckets: List<Triple<String, LocalDate, LocalDate>> = when {
      spanDays <= 14 -> {
        (0 until spanDays.toInt()).map { offset ->
          val d = startDate.plusDays(offset.toLong())
          Triple(d.format(dayFmt), d, d)
        }
      }
      spanDays <= 62 -> {
        val list = mutableListOf<Triple<String, LocalDate, LocalDate>>()
        var cursor = startDate
        while (!cursor.isAfter(endDate)) {
          val bucketEnd = minOf(cursor.plusDays(6), endDate)
          list.add(Triple(cursor.format(dayFmt), cursor, bucketEnd))
          cursor = bucketEnd.plusDays(1)
        }
        list
      }
      else -> {
        val list = mutableListOf<Triple<String, LocalDate, LocalDate>>()
        var ym = YearMonth.from(startDate)
        val endYm = YearMonth.from(endDate)
        while (!ym.isAfter(endYm)) {
          val bStart = maxOf(ym.atDay(1), startDate)
          val bEnd = minOf(ym.atEndOfMonth(), endDate)
          list.add(Triple(ym.format(monthFmt), bStart, bEnd))
          ym = ym.plusMonths(1)
        }
        list
      }
    }

    val spendingPoints = mutableListOf<TrendSeriesPoint>()
    val cashFlowPoints = mutableListOf<TrendSeriesPoint>()
    val savingsPoints = mutableListOf<TrendSeriesPoint>()
    var cumulativeSavingsMinor = 0L

    buckets.forEach { (label, bStart, bEnd) ->
      val (bStartMs, bEndMs) = AnalyticsDateRangeCalculator.toEpochBounds(bStart, bEnd, zone)
      var incMinor = 0L
      var expMinor = 0L
      for (tx in periodTransactions) {
        if (tx.dateTimestamp in bStartMs until bEndMs) {
          when (tx.type) {
            "INCOME" -> incMinor += MoneyMath.toMinorUnits(tx.amount)
            "EXPENSE" -> expMinor += MoneyMath.toMinorUnits(tx.amount)
          }
        }
      }
      val netMinor = incMinor - expMinor
      cumulativeSavingsMinor += netMinor
      spendingPoints.add(TrendSeriesPoint(label, MoneyMath.fromMinorUnits(expMinor)))
      cashFlowPoints.add(TrendSeriesPoint(label, MoneyMath.fromMinorUnits(netMinor)))
      savingsPoints.add(TrendSeriesPoint(label, MoneyMath.fromMinorUnits(cumulativeSavingsMinor)))
    }

    return Triple(spendingPoints, cashFlowPoints, savingsPoints)
  }

  private fun parseColorHex(hex: String): Color {
    val parsed = runCatching { Color(AndroidColor.parseColor(hex)) }
      .getOrDefault(Color(0xFF3478F6))
    val perceivedLuminance = (0.2126f * parsed.red) + (0.7152f * parsed.green) + (0.0722f * parsed.blue)
    return if (perceivedLuminance < 0.12f) Color(0xFF06B6D4) else parsed
  }

  override fun onCleared() {
    prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
    super.onCleared()
  }

  companion object {
    private val EXPENSE_PALETTE = listOf(
      Color(0xFFEF4444),
      Color(0xFF3478F6),
      Color(0xFFF59E0B),
      Color(0xFF8B5CF6),
      Color(0xFF0D9488),
      Color(0xFFEC4899)
    )
    private val INCOME_PALETTE = listOf(
      Color(0xFF10B981),
      Color(0xFF0D9488),
      Color(0xFF3478F6),
      Color(0xFF6366F1)
    )
  }
}
