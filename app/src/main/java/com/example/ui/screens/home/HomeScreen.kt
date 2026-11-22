package com.example.ui.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.local.entity.AccountWithBalance
import com.example.data.local.entity.CreditCardBillReminder
import com.example.data.local.entity.ExpenseRecord
import com.example.domain.calculator.SubscriptionDueDateCalculator
import com.example.ui.components.CreditCardBillReminderDialog
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.FintechCard
import com.example.ui.components.FintechCategoryDonutBreakdown
import com.example.ui.components.FintechEmptyState
import com.example.ui.components.FintechMetricTile
import com.example.ui.components.FintechQuickActionButton
import com.example.ui.components.FintechSectionHeader
import com.example.ui.components.FintechStatusPill
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.screens.analytics.AnalyticsViewModel
import com.example.ui.screens.debt.DebtTrackerViewModel
import com.example.ui.screens.finance.FinanceHubViewModel
import com.example.ui.screens.finance.FinanceTab
import com.example.ui.screens.loan.LoanTrackerViewModel
import com.example.ui.theme.OmniFinCalcThemeTokens
import com.example.ui.theme.performConfiguredHapticFeedback
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private data class UpcomingBillItem(
  val id: String,
  val title: String,
  val subtitle: String,
  val amount: Double,
  val dueDate: LocalDate,
  val daysRemaining: Long,
  val onClick: () -> Unit
)

