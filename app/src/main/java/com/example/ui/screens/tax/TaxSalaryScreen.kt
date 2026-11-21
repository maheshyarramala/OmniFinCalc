package com.example.ui.screens.tax

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculator.IncomeTaxCalculator
import com.example.domain.calculator.SalaryCalculator
import com.example.ui.components.*
import com.example.ui.theme.ChartInterest
import com.example.ui.theme.ChartPrincipal
import com.example.ui.theme.ChartWealthGain
import com.example.ui.theme.OmniFinCalcThemeTokens
import com.example.ui.theme.performConfiguredHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxSalaryScreen(
  viewModel: TaxSalaryViewModel,
  modifier: Modifier = Modifier,
  onNavigateBack: (() -> Unit)? = null
) {
  val state by viewModel.uiState.collectAsState()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  Scaffold(
    contentWindowInsets = WindowInsets(0, 0, 0, 0),
    snackbarHost = {
      state.toastMessage?.let { msg ->
        Snackbar(
          modifier = Modifier.padding(16.dp),
          action = {
            TextButton(onClick = { viewModel.clearToast() }) {
              Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
            }
          }
        ) {
          Text(msg)
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Top Tab Selector
      item {
        val semantic = OmniFinCalcThemeTokens.colors
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, semantic.cardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("tax_salary_tabs")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            TaxSalaryTab.values().forEach { tab ->
              val isSelected = state.selectedTab == tab
              val icon = if (tab == TaxSalaryTab.INCOME_TAX) Icons.Default.ReceiptLong else Icons.Default.AccountBalanceWallet
              Surface(
                onClick = {
                  haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                  viewModel.selectTab(tab)
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                modifier = Modifier.weight(1f)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = tab.title,
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                  )
                }
              }
            }
          }
        }
      }

      when (state.selectedTab) {
        TaxSalaryTab.INCOME_TAX -> {
          item {
            IncomeTaxSection(
              state = state,
              viewModel = viewModel,
              onSave = { viewModel.saveTaxCalculation(context) },
              onShare = {
                val res = state.taxResult
                val shareText = """
                  🇮🇳 Income Tax Comparison (${res.financialYear})
                  Gross Salary: ${CurrencyFormatter.format(res.grossSalary)}
                  
                  • New Regime Tax: ${CurrencyFormatter.format(res.newRegime.totalTaxPayable)} (Std Ded: ₹75,000)
                  • Old Regime Tax: ${CurrencyFormatter.format(res.oldRegime.totalTaxPayable)}
                  
                  Recommendation: ${res.recommendationSummary}
                  
                  Calculated with OmniFinCalc
                """.trimIndent()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  putExtra(Intent.EXTRA_TEXT, shareText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Tax Calculation"))
              }
            )
          }
        }
        TaxSalaryTab.SALARY_BREAKDOWN -> {
          item {
            SalaryBreakdownSection(
              state = state,
              viewModel = viewModel,
              onSave = { viewModel.saveSalaryCalculation(context) },
              onShare = {
                val s = state.salaryResult
                val shareText = """
                  💼 Take-Home Pay Slip Breakdown
                  Annual CTC: ${CurrencyFormatter.format(s.annualCtc)}
                  
                  • Monthly Gross Salary: ${CurrencyFormatter.format(s.monthlyGrossSalary)}
                  • Monthly In-Hand (Net): ${CurrencyFormatter.format(s.monthlyInHand)}
                  
                  Deductions:
                  - Employee PF: ${CurrencyFormatter.format(s.monthlyEpf)}
                  - Professional Tax: ${CurrencyFormatter.format(s.monthlyProfessionalTax)}
                  - Income Tax (TDS): ${CurrencyFormatter.format(s.monthlyTds)}
                  
                  Calculated with OmniFinCalc
                """.trimIndent()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                  putExtra(Intent.EXTRA_TEXT, shareText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Salary Breakdown"))
              }
            )
          }
        }
      }
    }
  }
}

@Composable
internal fun IncomeTaxSection(
  state: TaxSalaryUiState,
  viewModel: TaxSalaryViewModel,
  onSave: () -> Unit,
  onShare: () -> Unit
) {
  var showDeductions by remember { mutableStateOf(true) }
  val semantic = OmniFinCalcThemeTokens.colors
  val res = state.taxResult

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Inputs Card
    Card(
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.dp, semantic.cardBorder),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Annual Gross Salary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          AssistChip(
            onClick = {
              val nextFy = if (state.taxFinancialYear == "FY 2024-25") "FY 2025-26" else "FY 2024-25"
              viewModel.updateTaxInputs(fy = nextFy)
            },
            label = { Text(state.taxFinancialYear, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp)) }
          )
        }

        NumberInputField(
          value = state.taxAnnualGross,
          onValueChange = { viewModel.updateTaxInputs(gross = it) },
          label = "Annual Gross Income / CTC (₹)",
          prefix = "₹",
          sliderValue = (state.taxAnnualGross.toFloatOrNull() ?: 1200000f).coerceIn(100000f, 5000000f),
          sliderRange = 100000f..5000000f,
          onSliderChange = { viewModel.updateTaxInputs(gross = it.toLong().toString()) },
          presets = listOf("+1L" to 100000.0, "+2.5L" to 250000.0, "+5L" to 500000.0, "+10L" to 1000000.0),
          onPresetClick = {
            val cur = state.taxAnnualGross.toDoubleOrNull() ?: 0.0
            viewModel.updateTaxInputs(gross = (cur + it).toLong().toString())
          },
          testTag = "tax_gross_input"
        )

        // Notice banner on ₹75k Standard Deduction
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Text(
              text = "Budget 2024: New Regime includes ₹75,000 Standard Deduction + Zero Tax up to ₹7.75 Lakhs (with 87A rebate & marginal relief).",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }

        // Deductions accordion header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeductions = !showDeductions }
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text("Tax Deductions & Exemptions", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text("Applies primarily under Old Regime", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Icon(
            if (showDeductions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null
          )
        }

        AnimatedVisibility(visible = showDeductions) {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberInputField(
              value = state.taxSection80C,
              onValueChange = { viewModel.updateTaxInputs(c80 = it) },
              label = "Section 80C (EPF, PPF, ELSS, LIC - Max ₹1.5L)",
              prefix = "₹"
            )
            NumberInputField(
              value = state.taxSection80D,
              onValueChange = { viewModel.updateTaxInputs(d80 = it) },
              label = "Section 80D (Health Insurance Self/Parents - Max ₹75k)",
              prefix = "₹"
            )
            NumberInputField(
              value = state.taxHra,
              onValueChange = { viewModel.updateTaxInputs(hra = it) },
              label = "HRA Exemption (House Rent Allowance)",
              prefix = "₹"
            )
            NumberInputField(
              value = state.taxHomeLoan24b,
              onValueChange = { viewModel.updateTaxInputs(loan24b = it) },
              label = "Home Loan Interest u/s 24(b) (Max ₹2L)",
              prefix = "₹"
            )
            NumberInputField(
              value = state.taxOtherDeductions,
              onValueChange = { viewModel.updateTaxInputs(other = it) },
              label = "Other Deductions (NPS 80CCD, 80TTA, etc.)",
              prefix = "₹"
            )
          }
        }
      }
    }

    // Recommendation Banner
    val isNewRecommended = res.recommendedRegime == "New Regime"
    val bannerAccent = if (isNewRecommended) semantic.income else MaterialTheme.colorScheme.primary
    val bannerContainer = if (isNewRecommended) semantic.incomeContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, bannerAccent.copy(alpha = 0.3f)),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = CardDefaults.cardColors(containerColor = bannerContainer),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .background(bannerAccent.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Verified, contentDescription = null, tint = bannerAccent)
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Recommendation: ${res.recommendedRegime}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = bannerAccent
          )
          Text(
            text = res.recommendationSummary,
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }

    // Side-by-side comparison cards
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // New Regime Card
      RegimeCard(
        title = "New Regime",
        isWinner = isNewRecommended,
        breakdown = res.newRegime,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      )

      // Old Regime Card
      RegimeCard(
        title = "Old Regime",
        isWinner = !isNewRecommended && res.recommendedRegime != "Equal Tax",
        breakdown = res.oldRegime,
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      )
    }

    // Detailed Comparison Table
    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, semantic.cardBorder),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Detailed Tax Breakdown", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        ComparisonRow("Standard Deduction", "₹75,000", "₹50,000")
        ComparisonRow("Investments / 80C/80D", "₹0 (None)", CurrencyFormatter.format(res.oldRegime.otherDeductions))
        ComparisonRow("Taxable Income", CurrencyFormatter.format(res.newRegime.taxableIncome), CurrencyFormatter.format(res.oldRegime.taxableIncome))
        ComparisonRow("Base Slab Tax", CurrencyFormatter.format(res.newRegime.slabTax), CurrencyFormatter.format(res.oldRegime.slabTax))
        ComparisonRow("87A Rebate", "- ${CurrencyFormatter.format(res.newRegime.rebate87A)}", "- ${CurrencyFormatter.format(res.oldRegime.rebate87A)}")
        ComparisonRow("Health & Edu Cess (4%)", CurrencyFormatter.format(res.newRegime.cess), CurrencyFormatter.format(res.oldRegime.cess))
        HorizontalDivider(color = semantic.divider)
        ComparisonRow("Total Tax Payable", CurrencyFormatter.format(res.newRegime.totalTaxPayable), CurrencyFormatter.format(res.oldRegime.totalTaxPayable), isBold = true)
        ComparisonRow("Effective Tax Rate", String.format(java.util.Locale.US, "%.1f%%", res.newRegime.effectiveTaxRate), String.format(java.util.Locale.US, "%.1f%%", res.oldRegime.effectiveTaxRate))
      }
    }

    // Action buttons
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedButton(
        onClick = onShare,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, semantic.cardBorder),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share")
      }
      Button(
        onClick = onSave,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Record")
      }
    }
  }
}

