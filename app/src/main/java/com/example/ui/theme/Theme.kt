package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class FintechSemanticColors(
  val income: Color,
  val incomeContainer: Color,
  val expense: Color,
  val expenseContainer: Color,
  val savings: Color,
  val savingsContainer: Color,
  val warning: Color,
  val warningContainer: Color,
  val heroStart: Color,
  val heroEnd: Color,
  val cardBorder: Color,
  val isDark: Boolean
)

val LocalFintechColors = staticCompositionLocalOf {
  FintechSemanticColors(
    income = FinanceIncomeGreen,
    incomeContainer = Color(0xFFECFDF5),
    expense = FinanceExpenseCoral,
    expenseContainer = Color(0xFFFEF2F2),
    savings = FinanceSavingsTeal,
    savingsContainer = Color(0xFFF0FDFA),
    warning = FinanceWarningAmber,
    warningContainer = Color(0xFFFFFBEB),
    heroStart = NavyPrimary,
    heroEnd = Color(0xFF1D3461),
    cardBorder = OutlineVariantLight,
    isDark = false
  )
}

object OmniFinCalcThemeTokens {
  val colors: FintechSemanticColors
    @Composable
    get() = LocalFintechColors.current
}

private val BaseDarkColorScheme =
  darkColorScheme(
    primary = FintechPrimaryDark,
    onPrimary = FintechOnPrimaryDark,
    primaryContainer = FintechPrimaryContainerDark,
    onPrimaryContainer = FintechOnPrimaryContainerDark,
    secondary = FintechSecondaryDark,
    onSecondary = FintechOnSecondaryDark,
    secondaryContainer = FintechSecondaryContainerDark,
    onSecondaryContainer = FintechOnSecondaryContainerDark,
    tertiary = FintechTertiaryDark,
    onTertiary = FintechOnTertiaryDark,
    tertiaryContainer = FintechTertiaryContainerDark,
    onTertiaryContainer = FintechOnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = FinanceExpenseCoralDark,
    onError = Color(0xFF3F070A)
  )

private val BaseLightColorScheme =
  lightColorScheme(
    primary = FintechPrimaryLight,
    onPrimary = FintechOnPrimaryLight,
    primaryContainer = FintechPrimaryContainerLight,
    onPrimaryContainer = FintechOnPrimaryContainerLight,
    secondary = FintechSecondaryLight,
    onSecondary = FintechOnSecondaryLight,
    secondaryContainer = FintechSecondaryContainerLight,
    onSecondaryContainer = FintechOnSecondaryContainerLight,
    tertiary = FintechTertiaryLight,
    onTertiary = FintechOnTertiaryLight,
    tertiaryContainer = FintechTertiaryContainerLight,
    onTertiaryContainer = FintechOnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = FinanceExpenseCoral,
    onError = Color.White
  )

private val OmniFinCalcShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun OmniFinCalcTheme(
  content: @Composable () -> Unit,
) {
  val themeState by ThemeManager.state.collectAsState()
  val isSystemDark = isSystemInDarkTheme()

  val isDark = when (themeState.themeMode) {
    AppThemeMode.SYSTEM -> isSystemDark
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK, AppThemeMode.AMOLED -> true
  }

  val baseScheme = if (isDark) BaseDarkColorScheme else BaseLightColorScheme
  val primaryColor = if (isDark) themeState.accentTheme.primaryDark else themeState.accentTheme.primaryLight
  val primaryContainerColor = primaryColor.copy(alpha = if (isDark) 0.22f else 0.12f)
  val onPrimaryContainerColor = if (isDark) Color(0xFFEAF2FF) else NavyPrimary

  val colorScheme = if (themeState.themeMode == AppThemeMode.AMOLED) {
    baseScheme.copy(
      primary = primaryColor,
      primaryContainer = primaryContainerColor,
      onPrimaryContainer = onPrimaryContainerColor,
      background = Color(0xFF000000),
      surface = Color(0xFF090D16),
      surfaceVariant = Color(0xFF141B2B),
      outlineVariant = Color(0xFF1E293B),
      onBackground = Color(0xFFFFFFFF),
      onSurface = Color(0xFFFFFFFF)
    )
  } else {
    baseScheme.copy(
      primary = primaryColor,
      primaryContainer = primaryContainerColor,
      onPrimaryContainer = onPrimaryContainerColor
    )
  }

  val semanticColors = if (isDark) {
    FintechSemanticColors(
      income = FinanceIncomeGreenDark,
      incomeContainer = Color(0xFF0D2E25),
      expense = FinanceExpenseCoralDark,
      expenseContainer = Color(0xFF3A161A),
      savings = FinanceSavingsTealDark,
      savingsContainer = Color(0xFF0D2E30),
      warning = FinanceWarningAmberDark,
      warningContainer = Color(0xFF3A280A),
      heroStart = Color(0xFF101B35),
      heroEnd = Color(0xFF1A3566),
      cardBorder = if (themeState.themeMode == AppThemeMode.AMOLED) Color(0xFF1F2937) else OutlineVariantDark,
      isDark = true
    )
  } else {
    FintechSemanticColors(
      income = FinanceIncomeGreen,
      incomeContainer = Color(0xFFECFDF5),
      expense = FinanceExpenseCoral,
      expenseContainer = Color(0xFFFEF2F2),
      savings = FinanceSavingsTeal,
      savingsContainer = Color(0xFFF0FDFA),
      warning = FinanceWarningAmber,
      warningContainer = Color(0xFFFFFBEB),
      heroStart = NavyPrimary,
      heroEnd = Color(0xFF1E3A6E),
      cardBorder = OutlineVariantLight,
      isDark = false
    )
  }

  CompositionLocalProvider(LocalFintechColors provides semanticColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      shapes = OmniFinCalcShapes,
      content = content
    )
  }
}
