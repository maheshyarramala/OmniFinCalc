package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OmniFinCalcThemeTokens

@Composable
fun FintechCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  containerColor: Color = MaterialTheme.colorScheme.surface,
  borderColor: Color = OmniFinCalcThemeTokens.colors.cardBorder,
  cornerRadius: Dp = 18.dp,
  contentPadding: PaddingValues = PaddingValues(16.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val cardModifier = if (onClick != null) {
    modifier.clip(RoundedCornerShape(cornerRadius)).clickable(onClick = onClick)
  } else {
    modifier
  }
  Card(
    shape = RoundedCornerShape(cornerRadius),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (semantic.isDark) 0.dp else 1.5.dp
    ),
    modifier = cardModifier
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(contentPadding),
      content = content
    )
  }
}

@Composable
fun FintechSectionHeader(
  title: String,
  subtitle: String? = null,
  actionLabel: String? = null,
  onActionClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
    if (!actionLabel.isNullOrBlank() && onActionClick != null) {
      TextButton(
        onClick = onActionClick,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.minimumInteractiveComponentSize()
      ) {
        Text(
          text = actionLabel,
          style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
fun FintechMetricTile(
  label: String,
  value: String,
  icon: ImageVector,
  accentColor: Color,
  subtitle: String? = null,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val tileBorder = if (semantic.isDark) {
    accentColor.copy(alpha = 0.22f)
  } else {
    accentColor.copy(alpha = 0.20f)
  }
  val cardModifier = if (onClick != null) {
    modifier.clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick)
  } else {
    modifier
  }
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, tileBorder),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (semantic.isDark) 0.dp else 1.5.dp
    ),
    modifier = cardModifier
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(accentColor.copy(alpha = if (semantic.isDark) 0.06f else 0.045f))
        .padding(14.dp)
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(accentColor.copy(alpha = if (semantic.isDark) 0.18f else 0.14f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon,
              contentDescription = null,
              tint = accentColor,
              modifier = Modifier.size(17.dp)
            )
          }
          Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = value,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontFeatureSettings = "tnum",
            letterSpacing = (-0.4).sp
          ),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Medium,
              fontFeatureSettings = "tnum"
            ),
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
fun FintechStatusPill(
  text: String,
  textColor: Color,
  containerColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(999.dp),
    color = containerColor,
    modifier = modifier
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.SemiBold,
        fontFeatureSettings = "tnum"
      ),
      color = textColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
    )
  }
}

@Composable
fun FintechQuickActionButton(
  label: String,
  icon: ImageVector,
  accentColor: Color,
  onClick: () -> Unit,
  testTag: String,
  modifier: Modifier = Modifier
) {
  val semantic = OmniFinCalcThemeTokens.colors
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .minimumInteractiveComponentSize()
      .padding(horizontal = 4.dp, vertical = 6.dp)
      .testTag(testTag)
  ) {
    Surface(
      shape = RoundedCornerShape(15.dp),
      color = accentColor.copy(alpha = if (semantic.isDark) 0.16f else 0.13f),
      border = BorderStroke(1.dp, accentColor.copy(alpha = if (semantic.isDark) 0.24f else 0.22f)),
      modifier = Modifier.size(48.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = accentColor,
          modifier = Modifier.size(21.dp)
        )
      }
    }
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
      color = MaterialTheme.colorScheme.onSurface,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun FintechEmptyState(
  icon: ImageVector,
  title: String,
  description: String,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  FintechCard(
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(24.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(52.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
      )
      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
      )
      if (!actionLabel.isNullOrBlank() && onAction != null) {
        Spacer(modifier = Modifier.height(4.dp))
        Button(
          onClick = onAction,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.minimumInteractiveComponentSize()
        ) {
          Text(actionLabel, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}

@Composable
fun FintechConfirmationDialog(
  title: String,
  message: String,
  confirmLabel: String = "Confirm",
  dismissLabel: String = "Cancel",
  isDestructive: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  val semantic = OmniFinCalcThemeTokens.colors
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = if (isDestructive) {
          ButtonDefaults.buttonColors(
            containerColor = semantic.expense,
            contentColor = Color.White
          )
        } else {
          ButtonDefaults.buttonColors()
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.testTag("confirm_dialog_button")
      ) {
        Text(confirmLabel, fontWeight = FontWeight.SemiBold)
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("dismiss_dialog_button")
      ) {
        Text(dismissLabel, fontWeight = FontWeight.Medium)
      }
    },
    shape = RoundedCornerShape(22.dp)
  )
}