@Composable
private fun RegimeCard(
  title: String,
  isWinner: Boolean,
  breakdown: IncomeTaxCalculator.RegimeTaxBreakdown,
  modifier: Modifier = Modifier
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val borderColor = if (isWinner) semantic.income else semantic.cardBorder
  val containerColor = if (isWinner) semantic.incomeContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface

  Card(
    shape = RoundedCornerShape(18.dp),
    border = BorderStroke(if (isWinner) 1.5.dp else 1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    modifier = modifier
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      if (isWinner) {
        Surface(
          shape = RoundedCornerShape(99.dp),
          color = semantic.income,
          contentColor = Color.White
        ) {
          Text("RECOMMENDED", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
      } else {
        Spacer(modifier = Modifier.height(16.dp))
      }

      Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
      Text(
        text = CurrencyFormatter.format(breakdown.totalTaxPayable),
        style = MaterialTheme.typography.titleMedium.copy(
          fontWeight = FontWeight.ExtraBold,
          fontFeatureSettings = "tnum"
        ),
        color = if (isWinner) semantic.income else MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = "Tax Payable",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      HorizontalDivider(color = semantic.divider)

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Taxable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          CurrencyFormatter.formatCompact(breakdown.taxableIncome),
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
        )
      }
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          String.format(java.util.Locale.US, "%.1f%%", breakdown.effectiveTaxRate),
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontFeatureSettings = "tnum")
        )
      }
    }
  }
}

