package com.example.ui.screens.analytics

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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.ExpenseRecord
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.FintechAreaLineChart
import com.example.ui.components.FintechCard
import com.example.ui.components.FintechCategoryDonutBreakdown
import com.example.ui.components.FintechEmptyState
import com.example.ui.components.FintechMetricTile
import com.example.ui.components.FintechSectionHeader
import com.example.ui.components.FintechStatusPill
import com.example.ui.components.IncomeExpenseBarChart
import com.example.ui.theme.OmniFinCalcThemeTokens
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
  viewModel: AnalyticsViewModel,
  onNavigateToAddExpense: () -> Unit,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  val toastMessage by viewModel.toastMessage.collectAsState()
  val context = LocalContext.current
  val semantic = OmniFinCalcThemeTokens.colors
  val snackbarHostState = remember { SnackbarHostState() }

  var showCustomRangeDialog by remember { mutableStateOf(false) }

  LaunchedEffect(toastMessage) {
    toastMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearToast()
    }
  }

  if (showCustomRangeDialog) {
    CustomDateRangeDialog(
      initialStart = state.startDate,
      initialEnd = state.endDate,
      onDismiss = { showCustomRangeDialog = false },
      onApply = { start, end ->
        viewModel.setCustomDateRange(start, end)
        showCustomRangeDialog = false
      }
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .testTag("analytics_screen_list"),
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header + Export actions
      item(key = "analytics_header") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(
              modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
            ) {
              Text(
                text = "Reports & Analytics",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = state.periodRangeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedButton(
                onClick = { viewModel.exportCsv(context) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, semantic.cardBorder),
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("analytics_export_csv_btn")
              ) {
                Icon(Icons.Default.Download, contentDescription = "Export CSV", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("CSV", style = MaterialTheme.typography.labelMedium, maxLines = 1)
              }
              Button(
                onClick = { viewModel.exportPdf(context) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("analytics_export_pdf_btn")
              ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text("PDF", style = MaterialTheme.typography.labelMedium, maxLines = 1)
              }
            }
          }

          // 7 Period Filter Chips
          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            items(AnalyticsPeriod.entries, key = { it.name }) { period ->
              val isSelected = state.selectedPeriod == period
              FilterChip(
                selected = isSelected,
                onClick = {
                  if (period == AnalyticsPeriod.CUSTOM) {
                    showCustomRangeDialog = true
                  } else {
                    viewModel.selectPeriod(period)
                  }
                },
                label = {
                  Text(
                    text = period.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    )
                  )
                },
                shape = RoundedCornerShape(12.dp),
                leadingIcon = if (period == AnalyticsPeriod.CUSTOM) {
                  { Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                  containerColor = MaterialTheme.colorScheme.surface,
                  labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  selectedContainerColor = MaterialTheme.colorScheme.primary,
                  selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                  selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = semantic.cardBorder,
                  selectedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("analytics_period_${period.name.lowercase()}")
              )
            }
          }
        }
      }

      // Period KPI Summary
      item(key = "analytics_kpis") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            FintechMetricTile(
              label = "Period Income",
              value = CurrencyFormatter.formatWhole(state.periodIncome),
              icon = Icons.AutoMirrored.Filled.TrendingUp,
              accentColor = semantic.income,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
            FintechMetricTile(
              label = "Period Expenses",
              value = CurrencyFormatter.formatWhole(state.periodExpense),
              icon = Icons.AutoMirrored.Filled.TrendingDown,
              accentColor = semantic.expense,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
          }
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            FintechMetricTile(
              label = "Net Cash Flow",
              value = CurrencyFormatter.formatWhole(state.netSavings),
              subtitle = String.format(Locale.US, "%.1f%% savings rate", state.savingsRatePercent),
              icon = Icons.Default.Savings,
              accentColor = if (state.netSavings >= 0.0) semantic.savings else semantic.expense,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
            FintechMetricTile(
              label = "Liquid Balance",
              value = CurrencyFormatter.formatWhole(state.totalNetBalance),
              subtitle = "${state.filteredTransactions.size} entries in range",
              icon = Icons.Default.AccountBalanceWallet,
              accentColor = MaterialTheme.colorScheme.primary,
              modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
            )
          }
        }
      }

      // 1. Monthly Income vs Expenses Bar Comparison
      item(key = "monthly_comparison") {
        FintechCard {
          FintechSectionHeader(
            title = "Monthly Income vs Expenses",
            subtitle = "6-month performance ending ${state.endDate.month.name.lowercase().replaceFirstChar { it.uppercase() }}"
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (state.monthlyComparison.all { it.income == 0.0 && it.expense == 0.0 }) {
            Text(
              text = "Log income or expenses to compare monthly performance.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            IncomeExpenseBarChart(points = state.monthlyComparison)
          }
        }
      }

      // 2. Spending Trend
      item(key = "spending_trend") {
        FintechCard {
          FintechSectionHeader(
            title = "Spending Trend",
            subtitle = "Outflow across ${state.selectedPeriod.label.lowercase()}"
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (state.spendingTrend.all { it.value == 0.0 }) {
            Text(
              text = "No expenses recorded in this period.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            FintechAreaLineChart(
              points = state.spendingTrend,
              lineColor = semantic.expense
            )
          }
        }
      }

      // 3. Cash-Flow & Cumulative Savings Trends
      item(key = "cashflow_savings_trends") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          FintechCard {
            FintechSectionHeader(
              title = "Net Cash-Flow Trend",
              subtitle = "Income minus expenses per interval"
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (state.cashFlowTrend.all { it.value == 0.0 }) {
              Text(
                text = "No cash-flow activity recorded in this period.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              FintechAreaLineChart(
                points = state.cashFlowTrend,
                lineColor = MaterialTheme.colorScheme.primary
              )
            }
          }

          FintechCard {
            FintechSectionHeader(
              title = "Cumulative Savings Trajectory",
              subtitle = "Running net surplus over ${state.selectedPeriod.label.lowercase()}"
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (state.savingsTrend.all { it.value == 0.0 }) {
              Text(
                text = "Record income and expenses to plot your savings trajectory.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            } else {
              FintechAreaLineChart(
                points = state.savingsTrend,
                lineColor = semantic.savings
              )
            }
          }
        }
      }

      // 4. Expense Category Breakdown
      item(key = "expense_category_breakdown") {
        FintechCard {
          FintechSectionHeader(
            title = "Expense Category Breakdown",
            subtitle = "${state.expenseCategories.size} categories"
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (state.expenseCategories.isEmpty()) {
            Text(
              text = "No categorized expenses found for this period.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            FintechCategoryDonutBreakdown(
              slices = state.expenseCategories,
              centerLabel = "Total Spent",
              centerValue = CurrencyFormatter.formatCompact(state.periodExpense)
            )
          }
        }
      }

      // 5. Income Category Breakdown
      item(key = "income_category_breakdown") {
        FintechCard {
          FintechSectionHeader(
            title = "Income Sources Breakdown",
            subtitle = "${state.incomeCategories.size} sources"
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (state.incomeCategories.isEmpty()) {
            Text(
              text = "No income entries recorded for this period.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            FintechCategoryDonutBreakdown(
              slices = state.incomeCategories,
              centerLabel = "Total Income",
              centerValue = CurrencyFormatter.formatCompact(state.periodIncome)
            )
          }
        }
      }

      // 6. Account-Wise Analytics
      item(key = "account_analytics") {
        FintechCard {
          FintechSectionHeader(
            title = "Account-Wise Analytics",
            subtitle = "Live balances & period cash movement"
          )
          Spacer(modifier = Modifier.height(12.dp))
          if (state.accountAnalytics.isEmpty()) {
            Text(
              text = "Add a bank account, wallet, or credit card to track per-account flows.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              state.accountAnalytics.forEachIndexed { idx, row ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = row.bankName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                      Text(
                        text = "${row.accountType} • ${row.transactionCount} txns",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Text(
                      text = CurrencyFormatter.format(row.liveBalance),
                      style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                      ),
                      color = if (row.liveBalance >= 0.0) MaterialTheme.colorScheme.onSurface else semantic.expense
                    )
                  }
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                  ) {
                    Text(
                      text = "In: ${CurrencyFormatter.formatWhole(row.periodInflow)}",
                      style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                      color = semantic.income
                    )
                    Text(
                      text = "Out: ${CurrencyFormatter.formatWhole(row.periodOutflow)}",
                      style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                      color = semantic.expense
                    )
                  }
                }
                if (idx < state.accountAnalytics.lastIndex) {
                  HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                }
              }
            }
          }
        }
      }

      // 7. Budget Performance + Debt & Loan Analytics
      item(key = "budget_debt_loan_analytics") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          // Budget Performance
          val budget = state.budgetPerformance
          val budgetColor = when {
            budget.utilizationPercent >= 100.0 -> semantic.expense
            budget.utilizationPercent >= 80.0 -> semantic.warning
            else -> semantic.savings
          }
          FintechCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Monthly Budget Performance",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              FintechStatusPill(
                text = String.format(Locale.US, "%.0f%% used", budget.utilizationPercent),
                textColor = budgetColor,
                containerColor = budgetColor.copy(alpha = 0.14f)
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { (budget.utilizationPercent / 100.0).toFloat().coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = budgetColor,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Spent: ${CurrencyFormatter.formatWhole(budget.currentMonthSpent)}",
                style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (budget.remainingBudget >= 0.0) {
                  "Remaining: ${CurrencyFormatter.formatWhole(budget.remainingBudget)}"
                } else {
                  "Over by: ${CurrencyFormatter.formatWhole(kotlin.math.abs(budget.remainingBudget))}"
                },
                style = MaterialTheme.typography.bodySmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  fontFeatureSettings = "tnum"
                ),
                color = budgetColor
              )
            }
          }

          // Debt Analytics
          val debt = state.debtAnalytics
          FintechCard {
            FintechSectionHeader(
              title = "Debt & Lending Position",
              subtitle = "${debt.activeCount} active • ${debt.settledCount} settled"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Receivable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = CurrencyFormatter.formatWhole(debt.totalReceivable),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                  color = semantic.income
                )
              }
              Column {
                Text("Payable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = CurrencyFormatter.formatWhole(debt.totalPayable),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                  color = semantic.expense
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Net Position", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = CurrencyFormatter.formatWhole(debt.netPosition),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                  color = if (debt.netPosition >= 0.0) semantic.income else semantic.expense
                )
              }
            }
          }

          // Loan Repayment Analytics
          val loan = state.loanAnalytics
          FintechCard {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Loan Repayment Progress",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${loan.activeLoansCount} active • ${loan.completedLoansCount} completed",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              FintechStatusPill(
                text = String.format(Locale.US, "%.0f%% paid", loan.repaymentProgressPercent),
                textColor = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
              progress = { (loan.repaymentProgressPercent / 100.0).toFloat().coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = CurrencyFormatter.formatWhole(loan.totalPrincipalRemaining),
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum")
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text("Monthly EMI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                  text = CurrencyFormatter.formatWhole(loan.totalMonthlyEmiOutflow),
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFeatureSettings = "tnum"),
                  color = semantic.warning
                )
              }
            }
          }
        }
      }

      // 8. Filtered Transaction History
      item(key = "filtered_tx_header") {
        FintechSectionHeader(
          title = "Transactions in Period",
          subtitle = "${state.filteredTransactions.size} matching records"
        )
      }

      if (state.filteredTransactions.isEmpty()) {
        item(key = "filtered_tx_empty") {
          FintechEmptyState(
            icon = Icons.Default.BarChart,
            title = "No transactions in this range",
            description = "Try selecting a wider period or log a new expense or income record.",
            actionLabel = "Add Transaction",
            onAction = onNavigateToAddExpense
          )
        }
      } else {
        items(state.filteredTransactions.take(40), key = { it.id }) { tx ->
          AnalyticsTransactionRow(tx = tx)
        }
      }

      item(key = "analytics_bottom_spacer") {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter)
    )
  }
}

