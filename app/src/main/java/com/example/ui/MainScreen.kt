package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.components.AppInfoModalDialog
import com.example.ui.components.AppInfoType
import com.example.ui.components.CurrencyFormatter
import com.example.ui.screens.agecalendar.AgeCalendarScreen
import com.example.ui.screens.agecalendar.AgeCalendarTab
import com.example.ui.screens.agecalendar.AgeCalendarViewModel
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.analytics.AnalyticsViewModel
import com.example.ui.screens.calculator.CalculatorScreen
import com.example.ui.screens.calculator.CalculatorViewModel
import com.example.ui.screens.debt.DebtTrackerViewModel
import com.example.ui.screens.emi.EmiScreen
import com.example.ui.screens.emi.EmiViewModel
import com.example.ui.screens.finance.FinanceHubScreen
import com.example.ui.screens.finance.FinanceHubViewModel
import com.example.ui.screens.finance.FinanceTab
import com.example.ui.screens.history.HistoryBottomSheet
import com.example.ui.screens.history.HistoryViewModel
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.interest.InterestCalculatorScreen
import com.example.ui.screens.interest.InterestCalculatorViewModel
import com.example.ui.screens.investment.InvestmentScreen
import com.example.ui.screens.investment.InvestmentTab
import com.example.ui.screens.investment.InvestmentViewModel
import com.example.ui.screens.loan.LoanTrackerViewModel
import com.example.ui.screens.tax.TaxSalaryScreen
import com.example.ui.screens.tax.TaxSalaryViewModel
import com.example.ui.screens.utility.UtilityScreen
import com.example.ui.screens.utility.UtilityTab
import com.example.ui.screens.utility.UtilityViewModel
import com.example.ui.theme.AccentTheme
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.HapticStrength
import com.example.ui.theme.OmniFinCalcThemeTokens
import com.example.ui.theme.ThemeManager
import com.example.ui.theme.performConfiguredHapticFeedback

enum class MainDestination(
  val labelRes: Int,
  val icon: ImageVector,
  val isBottomTab: Boolean = true
) {
  HOME(R.string.navigation_home, Icons.Default.Dashboard),
  FINANCE(R.string.navigation_finance, Icons.Default.Savings),
  ANALYTICS(R.string.navigation_analytics, Icons.Default.BarChart),
  EMI(R.string.navigation_emi, Icons.Default.AccountBalance),
  CALCULATOR(R.string.navigation_calculator, Icons.Default.Calculate),
  INVESTMENTS(R.string.navigation_investments, Icons.AutoMirrored.Filled.TrendingUp, isBottomTab = false),
  UTILITIES(R.string.navigation_utility, Icons.Default.Widgets, isBottomTab = false),
  INTEREST(R.string.navigation_interest, Icons.Default.Percent, isBottomTab = false),
  AGE_CALENDAR(R.string.navigation_age_date, Icons.Default.CalendarMonth, isBottomTab = false),
  TAX_SALARY(R.string.navigation_tax_salary, Icons.Default.ReceiptLong, isBottomTab = false)
}

private val mainDestinationSaver = Saver<MainDestination, String>(
  save = { it.name },
  restore = { savedName ->
    MainDestination.entries.firstOrNull { it.name == savedName } ?: MainDestination.HOME
  }
)

