package com.example.ui.screens.loan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.AccountWithBalance
import com.example.data.local.entity.BankAccount
import com.example.data.local.entity.LoanRecord
import com.example.domain.calculator.LoanPrepaymentCalculator
import com.example.ui.components.CurrencyFormatter
import com.example.ui.components.NumberInputField
import com.example.ui.theme.OmniFinCalcThemeTokens
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val LOAN_DATE_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

private fun formatLoanDate(timestamp: Long): String =
  Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).format(LOAN_DATE_FORMATTER)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanTrackerScreen(
  viewModel: LoanTrackerViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  val userMessage by viewModel.userMessage.collectAsState()
  val paymentHistory by viewModel.paymentHistory.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  var loanPendingEmiPayment by remember { mutableStateOf<LoanRecord?>(null) }

  LaunchedEffect(userMessage) {
    userMessage?.let { msg ->
      if (msg.startsWith("Paid EMI #")) {
        val result = snackbarHostState.showSnackbar(
          message = msg,
          actionLabel = "UNDO",
          duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
          viewModel.undoLastPaidEmi()
        }
      } else {
        snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
      }
      viewModel.clearUserMessage()
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.setShowAddLoanDialog(true) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
        modifier = Modifier.testTag("btn_add_new_loan")
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add New Loan")
      }
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // 1. Hero Dashboard Card
      item(key = "loan_dashboard", contentType = "loan_dashboard") {
        LoanDashboardCard(
          activeLoansCount = state.totalActiveLoansCount,
          totalRemainingPrincipal = state.totalPrincipalRemaining,
          monthlyOutflow = state.totalMonthlyEmiOutflow,
          totalPaid = state.totalPrincipalPaid
        )
      }

      // 2. Category Filter Chips
      item(key = "loan_filters", contentType = "loan_filters") {
        LoanCategoryFilterRow(
          selectedCategory = state.selectedCategoryFilter,
          onSelectCategory = { viewModel.setCategoryFilter(it) }
        )
      }

      // 3. Loans Header
      item(key = "loan_header", contentType = "loan_header") {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (state.selectedCategoryFilter != null) "${state.selectedCategoryFilter} Loans" else "Active & Tracked Loans",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${state.loans.size} ${if (state.loans.size == 1) "Loan" else "Loans"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // 4. Empty State or Loans List
      if (state.loans.isEmpty()) {
        item(key = "loan_empty", contentType = "loan_empty") {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "No Loans Tracked Yet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Tap + to track your Home Loan, Car EMI, or Gadget financing.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              FilledTonalButton(onClick = { viewModel.setShowAddLoanDialog(true) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Your First Loan")
              }
            }
          }
        }
      } else {
        items(state.loans, key = { it.id }, contentType = { "loan_item" }) { loan ->
          LoanItemCard(
            loan = loan,
            onClick = { viewModel.selectLoanForDetail(loan) },
            onPayEmi = { loanPendingEmiPayment = loan },
            onUndoEmi = {
              viewModel.undoLastPaidEmi(loan.id)
            }
          )
        }
      }
    }
  }

  // Add Loan Dialog
  if (state.showAddLoanDialog) {
    AddLoanDialog(
      bankAccounts = state.bankAccounts,
      onDismiss = { viewModel.setShowAddLoanDialog(false) },
      onConfirm = { name, category, principal, rate, tenure, customEmi, startDate, paidCount, notes, bankId, bankName, dueDayOfMonth ->
        viewModel.addLoan(
          name = name,
          category = category,
          principal = principal,
          annualRate = rate,
          tenureMonths = tenure,
          customEmi = customEmi,
          startDate = startDate,
          paidEmis = paidCount,
          notes = notes,
          bankAccountId = bankId,
          bankAccountName = bankName,
          dueDayOfMonth = dueDayOfMonth
        )
      }
    )
  }

  loanPendingEmiPayment?.let { loan ->
    AlertDialog(
      onDismissRequest = { loanPendingEmiPayment = null },
      containerColor = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(22.dp),
      title = { Text("Select Account for EMI", fontWeight = FontWeight.Bold) },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text("Choose where to deduct ${CurrencyFormatter.format(loan.emiAmount)}:")
          if (state.bankAccounts.isEmpty()) {
            Text(
              "No accounts are available. You can still record this EMI without an account.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
              onClick = {
                viewModel.payMonthlyEmi(loan, null)
                loanPendingEmiPayment = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Pay without an account", maxLines = 1, softWrap = false)
            }
          } else {
            state.bankAccounts.forEach { account ->
              OutlinedButton(
                onClick = {
                  viewModel.payMonthlyEmi(loan, account.account.id)
                  loanPendingEmiPayment = null
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalAlignment = Alignment.Start
                ) {
                  Text(
                    account.account.bankName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    "Balance: ${CurrencyFormatter.format(account.currentBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { loanPendingEmiPayment = null }) { Text("Cancel") }
      }
    )
  }

  var editingLoan by remember { mutableStateOf<LoanRecord?>(null) }

  // Loan Detail Modal
  state.selectedLoanDetail?.let { loan ->
    LoanDetailDialog(
      loan = loan,
      bankAccounts = state.bankAccounts,
      paymentHistory = paymentHistory,
      onDismiss = { viewModel.selectLoanForDetail(null) },
      onEdit = {
        val target = loan
        viewModel.selectLoanForDetail(null)
        editingLoan = target
      },
      onPayEmi = { bankId, _ ->
        viewModel.payMonthlyEmi(loan, bankId)
      },
      onUndoEmi = {
        viewModel.undoLastPaidEmi(loan.id)
      },
      onDelete = {
        viewModel.deleteLoan(loan)
      }
    )
  }

  // Edit Loan Dialog
  editingLoan?.let { loan ->
    EditLoanDialog(
      loan = loan,
      bankAccounts = state.bankAccounts,
      onDismiss = { editingLoan = null },
      onConfirm = { updated ->
        viewModel.updateLoan(updated)
        editingLoan = null
      }
    )
  }
}

// -------------------------------------------------------------
// DASHBOARD HERO CARD
// -------------------------------------------------------------
@Composable
private fun LoanDashboardCard(
  activeLoansCount: Int,
  totalRemainingPrincipal: Double,
  monthlyOutflow: Double,
  totalPaid: Double
) {
  val semantic = OmniFinCalcThemeTokens.colors
  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.linearGradient(
            colors = listOf(semantic.heroStart, semantic.heroEnd)
          )
        )
        .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.AccountBalance,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(17.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "TOTAL PRINCIPAL REMAINING",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
              ),
              color = Color.White.copy(alpha = 0.72f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          Surface(
            shape = RoundedCornerShape(999.dp),
            color = Color.White.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
          ) {
            Text(
              text = "$activeLoansCount Active",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              color = Color.White,
              maxLines = 1,
              softWrap = false
            )
          }
        }

        Text(
          text = CurrencyFormatter.format(totalRemainingPrincipal),
          style = MaterialTheme.typography.displaySmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontFeatureSettings = "tnum"
          ),
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.09f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            Column(modifier = Modifier.padding(11.dp)) {
              Text(
                text = "Monthly EMI Outflow",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = CurrencyFormatter.format(monthlyOutflow),
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = Color(0xFFF17B83),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White.copy(alpha = 0.09f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            Column(modifier = Modifier.padding(11.dp)) {
              Text(
                text = "Principal Paid",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = CurrencyFormatter.format(totalPaid),
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum"
                ),
                color = Color(0xFF35C99A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// CATEGORY FILTER ROW
// -------------------------------------------------------------
@Composable
private fun LoanCategoryFilterRow(
  selectedCategory: String?,
  onSelectCategory: (String?) -> Unit
) {
  val categories = listOf(
    null to "All",
    "HOME" to "Home",
    "CAR" to "Car / Auto",
    "PERSONAL" to "Personal",
    "MOBILE" to "Mobile / Gadget",
    "EDUCATION" to "Education",
    "OTHER" to "Other"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    categories.forEach { (catKey, label) ->
      val isSelected = selectedCategory == catKey
      FilterChip(
        selected = isSelected,
        onClick = { onSelectCategory(catKey) },
        label = { Text(label, fontSize = 12.sp) },
        leadingIcon = if (catKey != null) {
          {
            Icon(
              getLoanCategoryIcon(catKey),
              contentDescription = null,
              modifier = Modifier.size(14.dp)
            )
          }
        } else null,
        shape = RoundedCornerShape(12.dp)
      )
    }
  }
}

// -------------------------------------------------------------
// ENHANCED LOAN ITEM CARD
// -------------------------------------------------------------
@Composable
private fun LoanItemCard(
  loan: LoanRecord,
  onClick: () -> Unit,
  onPayEmi: () -> Unit,
  onUndoEmi: () -> Unit
) {
  val progressFraction = remember(loan.paidEmisCount, loan.tenureMonths) { loan.progressFraction }
  val cumulativePrincipalPaid = remember(loan.principalAmount, loan.annualInterestRate, loan.tenureMonths, loan.paidEmisCount, loan.emiAmount) {
    loan.cumulativePrincipalPaid
  }
  val remainingPrincipal = remember(loan.principalAmount, cumulativePrincipalPaid) {
    maxOf(0.0, loan.principalAmount - cumulativePrincipalPaid)
  }

  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("loan_card_${loan.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top Row: Category Icon, Title, Status & Interest Rate Chips, and EMI Amount
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          modifier = Modifier.weight(1f).padding(end = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .background(getLoanCategoryColor(loan.loanCategory).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              getLoanCategoryIcon(loan.loanCategory),
              contentDescription = null,
              tint = getLoanCategoryColor(loan.loanCategory),
              modifier = Modifier.size(22.dp)
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = loan.loanName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Status Badge
              Box(
                modifier = Modifier.background(
                  if (loan.isFullyPaid) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                  RoundedCornerShape(6.dp)
                )
              ) {
                Text(
                  text = if (loan.isFullyPaid) "Closed" else "Active • ${loan.remainingEmisCount} left",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (loan.isFullyPaid) Color(0xFF059669) else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  maxLines = 1,
                  softWrap = false
                )
              }

              val pillBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              // Interest Rate Chip
              Box(
                modifier = Modifier.background(pillBg, RoundedCornerShape(6.dp))
              ) {
                Text(
                  text = "${loan.annualInterestRate}% p.a.",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  maxLines = 1,
                  softWrap = false
                )
              }

              Box(
                modifier = Modifier.background(pillBg, RoundedCornerShape(6.dp))
              ) {
                Text(
                  text = "Due day ${loan.dueDayOfMonth}",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                  maxLines = 1,
                  softWrap = false
                )
              }

              // Linked Account Chip
              if (loan.bankAccountName.isNotBlank()) {
                Box(
                  modifier = Modifier.background(pillBg, RoundedCornerShape(6.dp))
                ) {
                  Text(
                    text = loan.bankAccountName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }

        // Monthly EMI Amount Badge
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyFormatter.format(loan.emiAmount),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            softWrap = false
          )
          Text(
            text = "/month",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Exact Breakdown of EMIs Paid vs Pending
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.DoneAll,
            contentDescription = null,
            tint = if (loan.isFullyPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${loan.paidEmisCount}/${loan.tenureMonths} EMIs Paid",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Box(
          modifier = Modifier.background(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            RoundedCornerShape(8.dp)
          )
        ) {
          Text(
            text = "${(progressFraction * 100).toInt()}% Paid • ${loan.remainingEmisCount} EMIs Pending",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (loan.isFullyPaid) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Progress Bar representation
      LinearProgressIndicator(
        progress = { progressFraction },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = if (loan.isFullyPaid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Dual Principal Reporting Cards: Principal Paid vs. Remaining Principal
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Principal Paid Card (Emerald)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF10B981).copy(alpha = 0.10f),
          border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Principal Paid",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF047857)
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = CurrencyFormatter.format(cumulativePrincipalPaid),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF059669)
            )
          }
        }

        // Remaining Principal Card (Rose/Coral)
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = if (loan.isFullyPaid) Color(0xFF10B981).copy(alpha = 0.08f) else Color(0xFFEF4444).copy(alpha = 0.08f),
          border = BorderStroke(1.dp, if (loan.isFullyPaid) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFEF4444).copy(alpha = 0.2f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                if (loan.isFullyPaid) Icons.Default.Check else Icons.Default.Pending,
                contentDescription = null,
                tint = if (loan.isFullyPaid) Color(0xFF059669) else Color(0xFFDC2626),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Remaining Principal",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Medium,
                color = if (loan.isFullyPaid) Color(0xFF047857) else Color(0xFFB91C1C)
              )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
              text = CurrencyFormatter.format(remainingPrincipal),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = if (loan.isFullyPaid) Color(0xFF059669) else Color(0xFFDC2626)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Action row: "Undo EMI" & "Pay Monthly EMI"
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (loan.paidEmisCount > 0) {
          OutlinedButton(
            onClick = onUndoEmi,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              "Undo EMI (#${loan.paidEmisCount})",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              softWrap = false
            )
          }
        } else {
          Text(
            text = "Tenure: ${loan.tenureMonths} Mos (${loan.startDate})",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
          )
        }

        if (!loan.isFullyPaid) {
          FilledTonalButton(
            onClick = onPayEmi,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
          ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Pay Monthly EMI", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
          }
        } else {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF10B981).copy(alpha = 0.15f)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Loan Fully Closed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
            }
          }
        }
      }
    }
  }
}

// -------------------------------------------------------------
// LOAN DETAIL DIALOG / MODAL
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoanDetailDialog(
  loan: LoanRecord,
  bankAccounts: List<AccountWithBalance>,
  paymentHistory: List<com.example.data.local.entity.ExpenseRecord>,
  onDismiss: () -> Unit,
  onEdit: () -> Unit,
  onPayEmi: (Long?, String) -> Unit,
  onUndoEmi: () -> Unit,
  onDelete: () -> Unit
) {
  var selectedBankId by remember { mutableStateOf(loan.bankAccountId) }
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val selectedAccount = bankAccounts.find { it.account.id == selectedBankId }

  val progressFraction = if (loan.tenureMonths > 0) {
    (loan.paidEmisCount.toFloat() / loan.tenureMonths.toFloat()).coerceIn(0f, 1f)
  } else 0f
  val progressPct = (progressFraction * 100f).toInt()

  val scheduleRows = remember(loan.principalAmount, loan.annualInterestRate, loan.tenureMonths, loan.emiAmount) {
    val rows = ArrayList<Triple<Int, Pair<Double, Double>, Double>>(loan.tenureMonths)
    var outstanding = loan.principalAmount
    val monthlyRate = loan.annualInterestRate / 1200.0
    for (month in 1..loan.tenureMonths) {
      val interestPart = outstanding * monthlyRate
      val principalPart = minOf(outstanding, maxOf(0.0, loan.emiAmount - interestPart))
      outstanding = maxOf(0.0, outstanding - principalPart)
      rows.add(Triple(month, principalPart to interestPart, outstanding))
    }
    rows
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = MaterialTheme.colorScheme.background,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
        .testTag("loan_detail_dialog")
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Distinct Header Bar
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 0.dp
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = loan.loanName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${loan.loanCategory} • ${loan.paidEmisCount}/${loan.tenureMonths} EMIs Paid",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Loan", tint = MaterialTheme.colorScheme.primary)
              }
              IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Loan", tint = MaterialTheme.colorScheme.error)
              }
              IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Scrollable Boxed Content
        Column(
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // 1. Repayment Progress Box
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Repayment Progress",
                  style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                  Text(
                    text = "$progressPct% Complete",
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }
              LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
              )
            }
          }

          // 2. Loan Metrics — Clean 2-Column Separated Boxes
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DetailItem("Original Principal", CurrencyFormatter.format(loan.principalAmount), Modifier.weight(1f))
              DetailItem("Interest Rate", "${loan.annualInterestRate}% p.a.", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DetailItem("Monthly EMI", CurrencyFormatter.format(loan.emiAmount), Modifier.weight(1f), highlight = true)
              DetailItem("Tenure", "${loan.tenureMonths} Months", Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DetailItem("Total Interest Payable", CurrencyFormatter.format(loan.totalInterestPayable), Modifier.weight(1f))
              DetailItem("Total Repayment", CurrencyFormatter.format(loan.totalRepayment), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DetailItem("Principal Paid", CurrencyFormatter.format(loan.cumulativePrincipalPaid), Modifier.weight(1f))
              DetailItem("Principal Pending", CurrencyFormatter.format(loan.remainingPrincipal), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              DetailItem("Total EMI Paid", CurrencyFormatter.format(loan.totalPaidAmount), Modifier.weight(1f))
              DetailItem("EMIs Remaining", "${loan.remainingEmisCount}", Modifier.weight(1f))
            }
          }

          // 3. Payment History Card
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Payment History",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "${paymentHistory.size} recorded",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (paymentHistory.isEmpty()) {
                Text(
                  text = "No EMI payment transactions recorded yet.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              } else {
                paymentHistory.sortedByDescending { it.dateTimestamp }.forEachIndexed { idx, payment ->
                  if (idx > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                  }
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = payment.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                      )
                      val formattedPaymentDate = remember(payment.dateTimestamp) { formatLoanDate(payment.dateTimestamp) }
                      Text(
                        text = "$formattedPaymentDate • ${payment.bankAccountName.ifBlank { "No account" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Text(
                      text = CurrencyFormatter.format(payment.amount),
                      style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }

          // 4. Month-wise Principal & Interest Schedule Card
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Text(
                text = "Month-wise Principal & Interest",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Spacer(modifier = Modifier.height(10.dp))
              // Tinted Table Header
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text("EMI", Modifier.weight(0.55f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text("Principal", Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text("Interest", Modifier.weight(0.95f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                  Text("Balance", Modifier.weight(1.1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.End)
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              scheduleRows.forEachIndexed { idx, (month, parts, outstanding) ->
                val isPaidMonth = month <= loan.paidEmisCount
                val rowBg = when {
                  isPaidMonth -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                  idx % 2 == 1 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)
                  else -> Color.Transparent
                }
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(rowBg)
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "#$month",
                    modifier = Modifier.weight(0.55f),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = if (isPaidMonth) FontWeight.Bold else FontWeight.Medium,
                      fontFeatureSettings = "tnum"
                    ),
                    color = if (isPaidMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = CurrencyFormatter.format(parts.first),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = CurrencyFormatter.format(parts.second),
                    modifier = Modifier.weight(0.95f),
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = CurrencyFormatter.format(outstanding),
                    modifier = Modifier.weight(1.1f),
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.SemiBold,
                      fontFeatureSettings = "tnum"
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            }
          }

          // 5. Prepayment Simulator Box (already renders its own styled Card)
          if (!loan.isFullyPaid) {
            LoanPrepaymentSimulatorSection(loan = loan)
          }

          // 6. Payment Account & Notes Box
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "Payment & Bank Account",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              if (bankAccounts.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                  expanded = expanded,
                  onExpandedChange = { expanded = it }
                ) {
                  OutlinedTextField(
                    value = selectedAccount?.let { "${it.account.bankName} (${CurrencyFormatter.format(it.currentBalance)})" } ?: "Select payment account",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Deduct EMI from") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .menuAnchor()
                  )
                  ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                  ) {
                    bankAccounts.forEach { acc ->
                      DropdownMenuItem(
                        text = { Text("${acc.account.bankName} - Balance: ${CurrencyFormatter.format(acc.currentBalance)}") },
                        onClick = {
                          selectedBankId = acc.account.id
                          expanded = false
                        }
                      )
                    }
                  }
                }
              } else {
                Text(
                  text = "No bank accounts linked. EMI will be recorded without deducting from a bank account.",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (loan.notes.isNotBlank()) {
                Text(
                  text = "Notes: ${loan.notes}",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        // Sticky Footer Bar
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Surface(
          color = MaterialTheme.colorScheme.surface,
          tonalElevation = 0.dp
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (loan.paidEmisCount > 0) {
                OutlinedButton(
                  onClick = {
                    onUndoEmi()
                    onDismiss()
                  },
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                  modifier = Modifier.weight(0.9f)
                ) {
                  Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Undo #${loan.paidEmisCount}", fontSize = 12.sp, maxLines = 1, softWrap = false)
                }
              }
              if (!loan.isFullyPaid) {
                Button(
                  onClick = {
                    onPayEmi(selectedBankId, selectedAccount?.account?.bankName ?: "")
                    onDismiss()
                  },
                  shape = RoundedCornerShape(12.dp),
                  contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                  modifier = Modifier.weight(1.3f)
                ) {
                  Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Pay Monthly EMI", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }
              }
            }
          }
        }
      }
    }
  }

  if (showDeleteConfirm) {
    AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete Loan?") },
      text = { Text("Are you sure you want to delete '${loan.loanName}'? This will remove its tracker history.") },
      confirmButton = {
        Button(
          onClick = {
            showDeleteConfirm = false
            onDelete()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

// -------------------------------------------------------------
// EDIT LOAN DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditLoanDialog(
  loan: LoanRecord,
  bankAccounts: List<AccountWithBalance>,
  onDismiss: () -> Unit,
  onConfirm: (LoanRecord) -> Unit
) {
  var loanName by rememberSaveable { mutableStateOf(loan.loanName) }
  var loanCategory by rememberSaveable { mutableStateOf(loan.loanCategory) }
  var principalText by rememberSaveable { mutableStateOf(loan.principalAmount.toString()) }
  var interestRateText by rememberSaveable { mutableStateOf(loan.annualInterestRate.toString()) }
  var tenureMonthsText by rememberSaveable { mutableStateOf(loan.tenureMonths.toString()) }
  var customEmiText by rememberSaveable { mutableStateOf(loan.emiAmount.toString()) }
  var dueDayOfMonthText by rememberSaveable { mutableStateOf(loan.dueDayOfMonth.toString()) }
  var notes by rememberSaveable { mutableStateOf(loan.notes) }
  var selectedBankId by rememberSaveable { mutableStateOf(loan.bankAccountId) }

  val principalVal = principalText.toDoubleOrNull() ?: 0.0
  val rateVal = interestRateText.toDoubleOrNull() ?: 0.0
  val tenureVal = tenureMonthsText.toIntOrNull() ?: 0
  val dueDayOfMonth = dueDayOfMonthText.toIntOrNull()?.takeIf { it in 1..31 } ?: loan.dueDayOfMonth

  val calculatedEmi = remember(principalVal, rateVal, tenureVal) {
    LoanRecord.calculateEmi(principalVal, rateVal, tenureVal)
  }

  val selectedAccount = bankAccounts.find { it.account.id == selectedBankId }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Loan", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = loanName,
          onValueChange = { loanName = it },
          label = { Text("Loan Name *") },
          modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("HOME", "CAR", "PERSONAL", "MOBILE", "EDUCATION", "OTHER").forEach { cat ->
            FilterChip(
              selected = loanCategory.equals(cat, ignoreCase = true),
              onClick = { loanCategory = cat },
              label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }

        OutlinedTextField(
          value = principalText,
          onValueChange = { principalText = it },
          label = { Text("Principal Amount (₹) *") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = interestRateText,
            onValueChange = { interestRateText = it },
            label = { Text("Interest Rate (% p.a.) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = tenureMonthsText,
            onValueChange = { tenureMonthsText = it },
            label = { Text("Tenure (Months) *") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = customEmiText,
          onValueChange = { customEmiText = it },
          label = { Text("Monthly EMI (₹)") },
          placeholder = { Text("Default: ₹${calculatedEmi.toLong()}") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.fillMaxWidth()
        )

        // Optional Linked Bank Account
        if (bankAccounts.isNotEmpty()) {
          var expanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
          ) {
            OutlinedTextField(
              value = selectedAccount?.let { "${it.account.bankName} (${CurrencyFormatter.format(it.currentBalance)})" } ?: "Select Linked Account (Optional)",
              onValueChange = {},
              readOnly = true,
              label = { Text("Deduct EMI from Account") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false }
            ) {
              DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                  selectedBankId = null
                  expanded = false
                }
              )
              bankAccounts.forEach { acc ->
                DropdownMenuItem(
                  text = { Text("${acc.account.bankName} (${CurrencyFormatter.format(acc.currentBalance)})") },
                  onClick = {
                    selectedBankId = acc.account.id
                    expanded = false
                  }
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = dueDayOfMonthText,
          onValueChange = {
            if (it.length <= 2 && it.all { char -> char.isDigit() }) dueDayOfMonthText = it
          },
          label = { Text("EMI Due Date (Day of Month, 1-31)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (Optional)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      val parsedCustomEmi = customEmiText.toDoubleOrNull()?.takeIf { it > 0 } ?: calculatedEmi
      val canSave = loanName.isNotBlank() && principalVal > 0 && rateVal > 0 && tenureVal > 0
      Button(
        onClick = {
          if (canSave) {
            onConfirm(
              loan.copy(
                loanName = loanName.trim(),
                loanCategory = loanCategory,
                principalAmount = principalVal,
                annualInterestRate = rateVal,
                tenureMonths = tenureVal,
                emiAmount = parsedCustomEmi,
                dueDayOfMonth = dueDayOfMonth,
                bankAccountId = selectedBankId,
                bankAccountName = selectedAccount?.account?.bankName ?: (if (selectedBankId == null) "" else loan.bankAccountName),
                notes = notes.trim()
              )
            )
          }
        },
        enabled = canSave
      ) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun DetailItem(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  highlight: Boolean = false
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (highlight) {
      MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    } else {
      MaterialTheme.colorScheme.surface
    },
    border = BorderStroke(
      width = 1.dp,
      color = if (highlight) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
      } else {
        MaterialTheme.colorScheme.outlineVariant
      }
    ),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = value,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
        color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

// -------------------------------------------------------------
// ADD LOAN DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLoanDialog(
  bankAccounts: List<AccountWithBalance>,
  onDismiss: () -> Unit,
  onConfirm: (
    name: String,
    category: String,
    principal: Double,
    rate: Double,
    tenure: Int,
    customEmi: Double?,
    startDate: String,
    paidCount: Int,
    notes: String,
    bankId: Long?,
    bankName: String,
    dueDayOfMonth: Int
  ) -> Unit
) {
  var loanName by rememberSaveable { mutableStateOf("") }
  var loanCategory by rememberSaveable { mutableStateOf("PERSONAL") }
  var principalText by rememberSaveable { mutableStateOf("") }
  var interestRateText by rememberSaveable { mutableStateOf("10.5") }
  var tenureMonthsText by rememberSaveable { mutableStateOf("36") }
  var initialPaidEmisText by rememberSaveable { mutableStateOf("0") }
  var dueDayOfMonthText by rememberSaveable { mutableStateOf("1") }
  var notes by rememberSaveable { mutableStateOf("") }
  var selectedBankId by rememberSaveable { mutableStateOf<Long?>(bankAccounts.firstOrNull()?.account?.id) }

  val principalVal = principalText.toDoubleOrNull() ?: 0.0
  val rateVal = interestRateText.toDoubleOrNull() ?: 0.0
  val tenureVal = tenureMonthsText.toIntOrNull() ?: 0
  val dueDayOfMonth = dueDayOfMonthText.toIntOrNull()?.takeIf { it in 1..31 }

  val calculatedEmi = remember(principalVal, rateVal, tenureVal) {
    LoanRecord.calculateEmi(principalVal, rateVal, tenureVal)
  }

  val selectedAccount = bankAccounts.find { it.account.id == selectedBankId }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Loan or EMI Tracker", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp)
          .verticalScroll(rememberScrollState())
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = loanName,
          onValueChange = { loanName = it },
          label = { Text("Loan Name (e.g. HDFC Home Loan, Car EMI)") },
          modifier = Modifier.fillMaxWidth()
        )

        // Category Selection
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("HOME", "CAR", "PERSONAL", "MOBILE", "EDUCATION", "OTHER").forEach { cat ->
            FilterChip(
              selected = loanCategory == cat,
              onClick = { loanCategory = cat },
              label = { Text(cat.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
              shape = RoundedCornerShape(10.dp)
            )
          }
        }

        OutlinedTextField(
          value = principalText,
          onValueChange = { principalText = it },
          label = { Text("Principal Amount (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = interestRateText,
            onValueChange = { interestRateText = it },
            label = { Text("Interest Rate (% p.a.)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = tenureMonthsText,
            onValueChange = { tenureMonthsText = it },
            label = { Text("Tenure (Months)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
          )
        }

        // Live Calculated EMI Preview Card
        if (calculatedEmi > 0) {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Calculated Monthly EMI:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
              Text(
                CurrencyFormatter.format(calculatedEmi),
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        // Optional Linked Bank Account
        if (bankAccounts.isNotEmpty()) {
          var expanded by remember { mutableStateOf(false) }
          ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
          ) {
            OutlinedTextField(
              value = selectedAccount?.let { "${it.account.bankName} (${CurrencyFormatter.format(it.currentBalance)})" } ?: "Select Linked Account (Optional)",
              onValueChange = {},
              readOnly = true,
              label = { Text("Deduct EMI from Account") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
            )
            ExposedDropdownMenu(
              expanded = expanded,
              onDismissRequest = { expanded = false }
            ) {
              bankAccounts.forEach { acc ->
                DropdownMenuItem(
                  text = { Text("${acc.account.bankName} (${CurrencyFormatter.format(acc.currentBalance)})") },
                  onClick = {
                    selectedBankId = acc.account.id
                    expanded = false
                  }
                )
              }
            }
          }
        }

        OutlinedTextField(
          value = initialPaidEmisText,
          onValueChange = { initialPaidEmisText = it },
          label = { Text("Already Paid EMIs count (Default 0)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
          value = dueDayOfMonthText,
          onValueChange = {
            if (it.length <= 2 && it.all { char -> char.isDigit() }) dueDayOfMonthText = it
          },
          label = { Text("EMI Due Date (Day of Month, 1-31)") },
          placeholder = { Text("e.g. 5 or 10") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          isError = dueDayOfMonth == null,
          supportingText = if (dueDayOfMonth == null) {
            { Text("Enter a day from 1 to 31") }
          } else {
            null
          }
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Notes (Account No, Lender name, etc.)") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (loanName.isNotBlank() && principalVal > 0 && tenureVal > 0) {
            onConfirm(
              loanName,
              loanCategory,
              principalVal,
              rateVal,
              tenureVal,
              calculatedEmi,
              LocalDate.now().format(LOAN_DATE_FORMATTER),
              initialPaidEmisText.toIntOrNull() ?: 0,
              notes,
              selectedBankId,
              selectedAccount?.account?.bankName ?: "",
              dueDayOfMonth ?: return@Button
            )
          }
        },
        enabled = loanName.isNotBlank() && principalVal > 0 && tenureVal > 0 && dueDayOfMonth != null
      ) {
        Text("Save Loan")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

private fun getLoanCategoryIcon(category: String): ImageVector {
  return when (category.uppercase()) {
    "HOME" -> Icons.Default.Home
    "CAR" -> Icons.Default.DirectionsCar
    "MOBILE" -> Icons.Default.Smartphone
    "EDUCATION" -> Icons.Default.School
    "PERSONAL" -> Icons.Default.Person
    else -> Icons.Default.AccountBalance
  }
}

private fun getLoanCategoryColor(category: String): Color {
  return when (category.uppercase()) {
    "HOME" -> Color(0xFF3478F6)
    "CAR" -> Color(0xFFE7A33E)
    "MOBILE" -> Color(0xFF0EA5E9)
    "EDUCATION" -> Color(0xFF14B8A6)
    "PERSONAL" -> Color(0xFF16A879)
    else -> Color(0xFF3478F6)
  }
}

@Composable
private fun LoanPrepaymentSimulatorSection(loan: LoanRecord) {
  var prepayText by remember { mutableStateOf("50000") }
  val prepayAmount = prepayText.toDoubleOrNull() ?: 0.0
  val remainingMonths = (loan.tenureMonths - loan.paidEmisCount).coerceAtLeast(1)
  val currentPrincipal = loan.remainingPrincipal
  val result = remember(loan, prepayAmount) {
    LoanPrepaymentCalculator.calculate(
      remainingPrincipal = currentPrincipal,
      annualInterestRate = loan.annualInterestRate,
      monthlyEmi = loan.emiAmount,
      remainingMonths = remainingMonths,
      extraPrepayment = prepayAmount
    )
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Speed,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            "Prepayment & Foreclosure Simulator",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            "Simulate prepaying extra lumpsum towards principal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Quick chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(
          25000.0 to "+ ₹25,000",
          50000.0 to "+ ₹50,000",
          100000.0 to "+ ₹1 Lakh",
          200000.0 to "+ ₹2 Lakhs"
        ).forEach { (amt, label) ->
          AssistChip(
            onClick = { prepayText = "%.0f".format(amt) },
            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      OutlinedTextField(
        value = prepayText,
        onValueChange = { prepayText = it },
        label = { Text("Extra Prepayment Amount (₹)") },
        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Impact Result Cards
      if (!result.isValid) {
        Text(
          "Unable to estimate savings because the current EMI does not pay off the remaining balance within the tracked tenure.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error
        )
      } else if (result.isForeclosed) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFF10B981).copy(alpha = 0.18f),
          border = BorderStroke(1.dp, Color(0xFF10B981)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                "🎉 Loan Fully Foreclosed & Closed!",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                fontSize = 13.sp
              )
              Text(
                "You become debt-free immediately and save ${CurrencyFormatter.format(result.interestSaved)} in estimated interest!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      } else {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Tenure Reduced",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (result.monthsSaved > 0) "${result.monthsSaved} Months" else "0 Months",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum",
                  color = if (result.monthsSaved > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "New tenure: ${result.monthsAfter} mos",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            Column(
              modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Interest Saved",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = CurrencyFormatter.format(result.interestSaved),
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  fontFeatureSettings = "tnum",
                  color = if (result.interestSaved > 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "Pure direct savings",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
          "Estimate assumes the same EMI and excludes lender prepayment fees or rate changes.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}
