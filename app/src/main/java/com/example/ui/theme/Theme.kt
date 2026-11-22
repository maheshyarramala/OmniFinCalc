package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

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
  val elevatedSurface: Color,
  val divider: Color,
  val heroStart: Color,
  val heroEnd: Color,
  val heroAccent: Color,
  val heroCtaContainer: Color,
  val heroCtaContent: Color,
  val cardBorder: Color,
  val isDark: Boolean
)

val LocalFintechColors = staticCompositionLocalOf {
  FintechSemanticColors(
    income = FinanceIncomeGreen,
    incomeContainer = Color(0xFFE3F5EE),
    expense = FinanceExpenseCoral,
    expenseContainer = Color(0xFFFBEAEC),
    savings = FinanceSavingsTeal,
    savingsContainer = Color(0xFFE2F4F0),
    warning = FinanceWarningAmber,
    warningContainer = Color(0xFFFDF1DC),
    elevatedSurface = WealthSurfaceVariantLight,
    divider = WealthBorderLight,
    heroStart = Color(0xFF0A2921),
    heroEnd = Color(0xFF174B3D),
    heroAccent = Color(0xFFD4A74A),
    heroCtaContainer = Color(0xFFD4A74A),
    heroCtaContent = Color(0xFF13201B),
    cardBorder = WealthBorderLight,
    isDark = false
  )
}

object OmniFinCalcThemeTokens {
  val colors: FintechSemanticColors
    @Composable
    get() = LocalFintechColors.current
}

private fun buildLightColorScheme(theme: AccentTheme): ColorScheme {
  return when (theme) {
    AccentTheme.EMERALD -> lightColorScheme(
      primary = WealthPrimaryLight,
      onPrimary = Color.White,
      primaryContainer = WealthPrimaryContainerLight,
      onPrimaryContainer = WealthOnPrimaryContainerLight,
      secondary = Color(0xFF147D5F),
      onSecondary = Color.White,
      secondaryContainer = Color(0xFFDCEEE7),
      onSecondaryContainer = Color(0xFF0A3D2E),
      tertiary = WealthGoldLight,
      onTertiary = Color.White,
      tertiaryContainer = Color(0xFFF8EED8),
      onTertiaryContainer = Color(0xFF5D400B),
      background = WealthBgLight,
      onBackground = WealthOnSurfaceLight,
      surface = WealthSurfaceLight,
      onSurface = WealthOnSurfaceLight,
      surfaceVariant = WealthSurfaceVariantLight,
      onSurfaceVariant = WealthOnSurfaceVariantLight,
      outline = WealthOutlineLight,
      outlineVariant = WealthBorderLight,
      error = FinanceExpenseCoral,
      onError = Color.White
    )

    AccentTheme.INDIGO -> lightColorScheme(
      primary = FrostPrimaryLight,
      onPrimary = Color.White,
      primaryContainer = FrostPrimaryContainerLight,
      onPrimaryContainer = FrostOnPrimaryContainerLight,
      secondary = Color(0xFF0D8F74),
      onSecondary = Color.White,
      secondaryContainer = Color(0xFFDEF3EE),
      onSecondaryContainer = Color(0xFF0A4338),
      tertiary = FinanceWarningAmber,
      onTertiary = Color.White,
      tertiaryContainer = Color(0xFFFDF1DC),
      onTertiaryContainer = Color(0xFF663F0B),
      background = FrostBgLight,
      onBackground = FrostOnSurfaceLight,
      surface = FrostSurfaceLight,
      onSurface = FrostOnSurfaceLight,
      surfaceVariant = FrostSurfaceVariantLight,
      onSurfaceVariant = FrostOnSurfaceVariantLight,
      outline = FrostOutlineLight,
      outlineVariant = FrostBorderLight,
      error = FinanceExpenseCoral,
      onError = Color.White
    )

    AccentTheme.GRAPHITE -> lightColorScheme(
      primary = GraphitePrimaryLight,
      onPrimary = Color.White,
      primaryContainer = GraphitePrimaryContainerLight,
      onPrimaryContainer = GraphiteOnPrimaryContainerLight,
      secondary = Color(0xFF0E8762),
      onSecondary = Color.White,
      secondaryContainer = Color(0xFFDFF3EC),
      onSecondaryContainer = Color(0xFF093E2D),
      tertiary = Color(0xFFC4861C),
      onTertiary = Color.White,
      tertiaryContainer = Color(0xFFFAF0DA),
      onTertiaryContainer = Color(0xFF5D3E08),
      background = GraphiteBgLight,
      onBackground = GraphiteOnSurfaceLight,
      surface = GraphiteSurfaceLight,
      onSurface = GraphiteOnSurfaceLight,
      surfaceVariant = GraphiteSurfaceVariantLight,
      onSurfaceVariant = GraphiteOnSurfaceVariantLight,
      outline = GraphiteOutlineLight,
      outlineVariant = GraphiteBorderLight,
      error = FinanceExpenseCoral,
      onError = Color.White
    )
  }
}

