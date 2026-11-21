package com.example.ui.screens.history
import com.example.ui.theme.performConfiguredHapticFeedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OmniFinCalcThemeTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FAVORITES_DATE_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())

private val QUICK_LAUNCH_TOOLS = listOf(
  "EMI" to "Loan EMI",
  "INVEST" to "SIP & Growth",
  "EXPENSE" to "Expenses",
  "CALC" to "Scientific"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesBottomSheet(
  viewModel: HistoryViewModel,
  onOpenTool: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val favoritesList by viewModel.favoritesList.collectAsState()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val semantic = OmniFinCalcThemeTokens.colors

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("favorites_bottom_sheet")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp)
        .padding(bottom = 32.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Favorites & Starred",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
        }
        Text(
          text = "${favoritesList.size} starred",
          style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Quick Access Favorite Shortcuts
      Text(
        text = "Quick Launch Tools",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(QUICK_LAUNCH_TOOLS, key = { it.first }) { (key, label) ->
          AssistChip(
            onClick = {
              haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
              onOpenTool(key)
              onDismiss()
            },
            label = {
              Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
              )
            },
            leadingIcon = {
              Icon(
                Icons.Default.FlashOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = AssistChipDefaults.assistChipColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
              labelColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = "Starred Calculations",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(8.dp))

      if (favoritesList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.StarBorder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No starred calculations yet",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tap the star icon in History or bookmark in calculators to pin here.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxHeight(0.6f)
        ) {
          items(favoritesList, key = { it.id }, contentType = { "favorite_record" }) { item ->
            val formattedDate = remember(item.timestamp) {
              Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).format(FAVORITES_DATE_FORMATTER)
            }
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
              ),
              border = BorderStroke(1.dp, semantic.cardBorder),
              elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                  ) {
                    Text(
                      text = item.category,
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                      color = MaterialTheme.colorScheme.onPrimaryContainer,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                  Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                  text = item.title,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                  color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                  text = item.summary,
                  style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  IconButton(
                    onClick = {
                      haptic.performConfiguredHapticFeedback(HapticFeedbackType.LongPress)
                      viewModel.toggleFavorite(item)
                    },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(
                      Icons.Default.Star,
                      contentDescription = "Remove from Favorites",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(20.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.shareRecord(context, item) },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(
                      Icons.Default.Share,
                      contentDescription = "Share",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.delete(item) },
                    modifier = Modifier.size(36.dp)
                  ) {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = "Delete",
                      tint = semantic.expense,
                      modifier = Modifier.size(18.dp)
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