@Composable
private fun AnalyticsTransactionRow(tx: ExpenseRecord) {
  val semantic = OmniFinCalcThemeTokens.colors
  val isIncome = tx.type == "INCOME" || tx.type == "TRANSFER_IN"
  val isTransfer = tx.type == "TRANSFER_IN" || tx.type == "TRANSFER_OUT"
  val badgeColor = when {
    isTransfer -> MaterialTheme.colorScheme.primary
    isIncome -> semantic.income
    else -> semantic.expense
  }
  val dateLabel = remember(tx.dateTimestamp) {
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(tx.dateTimestamp))
  }

  FintechCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(badgeColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = when {
            isTransfer -> Icons.Default.SwapHoriz
            isIncome -> Icons.AutoMirrored.Filled.TrendingUp
            else -> Icons.AutoMirrored.Filled.TrendingDown
          },
          contentDescription = null,
          tint = badgeColor,
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
            append(" • $dateLabel")
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
        color = badgeColor
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomDateRangeDialog(
  initialStart: LocalDate,
  initialEnd: LocalDate,
  onDismiss: () -> Unit,
  onApply: (LocalDate, LocalDate) -> Unit
) {
  var startDate by remember { mutableStateOf(initialStart) }
  var endDate by remember { mutableStateOf(initialEnd) }
  var pickingStart by remember { mutableStateOf(false) }
  var pickingEnd by remember { mutableStateOf(false) }
  val fmt = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

  if (pickingStart) {
    val pickerState = rememberDatePickerState(
      initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
      onDismissRequest = { pickingStart = false },
      confirmButton = {
        TextButton(onClick = {
          pickerState.selectedDateMillis?.let { millis ->
            startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
          }
          pickingStart = false
        }) { Text("Select") }
      },
      dismissButton = {
        TextButton(onClick = { pickingStart = false }) { Text("Cancel") }
      }
    ) {
      DatePicker(state = pickerState)
    }
  }

  if (pickingEnd) {
    val pickerState = rememberDatePickerState(
      initialSelectedDateMillis = endDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    DatePickerDialog(
      onDismissRequest = { pickingEnd = false },
      confirmButton = {
        TextButton(onClick = {
          pickerState.selectedDateMillis?.let { millis ->
            endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
          }
          pickingEnd = false
        }) { Text("Select") }
      },
      dismissButton = {
        TextButton(onClick = { pickingEnd = false }) { Text("Cancel") }
      }
    ) {
      DatePicker(state = pickerState)
    }
  }

  val semantic = OmniFinCalcThemeTokens.colors
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        "Custom Date Range",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Choose start and end dates for your analytics report.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
          onClick = { pickingStart = true },
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, semantic.cardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Start Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                startDate.format(fmt),
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
          }
        }
        Surface(
          onClick = { pickingEnd = true },
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
          border = BorderStroke(1.dp, semantic.cardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("End Date", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Text(
                endDate.format(fmt),
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = MaterialTheme.colorScheme.onSurface
              )
              Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onApply(startDate, endDate) },
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Apply Range")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
    shape = RoundedCornerShape(22.dp)
  )
}