private fun buildDarkColorScheme(theme: AccentTheme, isAmoled: Boolean): ColorScheme {
  val base = when (theme) {
    AccentTheme.EMERALD -> darkColorScheme(
      primary = WealthPrimaryDark,
      onPrimary = Color(0xFF05261B),
      primaryContainer = WealthPrimaryContainerDark,
      onPrimaryContainer = WealthOnPrimaryContainerDark,
      secondary = Color(0xFF4AD3A5),
      onSecondary = Color(0xFF05281D),
      secondaryContainer = Color(0xFF12362B),
      onSecondaryContainer = Color(0xFFD4F8EB),
      tertiary = WealthGoldDark,
      onTertiary = Color(0xFF2D1E04),
      tertiaryContainer = Color(0xFF3A2D13),
      onTertiaryContainer = Color(0xFFFAECBE),
      background = WealthBgDark,
      onBackground = WealthOnSurfaceDark,
      surface = WealthSurfaceDark,
      onSurface = WealthOnSurfaceDark,
      surfaceVariant = WealthSurfaceVariantDark,
      onSurfaceVariant = WealthOnSurfaceVariantDark,
      outline = WealthOutlineDark,
      outlineVariant = WealthBorderDark,
      error = FinanceExpenseCoralDark,
      onError = Color(0xFF3A0B0E)
    )

    AccentTheme.INDIGO -> darkColorScheme(
      primary = FrostPrimaryDark,
      onPrimary = Color(0xFF08142B),
      primaryContainer = FrostPrimaryContainerDark,
      onPrimaryContainer = FrostOnPrimaryContainerDark,
      secondary = FinanceSavingsTealDark,
      onSecondary = Color(0xFF062920),
      secondaryContainer = Color(0xFF12362E),
      onSecondaryContainer = Color(0xFFD1FAF0),
      tertiary = FinanceWarningAmberDark,
      onTertiary = Color(0xFF3D2504),
      tertiaryContainer = Color(0xFF3E2D12),
      onTertiaryContainer = Color(0xFFFEF3DC),
      background = FrostBgDark,
      onBackground = FrostOnSurfaceDark,
      surface = FrostSurfaceDark,
      onSurface = FrostOnSurfaceDark,
      surfaceVariant = FrostSurfaceVariantDark,
      onSurfaceVariant = FrostOnSurfaceVariantDark,
      outline = FrostOutlineDark,
      outlineVariant = FrostBorderDark,
      error = FinanceExpenseCoralDark,
      onError = Color(0xFF3A0B0E)
    )

    AccentTheme.GRAPHITE -> darkColorScheme(
      primary = GraphitePrimaryDark,
      onPrimary = GraphiteOnPrimaryDark,
      primaryContainer = GraphitePrimaryContainerDark,
      onPrimaryContainer = GraphiteOnPrimaryContainerDark,
      secondary = FinanceIncomeGreenDark,
      onSecondary = Color(0xFF06291E),
      secondaryContainer = Color(0xFF143129),
      onSecondaryContainer = Color(0xFFD2F9EC),
      tertiary = Color(0xFF9CA3AF),
      onTertiary = Color(0xFF111318),
      tertiaryContainer = Color(0xFF262A35),
      onTertiaryContainer = Color(0xFFE5E7EB),
      background = GraphiteBgDark,
      onBackground = GraphiteOnSurfaceDark,
      surface = GraphiteSurfaceDark,
      onSurface = GraphiteOnSurfaceDark,
      surfaceVariant = GraphiteSurfaceVariantDark,
      onSurfaceVariant = GraphiteOnSurfaceVariantDark,
      outline = GraphiteOutlineDark,
      outlineVariant = GraphiteBorderDark,
      error = FinanceExpenseCoralDark,
      onError = Color(0xFF3A0B0E)
    )
  }

  return if (isAmoled) {
    base.copy(
      background = Color(0xFF000000),
      surface = when (theme) {
        AccentTheme.EMERALD -> Color(0xFF0A1411)
        AccentTheme.INDIGO -> Color(0xFF0C1322)
        AccentTheme.GRAPHITE -> Color(0xFF101217)
      },
      surfaceVariant = when (theme) {
        AccentTheme.EMERALD -> Color(0xFF12231D)
        AccentTheme.INDIGO -> Color(0xFF141E33)
        AccentTheme.GRAPHITE -> Color(0xFF191C24)
      }
    )
  } else {
    base
  }
}