@Composable
private fun ComparisonRow(
  label: String,
  newVal: String,
  oldVal: String,
  isBold: Boolean = false
) {
  val semantic = OmniFinCalcThemeTokens.colors
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      color = if (isBold) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1.3f)
    )
    Text(
      text = newVal,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
      ),
      color = if (isBold) semantic.income else MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.End
    )
    Text(
      text = oldVal,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        fontFeatureSettings = "tnum"
      ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(1f),
      textAlign = TextAlign.End
    )
  }
}

@Composable
internal fun SalaryBreakdownSection(
  state: TaxSalaryUiState,
  viewModel: TaxSalaryViewModel,
  onSave: () -> Unit,
  onShare: () -> Unit
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val s = state.salaryResult

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    // Inputs Card
    Card(
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.dp, semantic.cardBorder),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text("Enter Annual CTC (Cost to Company)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        NumberInputField(
          value = state.salaryAnnualCtc,
          onValueChange = { viewModel.updateSalaryInputs(ctc = it) },
          label = "Annual CTC (₹)",
          prefix = "₹",
          sliderValue = (state.salaryAnnualCtc.toFloatOrNull() ?: 1200000f).coerceIn(100000f, 5000000f),
          sliderRange = 100000f..5000000f,
          onSliderChange = { viewModel.updateSalaryInputs(ctc = it.toLong().toString()) },
          presets = listOf("+1L" to 100000.0, "+2.5L" to 250000.0, "+5L" to 500000.0, "+10L" to 1000000.0),
          onPresetClick = {
            val cur = state.salaryAnnualCtc.toDoubleOrNull() ?: 0.0
            viewModel.updateSalaryInputs(ctc = (cur + it).toLong().toString())
          },
          testTag = "salary_ctc_input"
        )

        // Regime toggle for TDS
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("TDS Tax Regime", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(
              selected = state.salaryRegime == "New Regime",
              onClick = { viewModel.updateSalaryInputs(regime = "New Regime") },
              label = { Text("New (75k Ded)", fontSize = 11.sp) }
            )
            FilterChip(
              selected = state.salaryRegime == "Old Regime",
              onClick = { viewModel.updateSalaryInputs(regime = "Old Regime") },
              label = { Text("Old Regime", fontSize = 11.sp) }
            )
          }
        }
      }
    }

    // Hero Monthly In-Hand Card
    Card(
      shape = RoundedCornerShape(20.dp),
      border = BorderStroke(1.dp, semantic.cardBorder),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text("Estimated Monthly In-Hand Take-Home", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          text = CurrencyFormatter.format(s.monthlyInHand),
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            fontFeatureSettings = "tnum"
          )
        )
        Text(
          text = "Annual Take-Home: ${CurrencyFormatter.format(s.annualInHand)}",
          style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Visual Donut Chart showing Monthly Breakdown
        DonutChart(
          slices = listOf(
            ChartSlice("Net Take-Home", s.monthlyInHand, semantic.income),
            ChartSlice("Income Tax TDS", s.monthlyTds, semantic.expense),
            ChartSlice("Employee PF", s.monthlyEpf, MaterialTheme.colorScheme.primary),
            ChartSlice("Prof Tax & Misc", s.monthlyProfessionalTax, semantic.warning)
          ),
          centerTitle = CurrencyFormatter.formatCompact(s.monthlyInHand),
          centerSubtitle = "Take-Home",
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    // Itemized Monthly Pay Slip Breakdown
    Card(
      shape = RoundedCornerShape(18.dp),
      border = BorderStroke(1.dp, semantic.cardBorder),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("Monthly Pay Slip Breakdown", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

        Text("GROSS EARNINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        BreakdownRow("Basic Salary (40% of CTC)", CurrencyFormatter.format(s.monthlyBasic))
        BreakdownRow("House Rent Allowance (HRA)", CurrencyFormatter.format(s.monthlyHra))
        BreakdownRow("Special / Other Allowance", CurrencyFormatter.format(s.monthlySpecialAllowance))
        BreakdownRow("Gross Monthly Salary", CurrencyFormatter.format(s.monthlyGrossSalary), isBold = true)

        HorizontalDivider(color = semantic.divider)

        Text("DEDUCTIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = semantic.expense)
        BreakdownRow("Employee Provident Fund (EPF 12%)", "- ${CurrencyFormatter.format(s.monthlyEpf)}")
        BreakdownRow("Professional Tax (PT)", "- ${CurrencyFormatter.format(s.monthlyProfessionalTax)}")
        BreakdownRow("Income Tax (TDS estimate)", "- ${CurrencyFormatter.format(s.monthlyTds)}")
        BreakdownRow("Total Monthly Deductions", "- ${CurrencyFormatter.format(s.monthlyTotalDeductions)}", isBold = true, isRed = true)

        HorizontalDivider(color = semantic.divider)

        BreakdownRow(
          label = "NET MONTHLY TAKE-HOME",
          value = CurrencyFormatter.format(s.monthlyInHand),
          isBold = true,
          isHighlight = true
        )
      }
    }

    // Actions
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedButton(
        onClick = onShare,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, semantic.cardBorder),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Share Slip")
      }
      Button(
        onClick = onSave,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save Pay Slip")
      }
    }
  }
}

@Composable
private fun BreakdownRow(
  label: String,
  value: String,
  isBold: Boolean = false,
  isRed: Boolean = false,
  isHighlight: Boolean = false
) {
  val semantic = OmniFinCalcThemeTokens.colors
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
      color = if (isHighlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
      ),
      color = when {
        isHighlight -> MaterialTheme.colorScheme.primary
        isRed -> semantic.expense
        else -> MaterialTheme.colorScheme.onSurface
      }
    )
  }
}