data class CalculatorHubItem(
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val iconBgColor: Color,
  val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  modifier: Modifier = Modifier
) {
  var currentDestination by rememberSaveable(stateSaver = mainDestinationSaver) {
    mutableStateOf(MainDestination.HOME)
  }
  var showHistorySheet by rememberSaveable { mutableStateOf(false) }
  var showHubSheet by rememberSaveable { mutableStateOf(false) }
  var showThemeSheet by rememberSaveable { mutableStateOf(false) }
  var menuExpanded by remember { mutableStateOf(false) }
  var showAppInfoDialog by remember { mutableStateOf<AppInfoType?>(null) }
  val isPrivacyMode = CurrencyFormatter.isPrivacyModeEnabled
  var openLoanTrackerRequest by rememberSaveable { mutableIntStateOf(0) }
  var requestedNotificationPermission by rememberSaveable { mutableStateOf(false) }
  val saveableStateHolder = rememberSaveableStateHolder()

  val emiViewModel: EmiViewModel = viewModel()
  val investmentViewModel: InvestmentViewModel = viewModel()
  val financeViewModel: FinanceHubViewModel = viewModel()
  val analyticsViewModel: AnalyticsViewModel = viewModel()
  val loanViewModel: LoanTrackerViewModel = viewModel()
  val debtViewModel: DebtTrackerViewModel = viewModel()
  val interestViewModel: InterestCalculatorViewModel = viewModel()
  val calculatorViewModel: CalculatorViewModel = viewModel()
  val ageCalendarViewModel: AgeCalendarViewModel = viewModel()
  val utilityViewModel: UtilityViewModel = viewModel()
  val taxSalaryViewModel: TaxSalaryViewModel = viewModel()
  val historyViewModel: HistoryViewModel = viewModel()

  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { /* handled gracefully if denied */ }

  LaunchedEffect(Unit) {
    if (!requestedNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      requestedNotificationPermission = true
      val granted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
      if (!granted) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  showAppInfoDialog?.let { info ->
    AppInfoModalDialog(
      type = info,
      onDismiss = { showAppInfoDialog = null }
    )
  }

  // Return to Dashboard from any secondary destination
  BackHandler(enabled = currentDestination != MainDestination.HOME) {
    currentDestination = MainDestination.HOME
  }

  if (showThemeSheet) {
    ThemeSettingsBottomSheet(
      onDismiss = { showThemeSheet = false }
    )
  }

  if (showHistorySheet) {
    HistoryBottomSheet(
      viewModel = historyViewModel,
      onDismiss = { showHistorySheet = false },
      onOpenRecord = { record ->
        showHistorySheet = false
        when (record.category.uppercase()) {
          "EMI" -> {
            emiViewModel.restoreFromRecord(record)
            currentDestination = MainDestination.EMI
          }
          "CALCULATOR" -> {
            record.detailsText.lineSequence()
              .firstOrNull { it.startsWith("Input:") }
              ?.substringAfter("Input:")
              ?.trim()
              ?.let(calculatorViewModel::restoreFromHistory)
            currentDestination = MainDestination.CALCULATOR
          }
          "SIP", "LUMPSUM", "COMPOUND", "PPF", "FD", "TRADE" -> {
            investmentViewModel.restoreFromRecord(record)
            currentDestination = MainDestination.INVESTMENTS
          }
          "INTEREST" -> {
            interestViewModel.restoreFromRecord(record)
            currentDestination = MainDestination.INTEREST
          }
          "AGE", "CALENDAR" -> {
            ageCalendarViewModel.restoreFromRecord(record)
            currentDestination = MainDestination.AGE_CALENDAR
          }
          "CURRENCY", "UNIT", "TIP", "GST" -> {
            utilityViewModel.restoreFromRecord(record)
            currentDestination = MainDestination.UTILITIES
          }
          "TAX", "SALARY" -> {
            currentDestination = MainDestination.TAX_SALARY
          }
          else -> currentDestination = MainDestination.CALCULATOR
        }
      }
    )
  }

  if (showHubSheet) {
    AllCalculatorsHubSheet(
      onDismiss = { showHubSheet = false },
      items = listOf(
        CalculatorHubItem("Dashboard", "Financial overview & charts", Icons.Default.Dashboard, Color(0xFF1E3254)) {
          currentDestination = MainDestination.HOME
          showHubSheet = false
        },
        CalculatorHubItem("Reports & Analytics", "Charts, trends & PDF/CSV", Icons.Default.BarChart, Color(0xFF3478F6)) {
          currentDestination = MainDestination.ANALYTICS
          showHubSheet = false
        },
        CalculatorHubItem("Expenses & Accounts", "Budget & bank balances", Icons.Default.Savings, Color(0xFF16A879)) {
          currentDestination = MainDestination.FINANCE
          financeViewModel.selectTab(FinanceTab.EXPENSES)
          showHubSheet = false
        },
        CalculatorHubItem("Credit Cards", "Cards, limits & bill alerts", Icons.Default.CreditCard, Color(0xFF2B6CB0)) {
          currentDestination = MainDestination.FINANCE
          financeViewModel.selectTab(FinanceTab.CREDIT_CARDS)
          showHubSheet = false
        },
        CalculatorHubItem("Lend & Borrow", "Personal debt tracker", Icons.Default.SwapHoriz, Color(0xFF0E9F82)) {
          currentDestination = MainDestination.FINANCE
          financeViewModel.selectTab(FinanceTab.DEBTS)
          showHubSheet = false
        },
        CalculatorHubItem("Split Bill", "Group expense splitting", Icons.Default.Group, Color(0xFF0E9F82)) {
          currentDestination = MainDestination.FINANCE
          financeViewModel.selectTab(FinanceTab.SPLIT_BILL)
          showHubSheet = false
        },
        CalculatorHubItem("Trip Manager", "Vacation budgets & caps", Icons.Default.FlightTakeoff, Color(0xFF0EA5E9)) {
          currentDestination = MainDestination.FINANCE
          financeViewModel.selectTab(FinanceTab.TRIPS)
          showHubSheet = false
        },
        CalculatorHubItem("Loan & EMI", "Amortization & active loans", Icons.Default.AccountBalance, Color(0xFF3478F6)) {
          currentDestination = MainDestination.EMI
          showHubSheet = false
        },
        CalculatorHubItem("Calculator", "Scientific & Basic", Icons.Default.Calculate, Color(0xFF475C7A)) {
          currentDestination = MainDestination.CALCULATOR
          showHubSheet = false
        },
        CalculatorHubItem("SIP & Growth", "Wealth compounding", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF16A879)) {
          currentDestination = MainDestination.INVESTMENTS
          investmentViewModel.selectTab(InvestmentTab.SIP)
          showHubSheet = false
        },
        CalculatorHubItem("Lump Sum", "One-time investment", Icons.Default.Payments, Color(0xFF3478F6)) {
          currentDestination = MainDestination.INVESTMENTS
          investmentViewModel.selectTab(InvestmentTab.LUMPSUM)
          showHubSheet = false
        },
        CalculatorHubItem("Step-Up SIP", "Annual increment SIP", Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF0E9F82)) {
          currentDestination = MainDestination.INVESTMENTS
          investmentViewModel.selectTab(InvestmentTab.STEP_UP_SIP)
          showHubSheet = false
        },
        CalculatorHubItem("Goal Planner", "Target wealth corpus", Icons.Default.TrackChanges, Color(0xFFE7A33E)) {
          currentDestination = MainDestination.INVESTMENTS
          investmentViewModel.selectTab(InvestmentTab.GOAL_PLANNER)
          showHubSheet = false
        },
        CalculatorHubItem("Interest Calc", "Simple & compound methods", Icons.Default.Percent, Color(0xFF2B6CB0)) {
          currentDestination = MainDestination.INTEREST
          showHubSheet = false
        },
        CalculatorHubItem("Currency", "Live exchange conversion", Icons.Default.CurrencyExchange, Color(0xFFE7A33E)) {
          currentDestination = MainDestination.UTILITIES
          utilityViewModel.selectTab(UtilityTab.CURRENCY)
          showHubSheet = false
        },
        CalculatorHubItem("Unit Converter", "Length, weight & temp", Icons.Default.Straighten, Color(0xFF16A879)) {
          currentDestination = MainDestination.UTILITIES
          utilityViewModel.selectTab(UtilityTab.UNIT_CONVERTER)
          showHubSheet = false
        },
        CalculatorHubItem("GST & Tax", "Inclusive & exclusive GST", Icons.AutoMirrored.Filled.ReceiptLong, Color(0xFF3478F6)) {
          currentDestination = MainDestination.UTILITIES
          utilityViewModel.selectTab(UtilityTab.GST_TAX)
          showHubSheet = false
        },
        CalculatorHubItem("Income Tax", "Old vs New Regime", Icons.Default.ReceiptLong, Color(0xFF0E9F82)) {
          currentDestination = MainDestination.UTILITIES
          utilityViewModel.selectTab(UtilityTab.INCOME_TAX)
          showHubSheet = false
        },
        CalculatorHubItem("In-Hand Salary", "CTC to Take-Home pay slip", Icons.Default.AccountBalanceWallet, Color(0xFF3478F6)) {
          currentDestination = MainDestination.UTILITIES
          utilityViewModel.selectTab(UtilityTab.SALARY)
          showHubSheet = false
        },
        CalculatorHubItem("Age Calculator", "Exact age & birthday", Icons.Default.Cake, Color(0xFFE56B70)) {
          currentDestination = MainDestination.AGE_CALENDAR
          ageCalendarViewModel.selectTab(AgeCalendarTab.AGE)
          showHubSheet = false
        },
        CalculatorHubItem("Calendar Date", "Difference & add days", Icons.Default.CalendarMonth, Color(0xFF0EA5E9)) {
          currentDestination = MainDestination.AGE_CALENDAR
          ageCalendarViewModel.selectTab(AgeCalendarTab.DATE_DIFF)
          showHubSheet = false
        }
      )
    )
  }

  val bottomTabs = remember { MainDestination.entries.filter { it.isBottomTab } }

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable { showHubSheet = true }
                .padding(horizontal = 2.dp, vertical = 4.dp)
                .testTag("omnifincalc_brand_title")
            ) {
              val primaryColor = MaterialTheme.colorScheme.primary
              val onSurfaceColor = MaterialTheme.colorScheme.onSurface
              val brandText = remember(primaryColor, onSurfaceColor) {
                buildAnnotatedString {
                  withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.ExtraBold)) {
                    append("Omni")
                  }
                  withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.ExtraBold)) {
                    append("Fin")
                  }
                  withStyle(SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
                    append("Calc")
                  }
                }
              }
              Text(
                text = brandText,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontSize = 20.sp,
                  letterSpacing = (-0.4).sp
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
              )
            }
          },
          actions = {
            IconButton(
              onClick = {
                haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                CurrencyFormatter.isPrivacyModeEnabled = !CurrencyFormatter.isPrivacyModeEnabled
              },
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("privacy_mode_top_button")
            ) {
              Icon(
                imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (isPrivacyMode) "Show financial figures" else "Hide financial figures",
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = {
                haptic.performConfiguredHapticFeedback(HapticFeedbackType.LongPress)
                showHistorySheet = true
              },
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("history_top_button")
            ) {
              Icon(Icons.Default.History, contentDescription = "Saved calculation history", modifier = Modifier.size(20.dp))
            }

            IconButton(
              onClick = {
                haptic.performConfiguredHapticFeedback(HapticFeedbackType.LongPress)
                showHubSheet = true
              },
              modifier = Modifier
                .minimumInteractiveComponentSize()
                .testTag("hub_top_button")
            ) {
              Icon(Icons.Default.GridView, contentDescription = "All tools and calculators", modifier = Modifier.size(20.dp))
            }

            Box {
              IconButton(
                onClick = {
                  haptic.performConfiguredHapticFeedback(HapticFeedbackType.LongPress)
                  menuExpanded = true
                },
                modifier = Modifier
                  .minimumInteractiveComponentSize()
                  .testTag("top_overflow_menu_button")
              ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Settings and more options", modifier = Modifier.size(20.dp))
              }
              if (menuExpanded) {
                val financeUiState by financeViewModel.uiState.collectAsState()
                DropdownMenu(
                  expanded = menuExpanded,
                  onDismissRequest = { menuExpanded = false },
                  containerColor = MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(16.dp)
                ) {
                  DropdownMenuItem(
                    text = { Text("Appearance & Themes") },
                    onClick = {
                      menuExpanded = false
                      showThemeSheet = true
                    },
                    modifier = Modifier.testTag("theme_settings_menu_item")
                  )
                  DropdownMenuItem(
                    text = { Text("Backup & Restore") },
                    onClick = {
                      menuExpanded = false
                      currentDestination = MainDestination.FINANCE
                      financeViewModel.setShowBackupModal(true)
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Export Finance (CSV)") },
                    onClick = {
                      menuExpanded = false
                      financeViewModel.exportCsv(context)
                    }
                  )
                  DropdownMenuItem(
                    text = {
                      val isLocked = financeUiState.isBiometricLockEnabled
                      Text(if (isLocked) "Disable Biometric Lock" else "Enable Biometric Lock")
                    },
                    onClick = {
                      menuExpanded = false
                      financeViewModel.setBiometricLockEnabled(!financeUiState.isBiometricLockEnabled)
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("About OmniFinCalc") },
                    onClick = {
                      menuExpanded = false
                      showAppInfoDialog = AppInfoType.ABOUT
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Terms & Conditions") },
                    onClick = {
                      menuExpanded = false
                      showAppInfoDialog = AppInfoType.TERMS
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Privacy Policy") },
                    onClick = {
                      menuExpanded = false
                      showAppInfoDialog = AppInfoType.PRIVACY
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Feedback & Contact") },
                    onClick = {
                      menuExpanded = false
                      showAppInfoDialog = AppInfoType.CONTACT
                    }
                  )
                  DropdownMenuItem(
                    text = { Text("Support Development") },
                    onClick = {
                      menuExpanded = false
                      showAppInfoDialog = AppInfoType.DONATE
                    }
                  )
                }
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
          )
        )
      },
      bottomBar = {
        if (!useNavigationRail) {
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = if (OmniFinCalcThemeTokens.colors.isDark) 0.dp else 8.dp
          ) {
            Column {
              HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
              )
              NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
              ) {
              bottomTabs.forEach { destination ->
                val isSelected = currentDestination == destination
                NavigationBarItem(
                  selected = isSelected,
                  onClick = {
                    haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                    currentDestination = destination
                    if (destination == MainDestination.FINANCE) {
                      financeViewModel.selectTab(FinanceTab.EXPENSES)
                    }
                  },
                  icon = {
                    Icon(
                      destination.icon,
                      contentDescription = stringResource(destination.labelRes),
                      modifier = Modifier.size(21.dp)
                    )
                  },
                  label = {
                    Text(
                      text = stringResource(destination.labelRes),
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      maxLines = 1,
                      softWrap = false,
                      overflow = TextOverflow.Ellipsis
                    )
                  },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                )
              }
            }
          }
          }
        }
      },
      contentWindowInsets = WindowInsets.systemBars,
      modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        if (useNavigationRail) {
          NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier
              .fillMaxHeight()
              .testTag("navigation_rail")
          ) {
            Spacer(modifier = Modifier.height(8.dp))
            bottomTabs.forEach { destination ->
              NavigationRailItem(
                selected = currentDestination == destination,
                onClick = {
                  haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                  currentDestination = destination
                  if (destination == MainDestination.FINANCE) {
                    financeViewModel.selectTab(FinanceTab.EXPENSES)
                  }
                },
                icon = {
                  Icon(destination.icon, contentDescription = stringResource(destination.labelRes))
                },
                label = {
                  Text(
                    text = stringResource(destination.labelRes),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  selectedTextColor = MaterialTheme.colorScheme.primary,
                  indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                  unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag("nav_rail_${destination.name.lowercase()}")
              )
            }
          }
          VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        Crossfade(
          targetState = currentDestination,
          animationSpec = tween(durationMillis = 220),
          label = "screenTransition",
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
        ) { destination ->
          saveableStateHolder.SaveableStateProvider(destination.name) {
            when (destination) {
              MainDestination.HOME -> HomeScreen(
                financeViewModel = financeViewModel,
                analyticsViewModel = analyticsViewModel,
                loanViewModel = loanViewModel,
                debtViewModel = debtViewModel,
                isPrivacyMode = isPrivacyMode,
                onTogglePrivacyMode = {
                  CurrencyFormatter.isPrivacyModeEnabled = !CurrencyFormatter.isPrivacyModeEnabled
                },
                onAddExpenseClick = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.EXPENSES)
                  financeViewModel.setShowAddExpense(true)
                },
                onAddIncomeClick = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.EXPENSES)
                  financeViewModel.setShowAddIncome(true)
                },
                onTransferClick = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.EXPENSES)
                  financeViewModel.setShowTransferDialog(true)
                },
                onAddDebtClick = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.DEBTS)
                  debtViewModel.setShowAddDebtDialog(true)
                },
                onAddLoanClick = {
                  openLoanTrackerRequest++
                  currentDestination = MainDestination.EMI
                  loanViewModel.setShowAddLoanDialog(true)
                },
                onSplitBillClick = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.SPLIT_BILL)
                },
                onReportsClick = {
                  currentDestination = MainDestination.ANALYTICS
                },
                onNavigateToFinance = { tab ->
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(tab)
                },
                onNavigateToLoans = {
                  openLoanTrackerRequest++
                  currentDestination = MainDestination.EMI
                },
                onNavigateToCalculatorHub = {
                  showHubSheet = true
                }
              )
              MainDestination.FINANCE -> FinanceHubScreen(viewModel = financeViewModel)
              MainDestination.ANALYTICS -> AnalyticsScreen(
                viewModel = analyticsViewModel,
                onNavigateToAddExpense = {
                  currentDestination = MainDestination.FINANCE
                  financeViewModel.selectTab(FinanceTab.EXPENSES)
                  financeViewModel.setShowAddExpense(true)
                }
              )
              MainDestination.EMI -> EmiScreen(
                viewModel = emiViewModel,
                loanViewModel = loanViewModel,
                openLoanTrackerRequest = openLoanTrackerRequest
              )
              MainDestination.CALCULATOR -> CalculatorScreen(viewModel = calculatorViewModel)
              MainDestination.INVESTMENTS -> InvestmentScreen(viewModel = investmentViewModel)
              MainDestination.UTILITIES -> UtilityScreen(
                viewModel = utilityViewModel,
                taxSalaryViewModel = taxSalaryViewModel,
                onNavigateToTaxSalary = { tab ->
                  taxSalaryViewModel.selectTab(tab)
                  currentDestination = MainDestination.TAX_SALARY
                }
              )
              MainDestination.INTEREST -> InterestCalculatorScreen(viewModel = interestViewModel)
              MainDestination.AGE_CALENDAR -> AgeCalendarScreen(viewModel = ageCalendarViewModel)
              MainDestination.TAX_SALARY -> TaxSalaryScreen(
                viewModel = taxSalaryViewModel,
                onNavigateBack = { currentDestination = MainDestination.HOME }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AllCalculatorsHubSheet(
  items: List<CalculatorHubItem>,
  onDismiss: () -> Unit
) {
  val haptic = LocalHapticFeedback.current
  var searchQuery by rememberSaveable { mutableStateOf("") }

  val filteredItems = remember(items, searchQuery) {
    if (searchQuery.isBlank()) items
    else items.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.subtitle.contains(searchQuery, ignoreCase = true)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "OmniFinCalc Tools",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Finance managers, reports & calculators",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search tools (EMI, SIP, tax, currency, split...)", fontSize = 13.sp) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          },
          trailingIcon = {
            if (searchQuery.isNotBlank()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("hub_search_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredItems.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No tools matching \"$searchQuery\"",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 14.sp
            )
          }
        } else {
          LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            items(filteredItems, key = { it.title }, contentType = { "calculator_hub_item" }) { item ->
              Card(
                onClick = {
                  haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                  item.onClick()
                },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(40.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(item.iconBgColor),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      item.icon,
                      contentDescription = item.title,
                      tint = Color.White,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.title,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = MaterialTheme.colorScheme.onSurface,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = item.subtitle,
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingsBottomSheet(
  onDismiss: () -> Unit
) {
  val themeState by ThemeManager.state.collectAsState()
  val haptic = LocalHapticFeedback.current

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 36.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = stringResource(R.string.theme_settings_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = stringResource(R.string.theme_settings_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }

      // Theme Mode — Clean 2x2 Grid (zero text wrapping on compact phones)
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = stringResource(R.string.theme_mode_label),
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth()
        )
        val modeRows = remember { AppThemeMode.entries.chunked(2) }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          modeRows.forEach { rowModes ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              rowModes.forEach { mode ->
                val isSelected = themeState.themeMode == mode
                val modeIcon = when (mode) {
                  AppThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                  AppThemeMode.LIGHT -> Icons.Default.LightMode
                  AppThemeMode.DARK -> Icons.Default.DarkMode
                  AppThemeMode.AMOLED -> Icons.Default.Contrast
                }
                val modeLabel = when (mode) {
                  AppThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
                  AppThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
                  AppThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
                  AppThemeMode.AMOLED -> stringResource(R.string.theme_mode_amoled)
                }
                Surface(
                  onClick = {
                    haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                    ThemeManager.setThemeMode(mode)
                  },
                  shape = RoundedCornerShape(14.dp),
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                  },
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.primary
                    } else {
                      MaterialTheme.colorScheme.outlineVariant
                    }
                  ),
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("theme_mode_${mode.name.lowercase()}")
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                  ) {
                    Icon(
                      imageVector = modeIcon,
                      contentDescription = null,
                      tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = modeLabel,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                      color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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

      // Signature Fintech Themes — 3 Full-Palette Visual Showcase Cards
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Signature Theme",
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth()
        )
        val isDarkPreview = OmniFinCalcThemeTokens.colors.isDark
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          AccentTheme.entries.forEach { accent ->
            val isSelected = themeState.accentTheme == accent
            val activeAccentColor = if (isDarkPreview) accent.primaryDark else accent.primaryLight
            val miniCanvas = if (isDarkPreview) accent.previewCanvasDark else accent.previewCanvasLight
            val miniCard = if (isDarkPreview) Color(0xFF1A222E) else Color.White
            Surface(
              onClick = {
                haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
                ThemeManager.setAccentTheme(accent)
              },
              shape = RoundedCornerShape(16.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (isDarkPreview) 0.45f else 0.55f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
              },
              border = BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) activeAccentColor else MaterialTheme.colorScheme.outlineVariant
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("accent_theme_${accent.name.lowercase()}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                // Miniature Live Theme Swatch (Canvas + Hero Card + Mini White Tile)
                Box(
                  modifier = Modifier
                    .size(width = 64.dp, height = 46.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(miniCanvas)
                    .padding(5.dp)
                ) {
                  Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                  ) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(19.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                          Brush.horizontalGradient(
                            colors = listOf(accent.previewHeroStart, accent.previewHeroEnd)
                          )
                        )
                        .padding(horizontal = 4.dp),
                      contentAlignment = Alignment.CenterEnd
                    ) {
                      Box(
                        modifier = Modifier
                          .size(width = 12.dp, height = 5.dp)
                          .clip(RoundedCornerShape(999.dp))
                          .background(accent.previewGoldAccent)
                      )
                    }
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .height(12.dp)
                          .clip(RoundedCornerShape(4.dp))
                          .background(miniCard)
                      )
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .height(12.dp)
                          .clip(RoundedCornerShape(4.dp))
                          .background(miniCard)
                      )
                    }
                  }
                }

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = accent.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = accent.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Box(
                  modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeAccentColor else activeAccentColor.copy(alpha = 0.22f)),
                  contentAlignment = Alignment.Center
                ) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.size(15.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Keypad Haptic Feedback — Uniform-height segmented tiles
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = stringResource(R.string.theme_haptics_label),
          fontWeight = FontWeight.Bold,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          HapticStrength.entries.forEach { strength ->
            val isSelected = themeState.hapticStrength == strength
            val primaryLabel = when (strength) {
              HapticStrength.CRISP -> "Crisp"
              HapticStrength.SUBTLE -> stringResource(R.string.haptic_subtle)
              HapticStrength.OFF -> stringResource(R.string.haptic_off)
            }
            val secondaryLabel = if (strength == HapticStrength.CRISP) "Default" else null
            Surface(
              onClick = {
                haptic.performConfiguredHapticFeedback(HapticFeedbackType.LongPress)
                ThemeManager.setHapticStrength(strength)
              },
              shape = RoundedCornerShape(14.dp),
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              },
              border = BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) {
                  MaterialTheme.colorScheme.primary
                } else {
                  MaterialTheme.colorScheme.outlineVariant
                }
              ),
              modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .testTag("haptic_strength_${strength.name.lowercase()}")
            ) {
              Column(
                modifier = Modifier
                  .fillMaxSize()
                  .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = primaryLabel,
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  softWrap = false
                )
                if (secondaryLabel != null) {
                  Text(
                    text = secondaryLabel,
                    fontSize = 10.sp,
                    color = if (isSelected) {
                      MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    } else {
                      MaterialTheme.colorScheme.onSurfaceVariant
                    },
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
  }
}