private val OmniFinCalcShapes = Shapes(
  extraSmall = RoundedCornerShape(8.dp),
  small = RoundedCornerShape(12.dp),
  medium = RoundedCornerShape(16.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(26.dp)
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
  val isAmoled = themeState.themeMode == AppThemeMode.AMOLED
  val activeTheme = themeState.accentTheme

  val colorScheme = if (isDark) {
    buildDarkColorScheme(activeTheme, isAmoled)
  } else {
    buildLightColorScheme(activeTheme)
  }

  val semanticColors = if (isDark) {
    val (heroStart, heroEnd, heroAccent, ctaContainer, ctaContent) = when (activeTheme) {
      AccentTheme.EMERALD -> FiveColors(
        Color(0xFF0D2C23), Color(0xFF195142), Color(0xFFE3B85B), Color(0xFFE3B85B), Color(0xFF13201B)
      )
      AccentTheme.INDIGO -> FiveColors(
        Color(0xFF112142), Color(0xFF1D3B73), Color(0xFF60A5FA), Color(0xFF5B96FF), Color(0xFF08142B)
      )
      AccentTheme.GRAPHITE -> FiveColors(
        Color(0xFF171A22), Color(0xFF282D3A), Color(0xFFE2B459), Color(0xFFE2B459), Color(0xFF181205)
      )
    }
    FintechSemanticColors(
      income = FinanceIncomeGreenDark,
      incomeContainer = Color(0xFF102E27),
      expense = FinanceExpenseCoralDark,
      expenseContainer = Color(0xFF35191F),
      savings = FinanceSavingsTealDark,
      savingsContainer = Color(0xFF102D2A),
      warning = FinanceWarningAmberDark,
      warningContainer = Color(0xFF362811),
      elevatedSurface = colorScheme.surfaceVariant,
      divider = colorScheme.outlineVariant,
      heroStart = heroStart,
      heroEnd = heroEnd,
      heroAccent = heroAccent,
      heroCtaContainer = ctaContainer,
      heroCtaContent = ctaContent,
      cardBorder = colorScheme.outlineVariant,
      isDark = true
    )
  } else {
    val (heroStart, heroEnd, heroAccent, ctaContainer, ctaContent) = when (activeTheme) {
      AccentTheme.EMERALD -> FiveColors(
        Color(0xFF0A2921), Color(0xFF174B3D), Color(0xFFD9AE57), Color(0xFFD9AE57), Color(0xFF12211B)
      )
      AccentTheme.INDIGO -> FiveColors(
        Color(0xFF0E1C36), Color(0xFF1D3E7A), Color(0xFF60A5FA), Color(0xFF3B82F6), Color.White
      )
      AccentTheme.GRAPHITE -> FiveColors(
        Color(0xFF14161C), Color(0xFF262B36), Color(0xFFDFB459), Color(0xFFDFB459), Color(0xFF14120B)
      )
    }
    FintechSemanticColors(
      income = FinanceIncomeGreen,
      incomeContainer = Color(0xFFE1F5EC),
      expense = FinanceExpenseCoral,
      expenseContainer = Color(0xFFFBE9EB),
      savings = FinanceSavingsTeal,
      savingsContainer = Color(0xFFE0F4F0),
      warning = FinanceWarningAmber,
      warningContainer = Color(0xFFFDF0DA),
      elevatedSurface = colorScheme.surfaceVariant,
      divider = colorScheme.outlineVariant,
      heroStart = heroStart,
      heroEnd = heroEnd,
      heroAccent = heroAccent,
      heroCtaContainer = ctaContainer,
      heroCtaContent = ctaContent,
      cardBorder = colorScheme.outlineVariant,
      isDark = false
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
      }
    }
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

private data class FiveColors(
  val a: Color,
  val b: Color,
  val c: Color,
  val d: Color,
  val e: Color
)
