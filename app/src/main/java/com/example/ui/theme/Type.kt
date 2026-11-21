package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val DisplayFontFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
  Font(R.font.plus_jakarta_sans, FontWeight.Bold),
  Font(R.font.plus_jakarta_sans, FontWeight.ExtraBold)
)

val BodyFontFamily = FontFamily(
  Font(R.font.inter, FontWeight.Normal),
  Font(R.font.inter, FontWeight.Medium),
  Font(R.font.inter, FontWeight.SemiBold),
  Font(R.font.inter, FontWeight.Bold)
)

val NumericFontFamily = FontFamily(
  Font(R.font.jetbrains_mono, FontWeight.Normal),
  Font(R.font.jetbrains_mono, FontWeight.Medium),
  Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 40.sp,
    lineHeight = 46.sp,
    letterSpacing = (-0.8).sp,
    fontFeatureSettings = "tnum"
  ),
  displayMedium = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.5).sp,
    fontFeatureSettings = "tnum"
  ),
  displaySmall = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 26.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.3).sp,
    fontFeatureSettings = "tnum"
  ),
  headlineLarge = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.4).sp,
    fontFeatureSettings = "tnum"
  ),
  headlineMedium = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.2).sp,
    fontFeatureSettings = "tnum"
  ),
  headlineSmall = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 19.sp,
    lineHeight = 25.sp,
    letterSpacing = (-0.15).sp,
    fontFeatureSettings = "tnum"
  ),
  titleLarge = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.15).sp,
    fontFeatureSettings = "tnum"
  ),
  titleMedium = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 21.sp,
    letterSpacing = 0.sp,
    fontFeatureSettings = "tnum"
  ),
  titleSmall = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp,
    fontFeatureSettings = "tnum"
  ),
  bodyLarge = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.1.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.5.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.15.sp
  ),
  bodySmall = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 17.sp,
    letterSpacing = 0.2.sp
  ),
  labelLarge = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.1.sp
  ),
  labelMedium = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.5.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.25.sp
  ),
  labelSmall = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 10.5.sp,
    lineHeight = 14.sp,
    letterSpacing = 0.3.sp
  )
)
