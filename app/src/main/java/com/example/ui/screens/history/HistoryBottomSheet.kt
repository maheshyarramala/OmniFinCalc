package com.example.ui.screens.history
import com.example.ui.theme.performConfiguredHapticFeedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.data.local.entity.CalculationRecord
import com.example.ui.theme.OmniFinCalcThemeTokens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HISTORY_DATE_FORMATTER: DateTimeFormatter =
  DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault())

private val HISTORY_FILTERS = listOf(
  "ALL" to "All",
  "EMI" to "Loans & EMI",
  "SIP" to "SIP",
  "LUMPSUM" to "Lump Sum",
  "CALCULATOR" to "Calculator",
  "AGE" to "Age",
  "CALENDAR" to "Calendar",
  "CURRENCY" to "Currency",
  "TIP" to "Split Bill",
  "GST" to "GST"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
  viewModel: HistoryViewModel,
  onDismiss: () -> Unit,
  onOpenRecord: (CalculationRecord) -> Unit = {}
) {
  val historyList by viewModel.historyList.collectAsState()
  val selectedFilter by viewModel.selectedFilter.collectAsState()
  val context = LocalContext.current
  val haptic = LocalHapticFeedback.current
  val semantic = OmniFinCalcThemeTokens.colors
  var showClearConfirm by remember { mutableStateOf(false) }
  var selectedRecord by remember { mutableStateOf<CalculationRecord?>(null) }

  val filters = HISTORY_FILTERS

  if (showClearConfirm) {
    AlertDialog(
      onDismissRequest = { showClearConfirm = false },
      shape = RoundedCornerShape(22.dp),
      containerColor = MaterialTheme.colorScheme.surface,
      title = {
        Text(
          "Clear All History?",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Text(
          "This will permanently remove all your saved calculations.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAll()
            showClearConfirm = false
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = semantic.expense)
        ) {
          Text("Clear All", fontWeight = FontWeight.SemiBold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  selectedRecord?.let { record ->
    AlertDialog(
      onDismissRequest = { selectedRecord = null },
      shape = RoundedCornerShape(22.dp),
      containerColor = MaterialTheme.colorScheme.surface,
      title = {
        Text(
          record.title,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      },
      text = {
        Column(
          Modifier
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            border = BorderStroke(1.dp, semantic.cardBorder),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              record.summary,
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum"
              ),
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.padding(12.dp)
            )
          }
          Text(
            record.detailsText,
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { onOpenRecord(record); selectedRecord = null },
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Open calculator", fontWeight = FontWeight.SemiBold)
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedRecord = null }) { Text("Close") }
      }
    )
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    modifier = Modifier.testTag("history_bottom_sheet")
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
        Column {
          Text(
            text = "Saved Calculations",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${historyList.size} records saved locally",
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        if (historyList.isNotEmpty()) {
          TextButton(
            onClick = { showClearConfirm = true },
            colors = ButtonDefaults.textButtonColors(contentColor = semantic.expense)
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(filters, key = { it.first }, contentType = { "history_filter" }) { (key, label) ->
          val isSelected = selectedFilter == key
          FilterChip(
            selected = isSelected,
            onClick = {
              haptic.performConfiguredHapticFeedback(HapticFeedbackType.TextHandleMove)
              viewModel.setFilter(key)
            },
            label = {
              Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
              )
            },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primary,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(
              width = 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else semantic.cardBorder
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (historyList.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Default.BookmarkBorder,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No saved calculations yet",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Use the bookmark icon on any screen to save.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxHeight(0.7f)
        ) {
          items(historyList, key = { it.id }, contentType = { "history_record" }) { item ->
            HistoryItemCard(
              item = item,
              onShare = { viewModel.shareRecord(context, item) },
              onDelete = { viewModel.delete(item) },
              onClick = { selectedRecord = item }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun HistoryItemCard(
  item: CalculationRecord,
  onShare: () -> Unit,
  onDelete: () -> Unit,
  onClick: () -> Unit
) {
  val semantic = OmniFinCalcThemeTokens.colors
  val formattedDate = remember(item.timestamp) {
    Instant.ofEpochMilli(item.timestamp).atZone(ZoneId.systemDefault()).format(HISTORY_DATE_FORMATTER)
  }
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    border = BorderStroke(1.dp, semantic.cardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
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
        IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
          Icon(
            Icons.Default.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
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
