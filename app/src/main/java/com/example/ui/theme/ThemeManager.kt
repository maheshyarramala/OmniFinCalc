package com.example.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppThemeMode(val title: String) {
  SYSTEM("System Default"),
  LIGHT("Light Mode"),
  DARK("Dark Mode"),
  AMOLED("Pure Black (AMOLED)")
}

/**
 * Curated full-palette fintech themes controlling canvas, elevated cards,
 * hero balance card metallic sheen, and primary/accent hierarchy across Light & Dark modes.
 */
enum class AccentTheme(
  val title: String,
  val subtitle: String,
  val primaryLight: Color,
  val primaryDark: Color,
  val previewCanvasLight: Color,
  val previewCanvasDark: Color,
  val previewHeroStart: Color,
  val previewHeroEnd: Color,
  val previewGoldAccent: Color
) {
  EMERALD(
    title = "Private Wealth",
    subtitle = "Warm Alabaster & Forest Emerald",
    primaryLight = Color(0xFF0E6B50),
    primaryDark = Color(0xFF34C795),
    previewCanvasLight = Color(0xFFECE8E0),
    previewCanvasDark = Color(0xFF091310),
    previewHeroStart = Color(0xFF0A2921),
    previewHeroEnd = Color(0xFF174B3D),
    previewGoldAccent = Color(0xFFD4A74A)
  ),
  INDIGO(
    title = "Neobank Frost",
    subtitle = "Cool Mist Slate & Royal Cobalt",
    primaryLight = Color(0xFF2563EB),
    primaryDark = Color(0xFF5B96FF),
    previewCanvasLight = Color(0xFFE6ECF5),
    previewCanvasDark = Color(0xFF0A1120),
    previewHeroStart = Color(0xFF0E1C36),
    previewHeroEnd = Color(0xFF1D3E7A),
    previewGoldAccent = Color(0xFF60A5FA)
  ),
  GRAPHITE(
    title = "Executive Graphite",
    subtitle = "Soft Pearl & Matte Obsidian",
    primaryLight = Color(0xFF1B1F27),
    primaryDark = Color(0xFFE2B459),
    previewCanvasLight = Color(0xFFE7E9EE),
    previewCanvasDark = Color(0xFF0B0C0F),
    previewHeroStart = Color(0xFF14161C),
    previewHeroEnd = Color(0xFF272C36),
    previewGoldAccent = Color(0xFFDFB459)
  )
}

enum class HapticStrength(val title: String) {
  CRISP("Crisp (Default)"),
  SUBTLE("Subtle"),
  OFF("Off")
}

data class AppThemeState(
  val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  val accentTheme: AccentTheme = AccentTheme.EMERALD,
  val hapticStrength: HapticStrength = HapticStrength.CRISP
)

object ThemeManager {
  // Retained internal preference filename for seamless user upgrade compatibility.
  private const val PREFERENCES_NAME = "omni_calc_user_preferences"
  private const val KEY_THEME_MODE = "theme_mode"
  private const val KEY_ACCENT_THEME = "accent_theme"
  private const val KEY_HAPTIC_STRENGTH = "haptic_strength"

  private val _state = MutableStateFlow(AppThemeState())
  val state: StateFlow<AppThemeState> = _state.asStateFlow()

  fun initialize(context: Context) {
    val savedPreferences = context.applicationContext
      .getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    preferences = savedPreferences
    _state.value = AppThemeState(
      themeMode = savedPreferences.getString(KEY_THEME_MODE, null)
        ?.let { value -> AppThemeMode.entries.firstOrNull { it.name == value } }
        ?: AppThemeMode.SYSTEM,
      accentTheme = savedPreferences.getString(KEY_ACCENT_THEME, null)
        ?.let { value -> mapSavedAccentTheme(value) }
        ?: AccentTheme.EMERALD,
      hapticStrength = savedPreferences.getString(KEY_HAPTIC_STRENGTH, null)
        ?.let { value -> HapticStrength.entries.firstOrNull { it.name == value } }
        ?: HapticStrength.CRISP
    )
  }

  private fun mapSavedAccentTheme(saved: String): AccentTheme {
    return when (saved) {
      "EMERALD", "CYAN" -> AccentTheme.EMERALD
      "INDIGO", "PURPLE" -> AccentTheme.INDIGO
      "GRAPHITE", "AMBER", "ROSE" -> AccentTheme.GRAPHITE
      else -> AccentTheme.entries.firstOrNull { it.name == saved } ?: AccentTheme.EMERALD
    }
  }

  fun setThemeMode(mode: AppThemeMode) {
    _state.update { it.copy(themeMode = mode) }
    persist(KEY_THEME_MODE, mode.name)
  }

  fun setAccentTheme(accent: AccentTheme) {
    _state.update { it.copy(accentTheme = accent) }
    persist(KEY_ACCENT_THEME, accent.name)
  }

  fun setHapticStrength(strength: HapticStrength) {
    _state.update { it.copy(hapticStrength = strength) }
    persist(KEY_HAPTIC_STRENGTH, strength.name)
  }

  private fun persist(key: String, value: String) {
    preferences?.edit()?.putString(key, value)?.apply()
  }

  @Volatile
  private var preferences: android.content.SharedPreferences? = null
}