@Composable
fun HomeScreen(
  financeViewModel: FinanceHubViewModel,
  analyticsViewModel: AnalyticsViewModel,
  loanViewModel: LoanTrackerViewModel,
  debtViewModel: DebtTrackerViewModel,
  isPrivacyMode: Boolean,
  onTogglePrivacyMode: () -> Unit,
  onAddExpenseClick: () -> Unit,
  onAddIncomeClick: () -> Unit,
  onTransferClick: () -> Unit,
  onAddDebtClick: () -> Unit,
  onAddLoanClick: () -> Unit,
  onSplitBillClick: () -> Unit,
  onReportsClick: () -> Unit,
  onNavigateToFinance: (FinanceTab) -> Unit,
  onNavigateToLoans: () -> Unit,
  onNavigateToCalculatorHub: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val semantic = OmniFinCalcThemeTokens.colors

  val totalBalance by financeViewModel.totalNetBalance.collectAsState()
  val totalCreditDue by financeViewModel.totalCreditCardDue.collectAsState()
  val totalIncome by financeViewModel.totalIncome.collectAsState()
  val totalExpense by financeViewModel.totalExpense.collectAsState()
  val monthIncome by financeViewModel.currentMonthIncome.collectAsState()
  val monthExpense by financeViewModel.currentMonthExpenses.collectAsState()
  val monthSavings by financeViewModel.currentMonthSavings.collectAsState()
  val monthlyBudget by financeViewModel.monthlyBudgetLimit.collectAsState()
  val accounts by financeViewModel.accountsWithBalances.collectAsState()
  val expenses by financeViewModel.expenses.collectAsState()
  val reminders by financeViewModel.creditCardBillReminders.collectAsState()
  val subscriptions by financeViewModel.subscriptions.collectAsState()

  val analyticsState by analyticsViewModel.uiState.collectAsState()
  val loanState by loanViewModel.uiState.collectAsState()
  val debtState by debtViewModel.uiState.collectAsState()

  val creditCardAccounts = remember(accounts) {
    accounts.map { it.account }.filter { it.accountType.equals("Credit Card", ignoreCase = true) }
  }
  var reminderDialogTarget by remember { mutableStateOf<CreditCardBillReminder?>(null) }
  var showReminderDialog by remember { mutableStateOf(false) }
  var pendingReminderSave by remember { mutableStateOf<Pair<CreditCardBillReminder, String>?>(null) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    pendingReminderSave?.let { (reminder, accountName) ->
      financeViewModel.saveCreditCardBillReminder(reminder, accountName)
      if (!granted) {
        financeViewModel.showToast(context.getString(R.string.card_bill_notification_permission_denied))
      }
      pendingReminderSave = null
    }
  }

  if (showReminderDialog) {
    CreditCardBillReminderDialog(
      cardAccounts = creditCardAccounts,
      reminders = reminders,
      editingAccountId = reminderDialogTarget?.accountId,
      onDismiss = {
        showReminderDialog = false
        reminderDialogTarget = null
      },
      onSave = { reminder ->
        val accountName = creditCardAccounts.firstOrNull { it.id == reminder.accountId }?.bankName ?: "Credit Card"
        showReminderDialog = false
        reminderDialogTarget = null
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
          ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission && reminder.notificationsEnabled) {
          pendingReminderSave = reminder to accountName
          notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
          financeViewModel.saveCreditCardBillReminder(reminder, accountName)
        }
      }
    )
  }

  // Real upcoming bills aggregated from credit card reminders, recurring subscriptions, and active loan EMIs
  val upcomingBills = remember(reminders, subscriptions, loanState.activeLoans, accounts) {
    val today = LocalDate.now()
    val accountNames = accounts.associate { it.account.id to it.account.bankName }
    val items = mutableListOf<UpcomingBillItem>()

    reminders.filter { it.paidDateEpochDay < 0L || it.paidDateEpochDay < it.dueDateEpochDay }
      .forEach { rem ->
        val due = LocalDate.ofEpochDay(rem.dueDateEpochDay)
        val days = ChronoUnit.DAYS.between(today, due)
        val name = accountNames[rem.accountId] ?: "Credit Card"
        items.add(
          UpcomingBillItem(
            id = "cc_${rem.accountId}",
            title = "$name Bill",
            subtitle = "Credit Card Statement",
            amount = rem.amount,
            dueDate = due,
            daysRemaining = days,
            onClick = { onNavigateToFinance(FinanceTab.CREDIT_CARDS) }
          )
        )
      }

    subscriptions.forEach { sub ->
      val due = SubscriptionDueDateCalculator.nextDueDate(sub, today)
      val days = ChronoUnit.DAYS.between(today, due)
      items.add(
        UpcomingBillItem(
          id = "sub_${sub.id}",
          title = sub.name,
          subtitle = "${sub.billingCycle.lowercase().replaceFirstChar { it.uppercase() }} • ${sub.categoryName}",
          amount = sub.amount,
          dueDate = due,
          daysRemaining = days,
          onClick = { onNavigateToFinance(FinanceTab.SUBSCRIPTIONS) }
        )
      )
    }

    loanState.activeLoans.forEach { loan ->
      val day = loan.dueDayOfMonth.coerceIn(1, today.lengthOfMonth())
      var due = today.withDayOfMonth(day)
      if (due.isBefore(today)) {
        val nextMonth = today.plusMonths(1)
        due = nextMonth.withDayOfMonth(loan.dueDayOfMonth.coerceIn(1, nextMonth.lengthOfMonth()))
      }
      val days = ChronoUnit.DAYS.between(today, due)
      items.add(
        UpcomingBillItem(
          id = "loan_${loan.id}",
          title = "${loan.loanName} EMI",
          subtitle = "Loan • ${loan.remainingEmisCount} EMIs left",
          amount = loan.emiAmount,
          dueDate = due,
          daysRemaining = days,
          onClick = onNavigateToLoans
        )
      )
    }

    items.sortedBy { it.daysRemaining }.take(5)
  }

  val recentTransactions = remember(expenses) {
    expenses.sortedByDescending { it.dateTimestamp }.take(5)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .testTag("dashboard_home_list"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Total Balance Hero Card (Luxury Metallic Banking Card)
    item(key = "hero_balance") {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, semantic.heroAccent.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(
          defaultElevation = if (semantic.isDark) 0.dp else 6.dp
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_balance_hero_card")
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.linearGradient(
                colors = listOf(semantic.heroStart, semantic.heroEnd)
              )
            )
            .background(
              Brush.radialGradient(
                colors = listOf(
                  semantic.heroAccent.copy(alpha = 0.22f),
                  Color.Transparent
                ),
                radius = 520f
              )
            )
            .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(11.dp),
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(semantic.heroAccent.copy(alpha = 0.18f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = semantic.heroAccent,
                    modifier = Modifier.size(19.dp)
                  )
                }
                Column {
                  Text(
                    text = "TOTAL LIQUID BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White.copy(alpha = 0.78f)
                  )
                  Text(
                    text = "${accounts.size} Linked Accounts",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.56f)
                  )
                }
              }
            }

            Text(
              text = CurrencyFormatter.format(totalBalance),
              style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFeatureSettings = "tnum"
              ),
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (totalCreditDue > 0.0) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = semantic.expense.copy(alpha = 0.28f),
                    border = BorderStroke(1.dp, semantic.expense.copy(alpha = 0.45f))
                  ) {
                    Text(
                      text = "Card Due: ${CurrencyFormatter.formatWhole(totalCreditDue)}",
                      style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum"
                      ),
                      color = Color.White,
                      maxLines = 1,
                      softWrap = false,
                      overflow = TextOverflow.Ellipsis,
                      modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                  }
                } else {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.11f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
                  ) {
                    Text(
                      text = "All cards settled",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                      color = Color.White.copy(alpha = 0.88f),
                      maxLines = 1,
                      softWrap = false,
                      modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                onClick = onReportsClick,
                shape = RoundedCornerShape(11.dp),
                color = semantic.heroCtaContainer,
                modifier = Modifier.testTag("dashboard_hero_analytics_btn")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                  Icon(
                    Icons.Default.BarChart,
                    contentDescription = null,
                    tint = semantic.heroCtaContent,
                    modifier = Modifier.size(15.dp)
                  )
                  Text(
                    text = "Analytics",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = semantic.heroCtaContent,
                    maxLines = 1,
                    softWrap = false
                  )
                }
              }
            }
          }
        }
      }
    }

    // 2-4. Lifetime Income / Expenses / Savings KPI Row
    item(key = "kpi_grid") {
      val savingsRate = if (monthIncome > 0.0) ((monthSavings / monthIncome) * 100.0).coerceIn(-100.0, 100.0) else 0.0
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FintechMetricTile(
            label = "Total Income",
            value = CurrencyFormatter.formatWhole(totalIncome),
            subtitle = "Month: ${CurrencyFormatter.formatWhole(monthIncome)}",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = semantic.income,
            onClick = { onNavigateToFinance(FinanceTab.EXPENSES) },
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          )
          FintechMetricTile(
            label = "Total Expenses",
            value = CurrencyFormatter.formatWhole(totalExpense),
            subtitle = "Month: ${CurrencyFormatter.formatWhole(monthExpense)}",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            accentColor = semantic.expense,
            onClick = { onNavigateToFinance(FinanceTab.EXPENSES) },
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          )
        }
        FintechMetricTile(
          label = "Monthly Savings",
          value = CurrencyFormatter.formatWhole(monthSavings),
          subtitle = if (monthIncome > 0.0) {
            String.format(Locale.US, "%.1f%% of monthly income saved", savingsRate)
          } else {
            "Record income to track your monthly savings rate"
          },
          icon = Icons.Default.Savings,
          accentColor = if (monthSavings >= 0.0) semantic.savings else semantic.expense,
          onClick = onReportsClick,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // 13. Quick Action Buttons (All 7 required actions)
    item(key = "quick_actions") {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FintechSectionHeader(
          title = "Quick Actions",
          actionLabel = "All Tools",
          onActionClick = onNavigateToCalculatorHub
        )
        FintechCard(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_log_expense),
                icon = Icons.Default.RemoveCircleOutline,
                accentColor = semantic.expense,
                onClick = onAddExpenseClick,
                testTag = "quick_action_add_expense",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_add_income),
                icon = Icons.Default.AddCircleOutline,
                accentColor = semantic.income,
                onClick = onAddIncomeClick,
                testTag = "quick_action_add_income",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_transfer),
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = onTransferClick,
                testTag = "quick_action_transfer",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_add_debt),
                icon = Icons.Default.Handshake,
                accentColor = semantic.warning,
                onClick = onAddDebtClick,
                testTag = "quick_action_add_debt",
                modifier = Modifier.weight(1f)
              )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_add_loan),
                icon = Icons.Default.AccountBalance,
                accentColor = Color(0xFF0EA5E9),
                onClick = onAddLoanClick,
                testTag = "quick_action_add_loan",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_split_bill),
                icon = Icons.Default.Group,
                accentColor = semantic.savings,
                onClick = onSplitBillClick,
                testTag = "quick_action_split_bill",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = stringResource(R.string.home_quick_reports),
                icon = Icons.Default.BarChart,
                accentColor = MaterialTheme.colorScheme.primary,
                onClick = onReportsClick,
                testTag = "quick_action_reports",
                modifier = Modifier.weight(1f)
              )
              FintechQuickActionButton(
                label = "Calculators",
                icon = Icons.Default.Calculate,
                accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                onClick = onNavigateToCalculatorHub,
                testTag = "quick_action_calculators",
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    // 7. Account Balances
    item(key = "account_balances") {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FintechSectionHeader(
          title = "Account Balances",
          subtitle = "${accounts.size} linked accounts",
          actionLabel = "Manage",
          onActionClick = { onNavigateToFinance(FinanceTab.EXPENSES) }
        )
        if (accounts.isEmpty()) {
          FintechEmptyState(
            icon = Icons.Default.AccountBalanceWallet,
            title = "No accounts added yet",
            description = "Add your bank accounts, cash wallets, or credit cards to track live balances.",
            actionLabel = "Add Account",
            onAction = {
              onNavigateToFinance(FinanceTab.EXPENSES)
              financeViewModel.setShowAddBank(true)
            }
          )
        } else {
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(accounts, key = { it.account.id }) { item ->
              DashboardAccountCard(
                accountWithBalance = item,
                onClick = {
                  financeViewModel.setSelectedAccountFilter(item.account.id)
                  onNavigateToFinance(
                    if (item.account.accountType.equals("Credit Card", ignoreCase = true)) {
                      FinanceTab.CREDIT_CARDS
                    } else {
                      FinanceTab.EXPENSES
                    }
                  )
                }
              )
            }
          }
        }
      }
    }

    // 9. Monthly Budget Progress
    item(key = "monthly_budget_progress") {
      val limit = monthlyBudget.coerceAtLeast(1.0)
      val ratio = (monthExpense / limit).toFloat().coerceIn(0f, 1f)
      val percent = (monthExpense / limit) * 100.0
      val remaining = limit - monthExpense
      val statusColor = when {
        percent >= 100.0 -> semantic.expense
        percent >= 80.0 -> semantic.warning
        else -> semantic.savings
      }
      FintechCard(
        onClick = {
          onNavigateToFinance(FinanceTab.EXPENSES)
          financeViewModel.setShowSetBudget(true)
        }
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Monthly Budget Progress",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Target: ${CurrencyFormatter.formatWhole(limit)}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          FintechStatusPill(
            text = String.format(Locale.US, "%.0f%%", percent),
            textColor = statusColor,
            containerColor = statusColor.copy(alpha = 0.14f)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
          progress = { ratio },
          modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
          color = statusColor,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Spent ${CurrencyFormatter.formatWhole(monthExpense)}",
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = if (remaining >= 0.0) {
              "${CurrencyFormatter.formatWhole(remaining)} left"
            } else {
              "Over by ${CurrencyFormatter.formatWhole(abs(remaining))}"
            },
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.SemiBold,
              fontFeatureSettings = "tnum"
            ),
            color = statusColor
          )
        }
      }
    }

    // 5. Income vs Expenses Chart
    item(key = "income_vs_expenses_chart") {
      FintechCard {
        FintechSectionHeader(
          title = "Income vs Expenses",
          subtitle = "Last 6 months cash flow",
          actionLabel = "Full Report",
          onActionClick = onReportsClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (analyticsState.monthlyComparison.all { it.income == 0.0 && it.expense == 0.0 }) {
          Text(
            text = "Add income and expenses to visualize your monthly cash flow.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          IncomeExpenseBarChart(points = analyticsState.monthlyComparison)
        }
      }
    }

    // 6. Expense Category Breakdown
    item(key = "expense_category_breakdown") {
      FintechCard {
        FintechSectionHeader(
          title = "Expense Category Breakdown",
          subtitle = analyticsState.selectedPeriod.label,
          actionLabel = "Details",
          onActionClick = onReportsClick
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (analyticsState.expenseCategories.isEmpty()) {
          Text(
            text = "No categorized expenses yet. Tap Add Expense to log your first transaction.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          FintechCategoryDonutBreakdown(
            slices = analyticsState.expenseCategories,
            centerLabel = "Spent",
            centerValue = CurrencyFormatter.formatCompact(analyticsState.periodExpense)
          )
        }
      }
    }

    // 10. Upcoming Bills
    item(key = "upcoming_bills") {
      FintechCard {
        FintechSectionHeader(
          title = "Upcoming Bills & Due Dates",
          subtitle = "Cards, subscriptions & EMIs",
          actionLabel = if (creditCardAccounts.isNotEmpty()) "+ Card Alert" else "Bills",
          onActionClick = {
            if (creditCardAccounts.isNotEmpty()) {
              reminderDialogTarget = null
              showReminderDialog = true
            } else {
              onNavigateToFinance(FinanceTab.SUBSCRIPTIONS)
            }
          }
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (upcomingBills.isEmpty()) {
          Text(
            text = "No upcoming bills scheduled. Add recurring subscriptions, credit card reminders, or active loans to see alerts here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          val dateFmt = remember { DateTimeFormatter.ofPattern("dd MMM") }
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            upcomingBills.forEachIndexed { idx, bill ->
              val pillColor = when {
                bill.daysRemaining < 0 -> semantic.expense
                bill.daysRemaining <= 3 -> semantic.warning
                else -> MaterialTheme.colorScheme.primary
              }
              val dueLabel = when {
                bill.daysRemaining < 0 -> "Overdue"
                bill.daysRemaining == 0L -> "Due today"
                bill.daysRemaining == 1L -> "Tomorrow"
                else -> "In ${bill.daysRemaining}d • ${bill.dueDate.format(dateFmt)}"
              }
              Surface(
                onClick = bill.onClick,
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(pillColor.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = pillColor, modifier = Modifier.size(18.dp))
                  }
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = bill.title,
                      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = bill.subtitle,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = CurrencyFormatter.formatWhole(bill.amount),
                      style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                      )
                    )
                    Text(
                      text = dueLabel,
                      style = MaterialTheme.typography.labelSmall,
                      color = pillColor,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
              if (idx < upcomingBills.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
              }
            }
          }
        }
      }
    }

    // 11 & 12. Debt Overview & Loan Overview
    item(key = "debt_and_loan_overview") {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Debt Overview
        FintechCard(onClick = { onNavigateToFinance(FinanceTab.DEBTS) }) {
          FintechSectionHeader(
            title = "Debt Overview",
            subtitle = "${debtState.activeLentDebts.size + debtState.activeBorrowedDebts.size} active entries",
            actionLabel = "Open",
            onActionClick = { onNavigateToFinance(FinanceTab.DEBTS) }
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("To Receive", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = CurrencyFormatter.formatWhole(debtState.totalLentReceivable),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = semantic.income
              )
            }
            Column {
              Text("To Pay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = CurrencyFormatter.formatWhole(debtState.totalBorrowedPayable),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = semantic.expense
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Net Debt", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = CurrencyFormatter.formatWhole(debtState.netBalance),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = if (debtState.netBalance >= 0.0) semantic.income else semantic.expense
              )
            }
          }
        }

        // Loan Overview
        FintechCard(onClick = onNavigateToLoans) {
          FintechSectionHeader(
            title = "Loan Overview",
            subtitle = "${loanState.totalActiveLoansCount} active loans • ${loanState.completedLoans.size} paid off",
            actionLabel = "Manage",
            onActionClick = onNavigateToLoans
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Remaining Principal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = CurrencyFormatter.formatWhole(loanState.totalPrincipalRemaining),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Monthly EMI Outflow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text(
                text = CurrencyFormatter.formatWhole(loanState.totalMonthlyEmiOutflow),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                color = semantic.warning
              )
            }
          }
        }
      }
    }

    // 8. Recent Transactions
    item(key = "recent_transactions") {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FintechSectionHeader(
          title = "Recent Transactions",
          subtitle = "Latest activity across accounts",
          actionLabel = "See All",
          onActionClick = { onNavigateToFinance(FinanceTab.EXPENSES) }
        )
        if (recentTransactions.isEmpty()) {
          FintechEmptyState(
            icon = Icons.AutoMirrored.Filled.ReceiptLong,
            title = "No transactions yet",
            description = "Record an expense or income entry to start building your OmniFinCalc ledger.",
            actionLabel = "Add Expense",
            onAction = onAddExpenseClick
          )
        } else {
          FintechCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) {
            Column {
              recentTransactions.forEachIndexed { idx, tx ->
                DashboardTransactionItem(tx = tx)
                if (idx < recentTransactions.lastIndex) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                }
              }
            }
          }
        }
      }
    }

    item(key = "home_bottom_spacer") {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
private fun DashboardAccountCard(
  accountWithBalance: AccountWithBalance,
  onClick: () -> Unit
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val acc = accountWithBalance.account
  val isCard = acc.accountType.equals("Credit Card", ignoreCase = true)
  val icon = when {
    isCard -> Icons.Default.CreditCard
    acc.accountType.equals("Cash", ignoreCase = true) -> Icons.Default.Payments
    else -> Icons.Default.AccountBalance
  }
  val accent = if (isCard) semantic.warning else MaterialTheme.colorScheme.primary

  FintechCard(
    onClick = onClick,
    contentPadding = PaddingValues(14.dp),
    modifier = Modifier.width(178.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(30.dp)
          .clip(RoundedCornerShape(9.dp))
          .background(accent.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
      }
      Text(
        text = acc.accountType,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    Spacer(modifier = Modifier.height(10.dp))
    Text(
      text = acc.bankName,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = if (isCard && accountWithBalance.currentBalance < 0.0) {
        "Due ${CurrencyFormatter.formatWhole(abs(accountWithBalance.currentBalance))}"
      } else {
        CurrencyFormatter.format(accountWithBalance.currentBalance)
      },
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.ExtraBold,
        fontFeatureSettings = "tnum"
      ),
      color = if (accountWithBalance.currentBalance < 0.0) semantic.expense else MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun DashboardTransactionItem(tx: ExpenseRecord) {
  val semantic = OmniFinCalcThemeTokens.colors
  val isIncome = tx.type == "INCOME" || tx.type == "TRANSFER_IN"
  val isTransfer = tx.type == "TRANSFER_IN" || tx.type == "TRANSFER_OUT"
  val tint = when {
    isTransfer -> MaterialTheme.colorScheme.primary
    isIncome -> semantic.income
    else -> semantic.expense
  }
  val dateStr = remember(tx.dateTimestamp) {
    SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(tx.dateTimestamp))
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    Box(
      modifier = Modifier
        .size(36.dp)
        .clip(CircleShape)
        .background(tint.copy(alpha = 0.14f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = when {
          isTransfer -> Icons.Default.SwapHoriz
          isIncome -> Icons.AutoMirrored.Filled.TrendingUp
          else -> Icons.AutoMirrored.Filled.TrendingDown
        },
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(18.dp)
      )
    }
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = tx.title,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = buildString {
          append(tx.categoryName)
          if (tx.bankAccountName.isNotBlank()) append(" • ${tx.bankAccountName}")
          append(" • $dateStr")
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
    Text(
      text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(tx.amount)}",
      style = MaterialTheme.typography.titleSmall.copy(
        fontWeight = FontWeight.Bold,
        fontFeatureSettings = "tnum"
      ),
      color = tint
    )
  }
}
